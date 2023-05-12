package com.melnikov.service.logic.impl;

import com.melnikov.service.exception.ServiceException;
import com.melnikov.service.logic.NameService;
import com.melnikov.service.vo.ApiSearchRequestVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NameServiceImpl implements NameService {

    private final Logger logger = LoggerFactory.getLogger(NameServiceImpl.class);

    int count = 0;

    @Override
    public ApiSearchRequestVo getRequestVo() throws ServiceException {
        count++;
        switch (count) {
            case 1 -> {
                logger.info("marina");
                return new ApiSearchRequestVo("Марина", (byte) 8, (byte) 9);
            }
            case 2 -> {
                logger.info("sasha");
                return new ApiSearchRequestVo("Саша", (byte) 4, (byte) 7);
            }
            case 3 -> {
                logger.info("masha");
                return new ApiSearchRequestVo("Маша", (byte) 2, (byte) 5);
            }
            case 4 -> {
                logger.info("alina");
                return new ApiSearchRequestVo("Алина", (byte) 8, (byte) 12);
            }
            case 5 -> {
                logger.info("polina");
                return new ApiSearchRequestVo("Полина", (byte) 1, (byte) 5);
            }
            default -> {
                return new ApiSearchRequestVo("Анна", (byte) 8, (byte) 1);
            }
        }
    }
}
