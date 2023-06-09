package com.melnikov.dao.repository;

import com.melnikov.dao.model.User;
import com.melnikov.dao.model.UserAppearance;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserRepository extends MongoRepository<User,Long> {
       int countByCityNameIgnoreCaseAndHasBeenViewed(String cityName, Boolean hasBeenViewed);
       List<User> findByIsApplicationFavorite(Boolean isApplicationFavorite);
       User findFirstByCityNameIgnoreCaseAndHasBeenViewedAndUserAppearance(String cityName, Boolean hasBeenViewed, UserAppearance userAppearance);
       User findFirstByHasBeenViewedAndUserAppearance(Boolean hasBeenViewed, UserAppearance userAppearance);

}
