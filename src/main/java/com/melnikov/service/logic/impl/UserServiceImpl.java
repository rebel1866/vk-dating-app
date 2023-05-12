package com.melnikov.service.logic.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.melnikov.dao.model.Photo;
import com.melnikov.dao.model.User;
import com.melnikov.dao.model.constant.Relation;
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
import com.melnikov.util.converter.UserVoToModelConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private JsonParser<SearchUserResponseWrapperVo<UserVo>> jsonParserUsers;
    private JsonParser<SearchUserResponseWrapperVo<PhotoVo>> jsonParserPhotos;
    private UserRepository userRepository;
    private NameService nameService;
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

    private final AtomicBoolean isContinue = new AtomicBoolean();

    @Autowired
    public void setJsonParserUsers(JsonParser<SearchUserResponseWrapperVo<UserVo>> jsonParserUsers) {
        this.jsonParserUsers = jsonParserUsers;
    }

    @Autowired
    public void setJsonParserPhotos(JsonParser<SearchUserResponseWrapperVo<PhotoVo>> jsonParserPhotos) {
        this.jsonParserPhotos = jsonParserPhotos;
    }

    @Autowired
    public void setNameService(NameService nameService) {
        this.nameService = nameService;
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void startIndexing(Integer amount, String accessToken, Integer tokenExpires) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> runIndexing(amount, accessToken, tokenExpires));
    }

    private void runIndexing(Integer amount, String accessToken, Integer tokenExpires) {
        Map<String, String> params = new HashMap<>();
        initParams(params);
        params.put("access_token", accessToken);
        LocalDateTime expireTime = LocalDateTime.now().plusSeconds(tokenExpires - 300);
        List<User> userList = new ArrayList<>();
        isContinue.set(true);
        int currentIteration = -1;
        Map<Integer, String> executionJournal = new HashMap<>();
        while (checkLoopCriteria(amount, userList)) {
            currentIteration++;
            LocalDateTime now = LocalDateTime.now();
            if (expireTime.isBefore(now)) {
                logger.info("Token has expired");
                break;
            }
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
            logger.info(params.toString());
            try {
                response = HttpClient.sendPOST("https://api.vk.com/method/users.search", params);
                logger.info("Got response. Parsing.");
                logger.info(response);
                response = response.replaceAll("personal\":\\[\\]", "personal\":{}");
                responseWrapperVO = jsonParserUsers.parseJson(response, new TypeReference<>() {
                });
                logger.info("Successfully parsed json.");
            } catch (IOException e) {
                logger.error("Error while making request and parsing it: " + e.getMessage());
                executionJournal.put(currentIteration, "Fail");
                boolean isThreeOneAfterAnother = isThreeErrorsOneAfterAnother(currentIteration, executionJournal);
                if (isThreeOneAfterAnother) {
                    logger.error("3 errors one after another - stopping execution.");
                    break;
                }
                continue;
            }
            List<UserVo> currentUsersVo = responseWrapperVO.getResponse().getItems();
            logger.info("currentUsersVo size: " + currentUsersVo.size());
            List<UserVo> basicCriteriaUsersVo = filterUsersBasicCriteria(currentUsersVo);
            logger.info("Filtered currentUsersVo by basic criterias, result list size: " + basicCriteriaUsersVo.size());
            //also take those accs are currently closed and write to db
            List<User> usersWithPhotos = basicCriteriaUsersVo.stream().map(UserVoToModelConverter::convert).
                    collect(Collectors.toList());
            for (User user : usersWithPhotos) {
                try {
                    setPhotos(user, accessToken);
                } catch (ServiceException e) {
                    logger.error(String.format("Error while trying to set photos for user with id %s : %s",
                            user.getId(), e.getMessage()));
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
            executionJournal.put(currentIteration, "Success");
        }
    }

    private boolean isThreeErrorsOneAfterAnother(int currentIteration, Map<Integer, String> executionJournal) {
        if (executionJournal.size() < 3) {
            return false;
        }
        int prevIteration = currentIteration - 1;
        int prevPrevIteration = currentIteration - 2;
        return executionJournal.get(prevIteration).equals("Fail") && executionJournal.get(prevPrevIteration).equals("Fail");
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
    public List<UserDto> getUsers(String accessToken, int amount, String city, Integer ageFrom, Integer ageTo,
                                  String name) throws ServiceException {
        return new ArrayList<>();
    }

    private List<User> filterUsersByPhotoAmount(List<User> usersWithPhotos) {
        return usersWithPhotos.stream().
                filter(user -> user.getPhotos().size() >= amountPhotoForFiltering).collect(Collectors.toList());
    }


    private void setPhotos(User user, String accessToken) throws ServiceException {
        String response;
        SearchUserResponseWrapperVo<PhotoVo> responseWrapperVO;
        Map<String, String> params = new HashMap<>();
        params.put("owner_id", user.getId().toString());
        params.put("access_token", accessToken);
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
        long countLastSeenFiltered = currentUsersVo.stream().filter(this::lastSeenFilter).count();
        logger.info("Filtered amount of accounts inactive: " + ((long) currentUsersVo.size() - countLastSeenFiltered));
        return currentUsersVo.stream().
                filter(el -> el.getIsClosed() != null && el.getIsClosed().equals(false)).
                filter(el -> el.getHasPhoto() != null && el.getHasPhoto().equals(true)).
                filter(this::lastSeenFilter).
                filter(el -> {
                    if (el.getRelation() == null) {
                        return true;
                    } else {
                        return !el.getRelation().equals(Relation.N2.getNumber())
                                && !el.getRelation().equals(Relation.N3.getNumber()) && !el.getRelation().equals(Relation.N4.getNumber())
                                && !el.getRelation().equals(Relation.N7.getNumber()) && !el.getRelation().equals(Relation.N8.getNumber());
                    }
                }).
                toList();
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
    }
}
