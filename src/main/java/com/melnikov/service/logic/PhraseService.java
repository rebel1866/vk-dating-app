package com.melnikov.service.logic;

import com.melnikov.dao.model.Phrase;
import com.melnikov.service.exception.ServiceException;

public interface PhraseService {
    Phrase getRandomPhrase() throws ServiceException;

    Phrase getPhraseById(Integer phraseId) throws ServiceException;
}
