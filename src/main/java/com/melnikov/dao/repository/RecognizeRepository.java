package com.melnikov.dao.repository;

import com.melnikov.dao.model.Recognize;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RecognizeRepository extends MongoRepository<Recognize,Long> {
}
