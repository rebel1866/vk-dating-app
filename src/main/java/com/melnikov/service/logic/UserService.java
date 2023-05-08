package com.melnikov.service.logic;

import com.melnikov.service.dto.UserDto;
import com.melnikov.service.exception.ServiceException;

import java.util.List;

public interface UserService {

    List<UserDto> getUsers(String accessToken, int amount, String city, Integer ageFrom, Integer ageTo) throws ServiceException;
}
