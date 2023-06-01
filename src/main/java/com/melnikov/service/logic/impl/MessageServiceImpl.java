package com.melnikov.service.logic.impl;

import com.melnikov.dao.model.Phrase;
import com.melnikov.dao.repository.PhraseRepository;
import com.melnikov.service.constant.VkDatingAppConstants;
import com.melnikov.service.exception.ServiceException;
import com.melnikov.service.logic.MessageService;
import com.melnikov.util.HttpClient;
import com.melnikov.util.JsonParser;
import com.melnikov.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MessageServiceImpl implements MessageService {
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
    public Phrase getPhraseById(Integer phraseId) throws ServiceException {
        return phraseRepository.findById(phraseId).orElseThrow(() -> new ServiceException("No phrase found by id"));
    }

    @Override
    public void addPhrase(String phraseText) throws ServiceException {
        Phrase phrase = new Phrase();
        Phrase lastPhrase = phraseRepository.findFirstByOrderByIdDesc();
        phrase.setPhraseText(phraseText);
        phrase.setId(lastPhrase == null ? 1 : lastPhrase.getId());
        phraseRepository.save(phrase);
    }

    @Override
    public void removePhrase(Integer id) throws ServiceException {
        phraseRepository.deleteById(id);
    }

    @Override
    public void sendMessage(String message, String token, Long id) throws ServiceException {
        Map<String, String> params = new HashMap<>();
        params.put("v", VkDatingAppConstants.API_VERSION);
        params.put("access_token", token);
        params.put("user_id", id.toString());
        params.put("message", message);
        params.put("random_id", String.valueOf(Random.getRandom(1000L, 1000000000000000000L)));
        String response;
        try {
            response = HttpClient.sendPOST("https://api.vk.com/method/messages.send", params);
        } catch (IOException e) {
            throw new ServiceException("Could not send message to user with id: " + id);
        }
        handleError(response, "Error sending message. Reason unknown. User id: ", id);
    }

    @Override
    public void addFriend(String message, String token, Long id) throws ServiceException {
        Map<String, String> params = new HashMap<>();
        params.put("v", VkDatingAppConstants.API_VERSION);
        params.put("access_token", token);
        params.put("user_id", id.toString());
        if (message != null) {
            params.put("text", message);
        }
        String response;
        try {
            response = HttpClient.sendPOST("https://api.vk.com/method/friends.add", params);
        } catch (IOException e) {
            throw new ServiceException("Could not send message to user with id: " + id);
        }
        handleError(response, "Error adding friend. Reason unknown. User id: ", id);
    }

    private void handleError(String response, String message, Long id) throws ServiceException {
        if (response.contains("error")) {
            String eMessage;
            try {
                eMessage = JsonParser.getValue(response, "error_msg");
            } catch (IOException e) {
                throw new ServiceException(message + id);
            }
            throw new ServiceException(eMessage + " User id: " + id);
        }
    }

    @Override
    public void sendPhraseById(String token, Long id, Integer phraseId) throws ServiceException {
        Phrase phrase = getPhraseById(phraseId);
        sendMessage(phrase.getPhraseText(), token, id);
    }

    @Override
    public void sendRandomPhrase(String token, Long id) throws ServiceException {
        Phrase phrase = getRandomPhrase();
        sendMessage(phrase.getPhraseText(), token, id);
    }
}
