package com.melnikov.service.logic;

import com.melnikov.service.dto.UserDto;
import com.melnikov.service.exception.ServiceException;

import java.util.List;

public interface UserService {

    List<UserDto> getUsers(int amount, String city, Integer ageFrom, Integer ageTo, String name)
            throws ServiceException;
    void startIndexing(Integer amount);

    void stopIndexing();

    void updateHasBeenViewed(Long id);
}
