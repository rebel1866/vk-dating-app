package com.melnikov.service.logic;

import com.melnikov.service.exception.ServiceException;
import com.melnikov.service.vo.ApiSearchRequestVo;

public interface NameService {
    ApiSearchRequestVo getRequestVo() throws ServiceException;
}
