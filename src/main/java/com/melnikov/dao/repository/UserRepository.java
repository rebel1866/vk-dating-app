package com.melnikov.dao.repository;

import com.melnikov.dao.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User,Long> {
}
