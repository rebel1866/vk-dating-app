package com.melnikov.service.logic.impl;

import com.melnikov.dao.model.Name;
import com.melnikov.dao.repository.NameRepository;
import com.melnikov.service.dto.StatisticNames;
import com.melnikov.service.exception.ServiceException;
import com.melnikov.service.logic.NameService;
import com.melnikov.service.vo.ApiSearchRequestVo;
import com.melnikov.service.vo.Zodiac;
import static com.melnikov.util.Random.getRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class NameServiceImpl implements NameService {
    @Value("${zodiacs}")
    private String zodiacs;

    private Map<String, List<String>> vacantZodiacDates;

    private List<Zodiac> zodiacList;

    private final Logger logger = LoggerFactory.getLogger(NameServiceImpl.class);

    private final List<String> allBirthDates = new ArrayList<>(366);

    private NameRepository nameRepository;

    @Autowired
    public NameServiceImpl(NameRepository nameRepository) {
        this.nameRepository = nameRepository;
        initConstants();
    }

    private void initZodiacs() {
        zodiacList = new ArrayList<>();
        Arrays.stream(zodiacs.split(",")).map(String::trim).forEach(zodiac -> {
            Zodiac zodiacObj = Zodiac.valueOf(zodiac.toUpperCase());
            zodiacList.add(zodiacObj);
        });
        vacantZodiacDates = new HashMap<>();
        List<Name> nameList = nameRepository.findByIsUsed(false);
        for (Name name : nameList) {
            vacantZodiacDates.put(name.getName(), getVacantZodiacDatesForName(name));
        }
    }

    public StatisticNames getStatisticForNamesNames() {
        List<Name> allNames = nameRepository.findAll();
        int allBirthDates = allNames.size() * 365;
        int usedBirthDates = 0;
        for (Name name : allNames) {
            usedBirthDates = usedBirthDates + name.getBirthDates().size();
        }
        return new StatisticNames(usedBirthDates / allBirthDates);
    }
    private List<String> getVacantZodiacDatesForName(Name name) {
        List<String> birthDatesForName = name.getBirthDates();
        List<String> vacantZodiacDatesForName = new ArrayList<>();
        List<String> allBirthDatesClone = new ArrayList<>(allBirthDates);
        allBirthDatesClone.removeAll(birthDatesForName);
        for (Zodiac zodiac : zodiacList) {
            List<String> temp = new ArrayList<>();
            LocalDate startDate = toLocalDate(zodiac.getStartDate());
            LocalDate endDate = toLocalDate(zodiac.getEndDate());
            for (String date : allBirthDatesClone) {
              LocalDate current  = toLocalDate(date);
                if ((startDate.isBefore(current) || startDate.equals(current)) && (endDate.isAfter(current)|| endDate.equals(current))) {
                    temp.add(date);
                }
            }
            vacantZodiacDatesForName.addAll(temp);
        }
        return vacantZodiacDatesForName;
    }

    private LocalDate toLocalDate(String date) {
        String[] arr = date.split("\\.");
        return LocalDate.of(2000, Integer.parseInt(arr[1]), Integer.parseInt(arr[0]));
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
        if (isPreferredZodiacsPresent()) {
            try {
                return getRequestVoByPreferredZodiacs();
            } catch (ServiceException e) {
                logger.error("Cannot generate requestvo by preferred zodiacs. " + e.getMessage());
                throw new ServiceException("Cannot generate requestvo by preferred zodiacs. " + e.getMessage());
            }
        }
        List<Name> nameList = nameRepository.findByIsUsed(false);
        if (nameList.size() == 0) {
            throw new ServiceException("All names have been used");
        }
        int random = (int) getRandom(0, nameList.size() - 1);
        Name name = nameList.get(random);
        List<String> nameBirthDates = name.getBirthDates();
        if (nameBirthDates.size() == allBirthDates.size()) {
            name.setIsUsed(true);
            nameRepository.save(name);
            return getRequestVo();
        }
        List<String> birthDates = new ArrayList<>(allBirthDates);
        birthDates.removeAll(nameBirthDates);
        int random2 = (int) getRandom(0, birthDates.size() - 1);
        String currentBirthDate = birthDates.get(random2);
        String[] dayMonth = currentBirthDate.split("\\.");
        name.getBirthDates().add(currentBirthDate);
        nameRepository.save(name);
        return new ApiSearchRequestVo(name.getName(), Byte.valueOf(dayMonth[0]), Byte.valueOf(dayMonth[1]));
    }

    private ApiSearchRequestVo getRequestVoByPreferredZodiacs() throws ServiceException {
        int count = 0;
        int random = (int) getRandom(0, vacantZodiacDates.size() - 1);
        Map.Entry<String, List<String>> entry = null;
        Iterator<Map.Entry<String, List<String>>> iterator = vacantZodiacDates.entrySet().iterator();
        while (iterator.hasNext()) {
            if (count == random) {
                entry = iterator.next();
                break;
            }
            iterator.next();
            count++;
        }
        if (entry == null) {
            throw new ServiceException("Entry is null");
        }
        List<String> dates = entry.getValue();
        int random2 = (int) getRandom(0, dates.size() - 1);
        String birthDay = dates.get(random2);
        String [] arr = birthDay.split("\\.");
        List<String> nameBDates = vacantZodiacDates.get(entry.getKey());
        nameBDates.remove(birthDay);
        if (nameBDates.isEmpty()) {
            vacantZodiacDates.remove(entry.getKey());
        }
        Name name = nameRepository.findByName(entry.getKey());
        name.getBirthDates().add(birthDay);
        nameRepository.save(name);
        return new ApiSearchRequestVo(entry.getKey(), Byte.valueOf(arr[0]), Byte.valueOf(arr[1]));
    }

    private boolean isPreferredZodiacsPresent() {
        if (zodiacList == null) {
            initZodiacs();
        }
        return !vacantZodiacDates.isEmpty();
    }
}
