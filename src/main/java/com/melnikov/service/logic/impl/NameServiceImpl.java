package com.melnikov.service.logic.impl;

import com.melnikov.dao.model.Name;
import com.melnikov.dao.repository.NameRepository;
import com.melnikov.service.exception.ServiceException;
import com.melnikov.service.logic.NameService;
import com.melnikov.service.vo.ApiSearchRequestVo;
import com.melnikov.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NameServiceImpl implements NameService {

    private final Logger logger = LoggerFactory.getLogger(NameServiceImpl.class);

    private final List<String> allBirthDates = new ArrayList<>(366);

    private NameRepository nameRepository;

    @Autowired
    public NameServiceImpl(NameRepository nameRepository) {
        this.nameRepository = nameRepository;
        initConstants();
    }

    private void initConstants() {
        for (int i = 1; i <= 12; i++) {
            int amountDays;
            if (i == 2) {
                amountDays = 29;
            } else if (i < 8 && i % 2 == 0) {
                amountDays = 30;
            } else if (i < 8) {
                amountDays = 31;
            } else if (i % 2 == 0) {
                amountDays = 31;
            } else {
                amountDays = 30;
            }
            for (int j = 1; j <= amountDays; j++) {
                String birthDate = String.format("%s.%s", j, i);
                allBirthDates.add(birthDate);
            }
        }
    }

    @Override
    public ApiSearchRequestVo getRequestVo() throws ServiceException {
        List<Name> nameList = nameRepository.findByIsUsed(false);
        if (nameList.size() == 0) {
            throw new ServiceException("All names have been used");
        }
        int random = (int) Random.getRandom(0, nameList.size() - 1);
        Name name = nameList.get(random);
        List<String> nameBirthDates = name.getBirthDates();
        if (nameBirthDates.size() == allBirthDates.size()) {
            name.setIsUsed(true);
            nameRepository.save(name);
            return getRequestVo();
        }
        List<String> birthDates = new ArrayList<>(allBirthDates);
        birthDates.removeAll(nameBirthDates);
        int random2 = (int) Random.getRandom(0, birthDates.size() - 1);
        String currentBirthDate = birthDates.get(random2);
        String[] dayMonth = currentBirthDate.split("\\.");
        name.getBirthDates().add(currentBirthDate);
        nameRepository.save(name);
        return new ApiSearchRequestVo(name.getName(), Byte.valueOf(dayMonth[0]), Byte.valueOf(dayMonth[1]));
    }
}
