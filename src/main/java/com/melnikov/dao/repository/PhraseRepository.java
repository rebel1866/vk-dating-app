package com.melnikov.dao.repository;

import com.melnikov.dao.model.Phrase;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PhraseRepository extends MongoRepository<Phrase, Integer> {
    Phrase findFirstByOrderByIdDesc();
}
