package com.melnikov.dao.repository.impl;

import com.melnikov.dao.model.User;
import com.melnikov.dao.repository.UserCustomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserCustomRepositoryImpl implements UserCustomRepository {

    private MongoTemplate mongoTemplate;

    @Autowired
    public void setMongoTemplate(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<User> findUsersByParams(int page, int pageSize, String city, Integer ageFrom, Integer ageTo, String name) {
        Query query = new Query();
        if (city != null) {
            query.addCriteria(Criteria.where("cityName").regex(city, "i"));
        }
        if (ageFrom != null && ageTo != null) {
            query.addCriteria(Criteria.where("age").gte(ageFrom).lte(ageTo));
        } else {
            if (ageTo != null) {
                query.addCriteria(Criteria.where("age").lte(ageTo));
            }
            if (ageFrom != null) {
                query.addCriteria(Criteria.where("age").gte(ageFrom));
            }
        }
        if (name != null) {
            query.addCriteria(Criteria.where("firstName").regex(name, "i"));
        }
        Pageable pageableRequest = PageRequest.of(page, pageSize);
        query.with(pageableRequest);
        query.with(Sort.by(Sort.Direction.DESC, "id"));
        query.addCriteria(Criteria.where("hasBeenViewed").is(false));
        return mongoTemplate.find(query, User.class);
    }
}
