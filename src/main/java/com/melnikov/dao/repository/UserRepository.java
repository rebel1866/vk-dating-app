package com.melnikov.dao.repository;

import com.melnikov.dao.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserRepository extends MongoRepository<User,Long> {
       int countByCityNameIgnoreCaseAndHasBeenViewed(String cityName, Boolean hasBeenViewed);
       List<User> findByIsApplicationFavorite(Boolean isApplicationFavorite);
}
