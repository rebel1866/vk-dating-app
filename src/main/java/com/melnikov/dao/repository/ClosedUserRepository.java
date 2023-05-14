package com.melnikov.dao.repository;

import com.melnikov.dao.model.ClosedUser;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ClosedUserRepository extends MongoRepository<ClosedUser, Long> {
}
