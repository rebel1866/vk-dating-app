package com.melnikov.util.converter;

import com.melnikov.dao.model.Career;
import com.melnikov.dao.model.User;
import com.melnikov.dao.model.UserDescription;
import com.melnikov.dao.model.constant.*;
import com.melnikov.service.vo.UserVo;
import com.melnikov.util.DateUtil;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class UserVoToModelConverter {
    public static User convert(UserVo userVo) {
        List<Career> careers = null;
        if (userVo.getCareer() != null) {
            careers = userVo.getCareer().stream().map(el -> new Career(el.getCompany(), el.getPosition())).
                    collect(Collectors.toList());
        }

        UserDescription userDescription = UserDescription.builder().interests(userVo.getInterests()).books(userVo.getBooks()).
                quotes(userVo.getQuotes()).about(userVo.getAbout()).movies(userVo.getMovies()).activities(userVo.getActivities()).
                music(userVo.getMusic()).mobilePhone(userVo.getMobilePhone()).universityName(userVo.getUniversityName()).
                facultyName(userVo.getFacultyName()).peopleMain(getPeopleMainLabel(userVo.getPersonal() == null ?
                        Integer.valueOf(0) : userVo.getPersonal().getPeopleMain()))
                .political(getPoliticalLabel(userVo.getPersonal() == null ? Integer.valueOf(0) : userVo.getPersonal().getPolitical())).
                smoking(getSmokingLabel(userVo.getPersonal() == null ? Integer.valueOf(0) : userVo.getPersonal().getSmoking())).
                religion(userVo.getPersonal() == null ? "" : userVo.getPersonal().getReligion()).
                alcohol(getAlcoholLabel(userVo.getPersonal() == null ? Integer.valueOf(0) : userVo.getPersonal().getAlcohol()))
                .inspiredBy(userVo.getPersonal() == null ? "" : userVo.getPersonal().getInspiredBy()).
                lifeMain(getLifeMainLabel(userVo.getPersonal() == null ? Integer.valueOf(0) : userVo.getPersonal().getLifeMain())).
                relation(getRelationLabel(userVo.getRelation())).careers(careers).build();
        Short age;
        try {
            age = DateUtil.parseAge(userVo.getBdate());
        } catch (DateTimeException e) {
            age = null;
        }
        return User.builder().id(userVo.getId()).bdate(userVo.getBdate()).cityName(userVo.getCity() == null ?
                        "" : userVo.getCity().getTitle()).
                firstName(userVo.getFirstName()).lastName(userVo.getLastName()).age(age).isFriend(userVo.getIsFriend()).
                canWritePrivateMessage(userVo.getCanWritePrivateMessage()).canSendFriendRequest(userVo.getCanSendFriendRequest()).
                savingTime(LocalDateTime.now()).hasBeenViewed(false).isVkFavorite(userVo.getIsFavorite()).isApplicationFavorite(false).
                userDescription(userDescription).build();
    }

    private static String getPeopleMainLabel(Integer number) {
        if (number == null) {
            return "";
        }
        return PeopleMain.valueOf("N" + number).getLabel();
    }

    private static String getPoliticalLabel(Integer number) {
        if (number == null) {
            return "";
        }
        return Political.valueOf("N" + number).getLabel();
    }

    private static String getSmokingLabel(Integer number) {
        if (number == null) {
            return "";
        }
        return Smoking.valueOf("N" + number).getLabel();
    }

    private static String getAlcoholLabel(Integer number) {
        if (number == null) {
            return "";
        }
        return Alcohol.valueOf("N" + number).getLabel();
    }

    private static String getLifeMainLabel(Integer number) {
        if (number == null) {
            return "";
        }
        return LifeMain.valueOf("N" + number).getLabel();
    }

    private static String getRelationLabel(Integer number) {
        if (number == null) {
            return "";
        }
        return Relation.valueOf("N" + number).getLabel();
    }
}
