package com.melnikov.service.logic.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.melnikov.dao.model.Match;
import com.melnikov.dao.model.Recognize;
import com.melnikov.dao.model.User;
import com.melnikov.dao.model.UserAppearance;
import com.melnikov.dao.repository.RecognizeRepository;
import com.melnikov.dao.repository.UserRepository;
import com.melnikov.service.constant.VkDatingAppConstants;
import com.melnikov.service.exception.ServiceException;
import com.melnikov.service.logic.UserFaceService;
import com.melnikov.service.vo.HttpResponseVo;
import com.melnikov.service.vo.betafaceapi.*;
import com.melnikov.util.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
    private final RecognizeRepository recognizeRepository;

    private final Logger logger = LoggerFactory.getLogger(UserFaceServiceImpl.class);

    private final JsonParser<ImageUploadRequest> jsonUploadRequestParser;
    private final JsonParser<ImageUploadResponseWrapper> jsonUploadResponseParser;
    private final JsonParser<RecognizeRequestVo> jsonRecognizeRequestParser;
    private final JsonParser<RecognizeResponseVo> jsonRecognizeResponseParser;

    @Autowired
    public UserFaceServiceImpl(UserRepository userRepository, RecognizeRepository recognizeRepository,
                               JsonParser<ImageUploadRequest> jsonUploadRequestParser,
                               JsonParser<ImageUploadResponseWrapper> jsonUploadResponseParser,
                               JsonParser<RecognizeRequestVo> jsonRecognizeRequestParser,
                               JsonParser<RecognizeResponseVo> jsonRecognizeResponseParser) {
        this.userRepository = userRepository;
        this.recognizeRepository = recognizeRepository;
        this.jsonUploadRequestParser = jsonUploadRequestParser;
        this.jsonUploadResponseParser = jsonUploadResponseParser;
        this.jsonRecognizeRequestParser = jsonRecognizeRequestParser;
        this.jsonRecognizeResponseParser = jsonRecognizeResponseParser;
    }

    @Override
    @Scheduled(fixedDelayString = "${fixedFaceDelay.in.milliseconds}", initialDelay = 2000)
    public void startFaceIndexing() {
        if (!isFaceIndexingEnabled) {
            return;
        }
        logger.info("Start face indexing");
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
                return;
            }
            if (faces == null || faces.size() == 0) {
                logger.info("Faces is empty");
                user.setUserAppearance(new UserAppearance());
                userRepository.save(user);
                continue;
            }
            boolean isManPresent = isManPresentFilter(faces, user);
            if (isManPresent) {
                logger.info("man detected on photo - continue iterating");
                user.setUserAppearance(new UserAppearance());
                userRepository.save(user);
                continue;
            }
            UserAppearance userAppearance = new UserAppearance();
            user.setUserAppearance(userAppearance);
            Face firstFace = faces.get(0);
            TagVo attractiveTag = getTagByName("attractive", firstFace.getTags());
            boolean isAttractive = getBoolean(attractiveTag.getValue());
            Double confidence = attractiveTag.getConfidence();
            userAppearance.setIsAttractive(isAttractive);
            if (isAttractive) {
                userAppearance.setAttractivenessConfidence(confidence);
            } else {
                userAppearance.setAttractivenessConfidence(1.0 - confidence);
            }
            TagVo blondTag = getTagByName("blond hair", firstFace.getTags());
            userAppearance.setIsBlond(getBoolean(blondTag.getValue()));
//            try {
//                recognize(userAppearance, firstFace.getFaceUid());
//            } catch (ServiceException e) {
//                logger.info("Error while recognizing: " + e.getMessage());
//            }
            userRepository.save(user);
            logger.info(String.format("Saved user face scanning result (user id %s): %s", user.getId(), userAppearance));
        }
    }

    private void recognize(UserAppearance userAppearance, String faceUid) throws ServiceException {
        RecognizeRequestVo requestVo = new RecognizeRequestVo(VkDatingAppConstants.PUBLIC_BETAFACE_API_KEY);
        requestVo.setFacesUids(Collections.singletonList(faceUid));
        List<Recognize> allRecognize = recognizeRepository.findAll();
        List<String> targets = new ArrayList<>();
        allRecognize.forEach(el -> targets.add(el.getNameSpace()));
        requestVo.setTargets(targets);
        RecognizeResponseVo responseVo = sendRecognizeRequest(requestVo);
        List<Match> matches = new ArrayList<>();
        userAppearance.setMatches(matches);
        responseVo.getResults().get(0).getMatches().forEach(matchVo -> {
            Match match = new Match();
            match.setIsMatch(matchVo.getIsMatch());
            match.setMatchConfidence(matchVo.getConfidence());
            match.setTargetNameSpace(matchVo.getPersonId());
            matches.add(match);
        });
        userAppearance.setHighestMatchRate(findHighestMatchRate(matches));
    }

    private Double findHighestMatchRate(List<Match> matches) {
        List<Double> result = new ArrayList<>(matches.stream().filter(el -> el.getIsMatch().equals(true)).map(Match::getMatchConfidence).toList());
        if (result.isEmpty()) {
            return 0.0;
        }
        Collections.sort(result);
        return result.get(result.size() - 1);
    }

    private RecognizeResponseVo sendRecognizeRequest(RecognizeRequestVo requestVo) throws ServiceException {
        String json;
        HttpResponseVo responseVo;
        RecognizeResponseVo response;
        try {
            json = jsonRecognizeRequestParser.marshallJson(requestVo);
        } catch (JsonProcessingException e) {
            throw new ServiceException("Error while marshalling json (recognize)");
        }
        try {
            responseVo = sendJsonPost("https://www.betafaceapi.com/api/v2/recognize", json);
        } catch (IOException e) {
            throw new ServiceException("Error while recognize request");
        }
        try {
            response = jsonRecognizeResponseParser.parseJson(responseVo.getBody(), new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new ServiceException("Error while parsing recognize response");
        }
        return response;
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
        return tags.stream().filter(el -> el.getName().equals(name)).findFirst().orElse(null);
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
        if (responseWrapper.getMedia() == null) {
            throw new ServiceException("Media is null. Response: " + responseVo.getBody());
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
