package com.melnikov.dao.repository;

import com.melnikov.dao.model.User;

import java.util.List;

public interface UserCustomRepository {
    List<User> findUsersByParams(int page, int amount, String city, Integer ageFrom, Integer ageTo, String name);
}
