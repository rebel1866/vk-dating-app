package com.melnikov.service.logic.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.melnikov.dao.model.Photo;
import com.melnikov.dao.model.User;
import com.melnikov.dao.model.constant.Relation;
import com.melnikov.dao.repository.UserRepository;
import com.melnikov.service.constant.VkDatingAppConstants;
import com.melnikov.service.dto.UserDto;
import com.melnikov.service.exception.ServiceException;
import com.melnikov.service.logic.UserService;
import com.melnikov.service.vo.*;
import com.melnikov.util.DateUtil;
import com.melnikov.util.HttpClient;
import com.melnikov.util.JsonParser;
import com.melnikov.util.converter.UserVoToModelConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private JsonParser<SearchUserResponseWrapperVo<UserVo>> jsonParserUsers;
    private JsonParser<SearchUserResponseWrapperVo<PhotoVo>> jsonParserPhotos;
    private UserRepository userRepository;
    @Value("${basic.age.from}")
    private Byte basicAgeFrom;
    @Value("${basic.age.to}")
    private Byte basicAgeTo;
    @Value("${amount.days.last.seen}")
    private Short amountDaysLastSeen;
    @Value("${photos.basic.amount}")
    private Byte photosBasicAmount;

    @Autowired
    public void setJsonParserUsers(JsonParser<SearchUserResponseWrapperVo<UserVo>> jsonParserUsers) {
        this.jsonParserUsers = jsonParserUsers;
    }

    @Autowired
    public void setJsonParserPhotos(JsonParser<SearchUserResponseWrapperVo<PhotoVo>> jsonParserPhotos) {
        this.jsonParserPhotos = jsonParserPhotos;
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<UserDto> getUsers(String accessToken, int amount, String city, Integer ageFrom, Integer ageTo) throws ServiceException {
        Map<String, String> params = new HashMap<>();
        initParams(params);
        params.put("access_token", accessToken);
        List<User> userList = new ArrayList<>();
        // search for users in db - check/update lastSeen,hasphoto,isClosed,relation conditions and update some fields specified in User class
        while (userList.size() < amount) {
            ApiSearchRequestVo requestVo = getRequestVo();
            params.put("q", requestVo.getQ());
            params.put("birth_day", requestVo.getBirthDay().toString());
            params.put("birth_month", requestVo.getBirthMonth().toString());
            String response;
            SearchUserResponseWrapperVo<UserVo> responseWrapperVO;
            try {
                response = HttpClient.sendPOST("https://api.vk.com/method/users.search", params);
                responseWrapperVO = jsonParserUsers.parseJson(response, new TypeReference<>() {
                });
            } catch (IOException e) {
                // logger
                throw new ServiceException(e.getMessage());
            }
            List<UserVo> currentUsersVo = responseWrapperVO.getResponse().getItems();
            List<UserVo> basicCriteriaUsersVo = filterUsersBasicCriteria(currentUsersVo); //also take those accs are currently closed
            List<User> usersWithPhotos = basicCriteriaUsersVo.stream().map(UserVoToModelConverter::convert).
                    collect(Collectors.toList());
            for (User user : usersWithPhotos) {
                setPhotos(user, accessToken);
            }
            userRepository.saveAll(usersWithPhotos);
            List<User> usersListForCurrentView = filterUsersForCurrentView(usersWithPhotos, ageFrom, ageTo, city);
            userList.addAll(usersListForCurrentView);
        }
        //convert to dto and return
        //sublist and return
        return new ArrayList<>();
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
            // logger
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

    private ApiSearchRequestVo getRequestVo() {
        return new ApiSearchRequestVo();
    }

    private List<UserVo> filterUsersBasicCriteria(List<UserVo> currentUsersVo) {
        return currentUsersVo.stream().
                filter(el -> el.getIsClosed() != null && el.getIsClosed().equals(false)).
                filter(el -> el.getHasPhoto() != null && el.getHasPhoto().equals(true)).
                filter(el -> {
                    if (el.getLastSeen() == null) {
                        return false;
                    }
                    long unixTimeNow = Instant.now().getEpochSecond();
                    long timeLastSeenUnix = el.getLastSeen().getTime();
                    return (unixTimeNow - timeLastSeenUnix) <= (amountDaysLastSeen * 24 * 60 * 60);
                }).
                filter(el -> el.getRelation() != null && !el.getRelation().equals(Relation.N2.getNumber())
                        && !el.getRelation().equals(Relation.N3.getNumber()) && !el.getRelation().equals(Relation.N4.getNumber())
                        && !el.getRelation().equals(Relation.N7.getNumber()) && !el.getRelation().equals(Relation.N8.getNumber())).
                toList();
    }

    private List<User> filterUsersForCurrentView(List<User> users, Integer ageFrom, Integer ageTo, String city) {
        List<User> userListToView = new ArrayList<>();
        if (ageFrom != null && ageTo != null) {
            userListToView = users.stream().filter(el -> {
                try {
                    int age = DateUtil.parseAge(el.getBdate());
                    return ageFrom > age && ageTo < age;
                } catch (DateTimeException e) {
                    return false;
                }
            }).collect(Collectors.toList());
        }
        if (city != null) {
            userListToView = userListToView.stream().filter(el ->
                    city.equalsIgnoreCase(el.getCityName())).collect(Collectors.toList());
        }
        return userListToView;
    }

    private void initParams(Map<String, String> params) {
        params.put("v", VkDatingAppConstants.API_VERSION);
        params.put("fields", VkDatingAppConstants.USER_SEARCH_FIELDS);
        params.put("age_from", basicAgeFrom.toString());
        params.put("age_to", basicAgeTo.toString());
        params.put("country_id", VkDatingAppConstants.COUNTRY_ID.toString());
        params.put("sex", VkDatingAppConstants.SEX.toString());
    }
}
