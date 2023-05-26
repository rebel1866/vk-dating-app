package com.melnikov.service.logic;

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

    void sendMessage(String message, String token, Long id) throws ServiceException;

    void addFriend(String message, String token, Long id) throws ServiceException;

    boolean checkTokenValid(String token);

    void sendRandomPhrase(String token, Long id) throws ServiceException;

    void sendPhraseById(String token, Long id, Integer phraseId) throws ServiceException;
}
