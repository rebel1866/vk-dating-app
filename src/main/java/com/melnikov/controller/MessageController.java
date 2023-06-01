package com.melnikov.controller;

import com.melnikov.controller.exception.ControllerException;
import com.melnikov.service.exception.ServiceException;
import com.melnikov.service.logic.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;
@RestController
@RequestMapping("/messages")
public class MessageController {

    private MessageService messageService;

    @Autowired
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/send/{id}")
    private Map<String, String> sendMessage(@RequestParam String message, @RequestParam String token, @PathVariable Long id)
            throws ControllerException {
        try {
            messageService.sendMessage(message, token, id);
        } catch (ServiceException e) {
            throw new ControllerException(e.getMessage());
        }
        return Collections.singletonMap("response", "success");
    }

    @PostMapping("/send/{id}/phrases")
    private Map<String, String> sendRandomPhrase(@RequestParam String token, @PathVariable Long id)
            throws ControllerException {
        try {
            messageService.sendRandomPhrase(token, id);
        } catch (ServiceException e) {
            throw new ControllerException(e.getMessage());
        }
        return Collections.singletonMap("response", "success");
    }

    @PostMapping("/send/{id}/phrases/{phraseId}")
    private Map<String, String> sendPhraseById(@RequestParam String token, @PathVariable Long id, @PathVariable Integer phraseId)
            throws ControllerException {
        try {
            messageService.sendPhraseById(token, id, phraseId);
        } catch (ServiceException e) {
            throw new ControllerException(e.getMessage());
        }
        return Collections.singletonMap("response", "success");
    }

    @PostMapping("/addFriend/{id}")
    private Map<String, String> addFriend(@RequestParam(required = false) String message, @RequestParam String token, @PathVariable Long id)
            throws ControllerException {
        try {
            messageService.addFriend(message, token, id);
        } catch (ServiceException e) {
            throw new ControllerException(e.getMessage());
        }
        return Collections.singletonMap("response", "success");
    }
    @DeleteMapping("phrases/{id}")
    private Map<String, String> removePhrase(@PathVariable Integer id) throws ControllerException {
        try {
            messageService.removePhrase(id);
        } catch (ServiceException e) {
            throw new ControllerException(e.getMessage());
        }
        return Collections.singletonMap("response", "success");
    }

    @PostMapping("phrases")
    private Map<String,String> addPhrase(@RequestParam String phraseText) throws ControllerException {
        try {
            messageService.addPhrase(phraseText);
        } catch (ServiceException e) {
            throw new ControllerException(e.getMessage());
        }
        return Collections.singletonMap("response", "success");
    }

}
