package com.melnikov.service.logic;

import com.melnikov.dao.model.Phrase;
import com.melnikov.service.exception.ServiceException;

public interface MessageService {
    Phrase getRandomPhrase() throws ServiceException;

    Phrase getPhraseById(Integer phraseId) throws ServiceException;

    void sendMessage(String message, String token, Long id) throws ServiceException;

    void addFriend(String message, String token, Long id) throws ServiceException;

    void sendPhraseById(String token, Long id, Integer phraseId) throws ServiceException;

    void sendRandomPhrase(String token, Long id) throws ServiceException;

    void removePhrase(Integer id) throws ServiceException;

    void addPhrase(String phraseText) throws ServiceException;
}
