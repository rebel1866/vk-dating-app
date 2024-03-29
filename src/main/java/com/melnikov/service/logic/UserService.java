package com.melnikov.service.logic;

import com.melnikov.service.dto.Statistic;
import com.melnikov.service.dto.UserDto;
import com.melnikov.service.exception.ServiceException;

import java.util.List;
import java.util.Map;

public interface UserService {

    List<UserDto> getUsers(int amount, String city, Integer ageFrom, Integer ageTo, String name)
            throws ServiceException;
    void startIndexing(Integer amount);

    void stopIndexing();

    void updateUserByParams(Long id, Map<String, Object> params) throws ServiceException;

    void startAmountChecking();
    boolean checkTokenValid(String token);

    List<UserDto> getFavorites() throws ServiceException;


    void addVkFavorite(Long id) throws ServiceException;

    Statistic getStatistic();
}
