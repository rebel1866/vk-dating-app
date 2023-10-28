package com.melnikov.util.converter;

import com.melnikov.dao.model.Photo;
import com.melnikov.dao.model.User;
import com.melnikov.service.dto.UserDto;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UserModelToDtoConverter {
    public static UserDto convert(User user) {

        List<String> careersList = new ArrayList<>();
        if (user.getUserDescription().getCareers() != null) {
            careersList = user.getUserDescription().getCareers().stream().map(career ->
                    String.format("%s (%s)", career.getCompany(), career.getPosition())).collect(Collectors.toList());
        }
        String attractiveness = "No data";
        if(user.getUserAppearance() != null){
            if(user.getUserAppearance().getIsAttractive() != null && user.getUserAppearance().getAttractivenessConfidence() != null){
                attractiveness = getIsAttractive(user.getUserAppearance().getIsAttractive()) + user.getUserAppearance().
                        getAttractivenessConfidence();
            }
        }
        List<String> photos = user.getPhotos().stream().map(Photo::getUrl).collect(Collectors.toList());
        return UserDto.builder().id(user.getId()).bdate(user.getBdate()).cityName(user.getCityName()).
                firstName(user.getFirstName()).lastName(user.getLastName()).age(user.getAge()).isFriend(user.getIsFriend()).
                canWritePrivateMessage(user.getCanWritePrivateMessage()).canSendFriendRequest(user.getCanSendFriendRequest()).
                isVkFavorite(user.getIsVkFavorite()).isApplicationFavorite(user.getIsApplicationFavorite()).
                interests(user.getUserDescription().getBooks()).interests(user.getUserDescription().getInterests()).
                quotes(user.getUserDescription().getQuotes()).about(user.getUserDescription().getAbout()).
                movies(user.getUserDescription().getMovies()).activities(user.getUserDescription().getActivities()).
                music(user.getUserDescription().getMusic()).mobilePhone(user.getUserDescription().getMobilePhone()).
                universityName(user.getUserDescription().getUniversityName()).facultyName(user.getUserDescription().getFacultyName()).
                peopleMain(user.getUserDescription().getPeopleMain()).political(user.getUserDescription().getPolitical()).
                smoking(user.getUserDescription().getSmoking()).religion(user.getUserDescription().getReligion()).
                alcohol(user.getUserDescription().getAlcohol()).inspiredBy(user.getUserDescription().getInspiredBy()).
                lifeMain(user.getUserDescription().getLifeMain()).relation(user.getUserDescription().getRelation()).
                career(careersList).photos(photos).friendsAmount(user.getFriendsAmount()).attractiveness(attractiveness).build();
    }

    private static String getIsAttractive(Boolean isAttractive) {
        return isAttractive ? "attractive ": "not attractive ";
    }
}
