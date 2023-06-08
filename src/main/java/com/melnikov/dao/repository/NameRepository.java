package com.melnikov.dao.repository;

import com.melnikov.dao.model.Name;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NameRepository extends MongoRepository<Name, String> {
    List<Name> findByIsUsed(Boolean isUsed);

    Name findByName(String name);
}
