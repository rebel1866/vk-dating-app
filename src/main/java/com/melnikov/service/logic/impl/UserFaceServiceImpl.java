package com.melnikov.service.logic.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.melnikov.dao.model.User;
import com.melnikov.dao.model.UserAppearance;
import com.melnikov.dao.repository.UserRepository;
import com.melnikov.service.constant.VkDatingAppConstants;
import com.melnikov.service.exception.ServiceException;
import com.melnikov.service.logic.UserFaceService;
import com.melnikov.service.vo.HttpResponseVo;
import com.melnikov.service.vo.betafaceapi.Face;
import com.melnikov.service.vo.betafaceapi.ImageUploadRequest;
import com.melnikov.service.vo.betafaceapi.ImageUploadResponseWrapper;
import com.melnikov.service.vo.betafaceapi.TagVo;
import com.melnikov.util.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static com.melnikov.util.HttpClient.sendJsonPost;


@Service
public class UserFaceServiceImpl implements UserFaceService {

    @Value("${is.face.indexing.enabled}")
    private Boolean isFaceIndexingEnabled;

    @Value("${priority.city.for.face.indexing}")
    private String priorityCity;

    private final UserRepository userRepository;

    private final Logger logger = LoggerFactory.getLogger(UserFaceServiceImpl.class);

    private final JsonParser<ImageUploadRequest> jsonUploadRequestParser;
    private final JsonParser<ImageUploadResponseWrapper> jsonUploadResponseParser;

    @Autowired
    public UserFaceServiceImpl(UserRepository userRepository, JsonParser<ImageUploadRequest> jsonUploadRequestParser,
                               JsonParser<ImageUploadResponseWrapper> jsonUploadResponseParser) {
        this.userRepository = userRepository;
        this.jsonUploadRequestParser = jsonUploadRequestParser;
        this.jsonUploadResponseParser = jsonUploadResponseParser;
    }

    @Override
    @Scheduled(fixedDelayString = "${fixedFaceDelay.in.milliseconds}", initialDelay = 20000)
    public void startFaceIndexing() {
        if (!isFaceIndexingEnabled) {
            return;
        }
        int iteration = -1;
        while (true) {
            iteration++;
            User user = findUser();
            if (user == null) {
                logger.info("No user found for face scanning.");
                return;
            }
            ImageUploadRequest imageUploadRequest = new ImageUploadRequest(VkDatingAppConstants.PUBLIC_BETAFACE_API_KEY,
                    VkDatingAppConstants.DETECTION_FLAGS);
            if (user.getPhotos().isEmpty()) {
                return;
            }
            String url = user.getPhotos().get(0).getUrl();
            String fileName = UUID.randomUUID() + ".jpg";
            imageUploadRequest.setFileUri(url);
            imageUploadRequest.setOriginalFileName(fileName);
            List<Face> faces;
            try {
                faces = sendUploadRequest(imageUploadRequest);
            } catch (ServiceException e) {
                logger.info("Exception happened while face scanning, stopping. Iteration: " + iteration);
                logger.info("Exception message: " + e.getMessage());
                break;
            }
            if (faces.isEmpty()) {
                logger.info("Faces is empty");
                return;
            }
            boolean isManPresent = isManPresentFilter(faces, user);
            if (isManPresent) {
                logger.info("man detected on photo - continue iterating");
                continue;
            }
            UserAppearance userAppearance = new UserAppearance();
            user.setUserAppearance(userAppearance);
            Face firstFace = faces.get(0);
            TagVo attractiveTag = getTagByName("attractive", firstFace.getTags());
            boolean isAttractive = getBoolean(attractiveTag.getValue());
            Double confidence = attractiveTag.getConfidence();
            userAppearance.setIsAttractive(isAttractive);
            userAppearance.setAttractivenessConfidence(confidence);
            TagVo blondTag = getTagByName("blond hair", firstFace.getTags());
            userAppearance.setIsBlond(getBoolean(blondTag.getValue()));
            recognize(userAppearance);
            userRepository.save(user);
        }
    }

    private void recognize(UserAppearance userAppearance) {
    }

    private Boolean getBoolean(String value) {
        return value.equals("yes");
    }

    private boolean isManPresentFilter(List<Face> faces, User user) {
        if (faces.size() > 1) {
            for (Face face : faces) {
                TagVo genderTag = getTagByName("gender", face.getTags());
                if (!genderTag.getValue().equals("female")) {
                    userRepository.delete(user);
                    return true;
                }
            }
        }
        return false;
    }

    private TagVo getTagByName(String name, List<TagVo> tags) {
        return tags.stream().filter(el -> el.getName().equals("gender")).findFirst().orElse(null);
    }

    private List<Face> sendUploadRequest(ImageUploadRequest imageUploadRequest) throws ServiceException {
        String json;
        HttpResponseVo responseVo;
        ImageUploadResponseWrapper responseWrapper;
        try {
            json = jsonUploadRequestParser.marshallJson(imageUploadRequest);
        } catch (JsonProcessingException e) {
            throw new ServiceException("Error while marshalling json");
        }
        try {
            responseVo = sendJsonPost("https://www.betafaceapi.com/api/v2/media", json);
        } catch (IOException e) {
            throw new ServiceException("Error while sending upload image request");
        }
        try {
            responseWrapper = jsonUploadResponseParser.parseJson(responseVo.getBody(), new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new ServiceException("Error while parsing upload image response");
        }
        return responseWrapper.getMedia().getFaces();
    }

    private User findUser() {
        User user = userRepository.findFirstByCityNameIgnoreCaseAndHasBeenViewedAndUserAppearance(priorityCity, false, null);
        if (user == null) {
            user = userRepository.findFirstByHasBeenViewedAndUserAppearance(false, null);
            return user;
        }
        return user;
    }
}
