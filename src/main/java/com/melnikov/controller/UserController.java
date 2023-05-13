package com.melnikov.controller;

import com.melnikov.service.dto.UserDto;
import com.melnikov.service.exception.ServiceException;
import com.melnikov.service.logic.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
    private List<UserDto> getUsers( @RequestParam(required = false, defaultValue = "100")
    int amount, @RequestParam(required = false) String city, @RequestParam(required = false) Integer ageFrom,
            @RequestParam(required = false) Integer ageTo, @RequestParam(required = false) String name) {
        try {
            return userService.getUsers(amount, city, ageFrom, ageTo, name);
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/startSearching")
    private Map<String, String> startSearching(@RequestParam(required = false) Integer amount) {
        Map<String, String> response = new HashMap<>();
        userService.startIndexing(amount);
        response.put("response", "Search task has been successfully started");
        return response;
    }
    @GetMapping("/stopSearching")
    private Map<String, String> stopSearching() {
        Map<String, String> response = new HashMap<>();
        userService.stopIndexing();
        response.put("response", "Search task has been successfully stopped");
        return response;
    }
}
