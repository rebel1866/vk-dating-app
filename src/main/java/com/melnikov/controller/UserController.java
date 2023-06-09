package com.melnikov.controller;

import com.melnikov.controller.exception.ControllerException;
import com.melnikov.service.dto.UserDto;
import com.melnikov.service.exception.ServiceException;
import com.melnikov.service.logic.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
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

    @GetMapping("/favorites")
    private List<UserDto> getFavorites() throws ControllerException {
        try {
            return userService.getFavorites();
        } catch (ServiceException e) {
            throw new ControllerException(e.getMessage());
        }
    }

    @GetMapping("/vkFavorites/{id}")
    private Map<String, String> addVkFavorite(@PathVariable Long id) throws ControllerException {
        try {
            userService.addVkFavorite(id);
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

    // TODO: 9.06.23 getMatches
}
