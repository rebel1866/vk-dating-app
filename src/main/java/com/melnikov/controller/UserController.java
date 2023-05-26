package com.melnikov.controller;

import com.melnikov.controller.exception.ControllerException;
import com.melnikov.service.dto.UserDto;
import com.melnikov.service.exception.ServiceException;
import com.melnikov.service.logic.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController()
@RequestMapping("/users")
public class UserController {
    private UserService userService;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    private List<UserDto> getUsers(@RequestParam(required = false, defaultValue = "100")
                                   int amount, @RequestParam(required = false) String city, @RequestParam(required = false) Integer ageFrom,
                                   @RequestParam(required = false) Integer ageTo, @RequestParam(required = false) String name)
            throws ControllerException {
        try {
            return userService.getUsers(amount, city, ageFrom, ageTo, name);
        } catch (ServiceException e) {
            throw new ControllerException(e.getMessage());
        }
    }

    @GetMapping("/startSearching")
    private Map<String, String> startSearching(@RequestParam(required = false) Integer amount) {
        userService.startIndexing(amount);
        return Collections.singletonMap("response", "Search task has been successfully started");
    }

    @GetMapping("/stopSearching")
    private Map<String, String> stopSearching() {
        userService.stopIndexing();
        return Collections.singletonMap("response", "Search task has been successfully stopped");
    }

    @GetMapping("/setViewed/{id}")
    private Map<String, String> setViewed(@PathVariable Long id) throws ControllerException {
        try {
            userService.updateUserByParams(id, Collections.singletonMap("hasBeenViewed", true));
        } catch (ServiceException e) {
            throw new ControllerException(e.getMessage());
        }
        return Collections.singletonMap("response", "success");
    }

    @GetMapping("/setAppFavorite/{id}")
    private Map<String, String> setAppFavorite(@PathVariable Long id) throws ControllerException {
        try {
            userService.updateUserByParams(id, Collections.singletonMap("isApplicationFavorite", true));
        } catch (ServiceException e) {
            throw new ControllerException(e.getMessage());
        }
        return Collections.singletonMap("response", "success");
    }

    @PostMapping("/send/{id}")
    private Map<String, String> sendMessage(@RequestParam String message, @RequestParam String token, @PathVariable Long id)
            throws ControllerException {
        try {
            userService.sendMessage(message, token, id);
        } catch (ServiceException e) {
            throw new ControllerException(e.getMessage());
        }
        return Collections.singletonMap("response", "success");
    }

    @PostMapping("/send/{id}/phrases")
    private Map<String, String> sendRandomPhrase(@RequestParam String token, @PathVariable Long id)
            throws ControllerException {
        try {
            userService.sendRandomPhrase(token, id);
        } catch (ServiceException e) {
            throw new ControllerException(e.getMessage());
        }
        return Collections.singletonMap("response", "success");
    }
    @PostMapping("/send/{id}/phrases/{phraseId}")
    private Map<String, String> sendPhraseById(@RequestParam String token, @PathVariable Long id, @PathVariable Integer phraseId)
            throws ControllerException {
        try {
            userService.sendPhraseById(token, id, phraseId);
        } catch (ServiceException e) {
            throw new ControllerException(e.getMessage());
        }
        return Collections.singletonMap("response", "success");
    }

    @PostMapping("/addFriend/{id}")
    private Map<String, String> addFriend(@RequestParam(required = false) String message, @RequestParam String token, @PathVariable Long id)
            throws ControllerException {
        try {
            userService.addFriend(message, token, id);
        } catch (ServiceException e) {
            throw new ControllerException(e.getMessage());
        }
        return Collections.singletonMap("response", "success");
    }

    @PostMapping("/checkTokenValid")
    private Map<String, String> checkTokenValid(@RequestParam String token) {
        boolean isTokenValid = userService.checkTokenValid(token);
        if (isTokenValid) {
            return Collections.singletonMap("response", "valid");
        }
        return Collections.singletonMap("response", "not_valid");
    }
}
