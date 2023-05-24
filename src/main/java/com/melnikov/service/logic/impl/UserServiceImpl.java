package com.melnikov.service.logic.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.melnikov.dao.model.ClosedUser;
import com.melnikov.dao.model.Photo;
import com.melnikov.dao.model.User;
import com.melnikov.dao.model.constant.Relation;
import com.melnikov.dao.repository.ClosedUserRepository;
import com.melnikov.dao.repository.UserCustomRepository;
import com.melnikov.dao.repository.UserRepository;
import com.melnikov.service.constant.VkDatingAppConstants;
import com.melnikov.service.dto.UserDto;
import com.melnikov.service.exception.ServiceException;
import com.melnikov.service.logic.NameService;
import com.melnikov.service.logic.UserService;
import com.melnikov.service.vo.*;
import com.melnikov.util.DateUtil;
import com.melnikov.util.HttpClient;
import com.melnikov.util.JsonParser;
import com.melnikov.util.converter.UserModelToDtoConverter;
import com.melnikov.util.converter.UserVoToModelConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private JsonParser<SearchUserResponseWrapperVo<UserVo>> jsonParserUsers;
    private JsonParser<SearchUserResponseWrapperVo<PhotoVo>> jsonParserPhotos;
    private JsonParser<UserGetVoWrapper> jsonParserUser;
    private UserRepository userRepository;
    private NameService nameService;
    private ClosedUserRepository closedUserRepository;
    private UserCustomRepository userCustomRepository;
    private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Value("${basic.age.from}")
    private Byte basicAgeFrom;
    @Value("${basic.age.to}")
    private Byte basicAgeTo;
    @Value("${amount.days.last.seen}")
    private Short amountDaysLastSeen;
    @Value("${photos.basic.amount}")
    private Byte photosBasicAmount;
    @Value("${amount.photos.for.filtering}")
    private Byte amountPhotoForFiltering;

    @Value("${amount.days.when.need.refresh}")
    private Byte amountDaysForRefresh;

    private final AtomicBoolean isContinue = new AtomicBoolean();

    @Autowired
    public UserServiceImpl(JsonParser<SearchUserResponseWrapperVo<UserVo>> jsonParserUsers,
                           JsonParser<SearchUserResponseWrapperVo<PhotoVo>> jsonParserPhotos,
                           JsonParser<UserGetVoWrapper> jsonParserUser, UserRepository userRepository,
                           NameService nameService, ClosedUserRepository closedUserRepository,
                           UserCustomRepository userCustomRepository) {
        this.jsonParserUsers = jsonParserUsers;
        this.jsonParserPhotos = jsonParserPhotos;
        this.jsonParserUser = jsonParserUser;
        this.userRepository = userRepository;
        this.nameService = nameService;
        this.closedUserRepository = closedUserRepository;
        this.userCustomRepository = userCustomRepository;
    }

    @Override
    public void startIndexing(Integer amount) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> runIndexing(amount));
    }

    private void runIndexing(Integer amount) {
        Map<String, String> params = new HashMap<>();
        initParams(params);
        List<User> userList = new ArrayList<>();
        isContinue.set(true);
        int currentIteration = -1;
        Map<Integer, ExecutionRecord> executionJournal = new LinkedHashMap<>();
        while (checkLoopCriteria(amount, userList)) {
            currentIteration++;
            ApiSearchRequestVo requestVo;
            try {
                requestVo = nameService.getRequestVo();
            } catch (ServiceException e) {
                logger.error(e.getMessage());
                break;
            }
            logger.info("RequestVo: " + requestVo);
            params.put("q", requestVo.getQ());
            params.put("birth_day", requestVo.getBirthDay().toString());
            params.put("birth_month", requestVo.getBirthMonth().toString());
            String response;
            SearchUserResponseWrapperVo<UserVo> responseWrapperVO;
            logger.info("Sending request.");
            try {
                response = HttpClient.sendPOST("https://api.vk.com/method/users.search", params);
                logger.info("Got response. Parsing.");
                response = response.replaceAll("personal\":\\[\\]", "personal\":{}");
                responseWrapperVO = jsonParserUsers.parseJson(response, new TypeReference<>() {
                });
                logger.info("Successfully parsed json.");
            } catch (IOException e) {
                logger.error("Error while making request and parsing it: " + e.getMessage());
                executionJournal.put(currentIteration, new ExecutionRecord(false, null, 0, requestVo));
                boolean isThreeOneAfterAnother = isThreeErrorsOneAfterAnother(currentIteration, executionJournal);
                if (isThreeOneAfterAnother) {
                    logger.error("3 errors one after another - stopping execution.");
                    break;
                }
                continue;
            }
            List<UserVo> currentUsersVo = responseWrapperVO.getResponse().getItems();
            if (currentUsersVo.size() == 0) {
                logger.info("Empty response has been detected.");
                executionJournal.put(currentIteration, new ExecutionRecord(false, true,
                        0, requestVo));
                continue;
            }
            logger.info("found users: " + currentUsersVo.size());
            List<UserVo> basicCriteriaUsersVo = filterUsersBasicCriteria(currentUsersVo);
            logger.info("Filtered currentUsersVo by basic criterias, amount: " + basicCriteriaUsersVo.size());
            List<ClosedUser> usersClosed = currentUsersVo.stream().
                    filter(el -> el.getHasPhoto() != null && el.getHasPhoto().equals(true)).
                    filter(this::lastSeenFilter).
                    filter(el -> el.getIsClosed() != null && el.getIsClosed().equals(true)).
                    map(user -> new ClosedUser(user.getId(), LocalDateTime.now())).toList();
            logger.info("Found closed users, amount: " + usersClosed.size());
            closedUserRepository.saveAll(usersClosed);
            logger.info("Saved all closed users");
            List<User> usersWithPhotos = basicCriteriaUsersVo.stream().map(UserVoToModelConverter::convert).
                    collect(Collectors.toList());
            List<String> comments = new ArrayList<>();
            for (User user : usersWithPhotos) {
                try {
                    setPhotos(user);
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (ServiceException | InterruptedException e) {
                    logger.error(String.format("Error while trying to set photos for user with id %s : %s",
                            user.getId(), e.getMessage()));
                    comments.add("Failed to set photos for user with id: " + user.getId());
                    user.setPhotos(new ArrayList<>());
                }
            }
            logger.info("Photos have been set.");
            usersWithPhotos = filterUsersByPhotoAmount(usersWithPhotos);
            logger.info("Filtered users by photo amount, result list size: " + usersWithPhotos.size());
            userRepository.saveAll(usersWithPhotos);
            logger.info("Saved all.");
            userList.addAll(usersWithPhotos);
            logger.info("Target user list size: " + userList.size());
            executionJournal.put(currentIteration, new ExecutionRecord(true, false,
                    usersWithPhotos.size(), requestVo, comments));
            logger.info("Current iteration: " + currentIteration);
        }
        StringBuilder executionInfo = new StringBuilder();
        executionInfo.append("\n");
        executionJournal.forEach((key, value) -> {
            executionInfo.append("Iteration: ").append(key);
            executionInfo.append("\n");
            executionInfo.append(value.toString());
            executionInfo.append("\n");
        });
        logger.info(executionInfo.toString());
    }

    private boolean isThreeErrorsOneAfterAnother(int currentIteration, Map<Integer, ExecutionRecord> executionJournal) {
        if (executionJournal.size() < 3) {
            return false;
        }
        int prevIteration = currentIteration - 1;
        int prevPrevIteration = currentIteration - 2;
        return !executionJournal.get(prevIteration).getIsSuccess() && !executionJournal.get(prevPrevIteration).getIsSuccess();
    }

    @Override
    public void stopIndexing() {
        isContinue.set(false);
    }

    private boolean checkLoopCriteria(Integer amount, List<User> userList) {
        if (!isContinue.get()) {
            return false;
        }
        if (amount == null) {
            return true;
        } else {
            return userList.size() <= amount;
        }
    }

    @Override
    public List<UserDto> getUsers(int amount, String city, Integer ageFrom, Integer ageTo, String name) throws ServiceException {
        logger.info("Request - " + String.format("amount: %s, city: %s, ageFrom: %s, ageTo: %s, name: %s", amount, city,
                ageFrom, ageTo, name));
        List<User> userList = new ArrayList<>();
        for (int page = 0; userList.size() != amount; page++) {
            List<User> currentUsers = userCustomRepository.findUsersByParams(page, amount, city, ageFrom, ageTo, name);
            if (currentUsers.size() == 0) {
                break;
            }
            for (User user : currentUsers) {
                if (isStillActual(user)) {
                    userList.add(user);
                    if (userList.size() == amount) {
                        break;
                    }
                }
            }
        }
        if (userList.size() == 0) {
            throw new ServiceException("No users found");
        }
        return userList.stream().map(UserModelToDtoConverter::convert).collect(Collectors.toList());
    }

    private boolean isStillActual(User user) {
        long periodDays = TimeUnit.MILLISECONDS.toDays(Duration.between(user.getSavingTime(), LocalDateTime.now()).toMillis());
        if (periodDays < amountDaysForRefresh) {
            return true;
        }
        String response;
        Map<String, String> params = new HashMap<>();
        params.put("user_ids", user.getId().toString());
        params.put("fields", VkDatingAppConstants.USER_SEARCH_FIELDS);
        params.put("access_token", VkDatingAppConstants.ACCESS_TOKEN);
        params.put("v", VkDatingAppConstants.API_VERSION);
        UserGetVoWrapper userGetVoWrapper;
        try {
            response = HttpClient.sendPOST("https://api.vk.com/method/users.get", params);
            response = response.replaceAll("personal\":\\[\\]", "personal\":{}");
            userGetVoWrapper = jsonParserUser.parseJson(response, new TypeReference<>(){});
        } catch (IOException e) {
            logger.error("Could not update user with id " + user.getId() + ": " + e.getMessage());
            return false;
        }
        List<UserVo> userUpds = userGetVoWrapper.getResponse();
        if (userUpds.size() == 0) {
            return false;
        }
        UserVo userUpd = userUpds.get(0);
        if (!lastSeenFilter(userUpd) || !userUpd.getHasPhoto()) {
            userRepository.deleteById(userUpd.getId());
            return false;
        }
        if (!userUpd.getIsClosed()) {
            if (!relationFilter(userUpd)) {
                userRepository.deleteById(userUpd.getId());
                return false;
            }
        }
        updateBasicUserFields(user, userUpd);
        return true;
    }

    private void updateBasicUserFields(User user, UserVo userUpd) {
        user.setCanSendFriendRequest(userUpd.getCanSendFriendRequest());
        user.setCanWritePrivateMessage(userUpd.getCanWritePrivateMessage());
        user.setIsFriend(userUpd.getIsFriend());
        user.setIsVkFavorite(userUpd.getIsFavorite());
    }

    private List<User> filterUsersByPhotoAmount(List<User> usersWithPhotos) {
        return usersWithPhotos.stream().
                filter(user -> user.getPhotos().size() >= amountPhotoForFiltering).collect(Collectors.toList());
    }


    private void setPhotos(User user) throws ServiceException {
        String response;
        SearchUserResponseWrapperVo<PhotoVo> responseWrapperVO;
        Map<String, String> params = new HashMap<>();
        params.put("owner_id", user.getId().toString());
        params.put("access_token", VkDatingAppConstants.ACCESS_TOKEN);
        params.put("v", VkDatingAppConstants.API_VERSION);
        try {
            response = HttpClient.sendPOST("https://api.vk.com/method/photos.getAll", params);
            responseWrapperVO = jsonParserPhotos.parseJson(response, new TypeReference<>() {
            });
        } catch (IOException e) {
            logger.error("Error while trying to make request for photos and/or parse it: " + e.getMessage());
            throw new ServiceException(e.getMessage());
        }
        List<PhotoVo> photoVoList = responseWrapperVO.getResponse().getItems();
        List<Photo> photoList = new ArrayList<>();
        for (int i = 0; i < photoVoList.size(); i++) {
            if (i == photosBasicAmount) {
                break;
            }
            PhotoVo photoVo = photoVoList.get(i);
            Photo photo = new Photo();
            photo.setDateTime(DateUtil.unixToLocalDateTime(photoVo.getDate()));
            for (SizeVo sizeVo : photoVo.getSizes()) {
                String type = "x";
                if (type.equals(sizeVo.getType())) {
                    photo.setUrl(sizeVo.getUrl());
                    break;
                }
            }
            photoList.add(photo);
        }
        user.setPhotos(photoList);
    }


    private List<UserVo> filterUsersBasicCriteria(List<UserVo> currentUsersVo) {
        return currentUsersVo.stream().
                filter(el -> el.getIsClosed() != null && el.getIsClosed().equals(false)).
                filter(el -> el.getHasPhoto() != null && el.getHasPhoto().equals(true)).
                filter(this::lastSeenFilter).
                filter(this::relationFilter).
                toList();
        //status & relatives - child
    }

    private boolean relationFilter(UserVo userVo) {
        if (userVo.getRelation() == null) {
            return true;
        } else {
            return !userVo.getRelation().equals(Relation.N2.getNumber())
                    && !userVo.getRelation().equals(Relation.N3.getNumber())
                    && !userVo.getRelation().equals(Relation.N4.getNumber())
                    && !userVo.getRelation().equals(Relation.N7.getNumber())
                    && !userVo.getRelation().equals(Relation.N8.getNumber());
        }
    }

    private boolean lastSeenFilter(UserVo userVo) {
        if (userVo.getLastSeen() == null) {
            return false;
        }
        long unixTimeNow = Instant.now().getEpochSecond();
        long timeLastSeenUnix = userVo.getLastSeen().getTime();
        return (unixTimeNow - timeLastSeenUnix) <= (amountDaysLastSeen * 24 * 60 * 60);
    }

    private void initParams(Map<String, String> params) {
        params.put("v", VkDatingAppConstants.API_VERSION);
        params.put("fields", VkDatingAppConstants.USER_SEARCH_FIELDS);
        params.put("age_from", basicAgeFrom.toString());
        params.put("age_to", basicAgeTo.toString());
        params.put("country_id", VkDatingAppConstants.COUNTRY_ID.toString());
        params.put("sex", VkDatingAppConstants.SEX.toString());
        params.put("count", VkDatingAppConstants.COUNT.toString());
        params.put("access_token", VkDatingAppConstants.ACCESS_TOKEN);
    }

    @Override
    public void updateUserByParams(Long id, Map<String, Object> params) throws ServiceException {
        User user = userRepository.findById(id).orElseThrow(() -> new ServiceException("User has not been found with id: " + id));
        Object hasBeenViewedStr = params.get("hasBeenViewed");
        if (hasBeenViewedStr != null) {
            user.setHasBeenViewed(Boolean.valueOf((String) hasBeenViewedStr));
        }
        userRepository.save(user);
    }
}
