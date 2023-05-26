package com.melnikov.service.logic.impl;

import com.melnikov.dao.model.Phrase;
import com.melnikov.dao.repository.PhraseRepository;
import com.melnikov.service.exception.ServiceException;
import com.melnikov.service.logic.PhraseService;
import com.melnikov.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PhraseServiceImpl implements PhraseService {
    private PhraseRepository phraseRepository;

    @Autowired
    public void setPhraseRepository(PhraseRepository phraseRepository) {
        this.phraseRepository = phraseRepository;
    }

    @Override
    public Phrase getRandomPhrase() throws ServiceException {
        List<Phrase> allPhrases = phraseRepository.findAll();
        if (allPhrases.size() == 0) {
            throw new ServiceException("No phrases found");
        }
        long index = Random.getRandom(0, allPhrases.size() - 1);
        return allPhrases.get((int) index);
    }

    @Override
    public Phrase getPhraseById(Integer phraseId) throws ServiceException{
        return phraseRepository.findById(phraseId).orElseThrow(()-> new ServiceException("No phrase found by id"));
    }
    //addPhrase
    //remove
}
