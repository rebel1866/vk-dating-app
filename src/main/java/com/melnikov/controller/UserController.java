package com.melnikov.controller;

import com.melnikov.service.dto.UserDto;
import com.melnikov.service.exception.ServiceException;
import com.melnikov.service.logic.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController()
@RequestMapping("/users")
public class UserController {
    private UserService userService;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    private List<UserDto> getUsers(
            @RequestParam String accessToken, @RequestParam(required = false, defaultValue = "100")
    int amount, @RequestParam(required = false) String city, @RequestParam(required = false) Integer ageFrom,
            @RequestParam(required = false) Integer ageTo) {
        try {
            return userService.getUsers(accessToken, amount, city, ageFrom, ageTo);
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        }
    }
}
