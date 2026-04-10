package com.ootd.fitme.domain.notification.service;

import com.ootd.fitme.domain.follow.repository.FollowRepository;
import com.ootd.fitme.domain.notification.dto.request.NotificationDeleteRequest;
import com.ootd.fitme.domain.notification.dto.request.NotificationPageRequest;
import com.ootd.fitme.domain.notification.dto.response.NotificationPageResponse;
import com.ootd.fitme.domain.notification.dto.response.NotificationDto;
import com.ootd.fitme.domain.notification.entity.Notification;
import com.ootd.fitme.domain.notification.entity.NotificationFactory;
import com.ootd.fitme.domain.notification.enums.AttributeAction;
import com.ootd.fitme.domain.notification.event.NotificationCreatedEvent;
import com.ootd.fitme.domain.notification.exception.NotificationBadRequestException;
import com.ootd.fitme.domain.notification.exception.NotificationException;
import com.ootd.fitme.domain.notification.exception.NotificationNotFoundException;
import com.ootd.fitme.domain.notification.mapper.NotificationMapper;
import com.ootd.fitme.domain.notification.repository.NotificationProfileRepository;
import com.ootd.fitme.domain.notification.repository.NotificationRepository;
import com.ootd.fitme.domain.profile.repository.ProfileRepository;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.exception.user.UserException;
import com.ootd.fitme.domain.user.repository.UserRepository;
import com.ootd.fitme.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationFactory notificationFactory;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final ApplicationEventPublisher eventPublisher;




    @Transactional
    public Notification  notifyDirectMessage(UUID receiverId, String senderName,String message) {

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        Notification notification = notificationFactory.dm(receiver, senderName, message);

        return saveAndPublish(notification);
    }

    @Transactional
    public Notification notifyFollowed(UUID followeeId, String followerName) {

        User user = userRepository.findById(followeeId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        Notification notification = notificationFactory.followed(user, followerName);

        return saveAndPublish(notification);
    }

    @Transactional
    public Notification notifyFeedLiked(UUID likedId,String feedName, String likerName) {

        User user = userRepository.findById(likedId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        Notification notification = notificationFactory.feedLiked(user,feedName, likerName);

        return saveAndPublish(notification);
    }

    @Transactional
    public Notification notifyFeedCommented(UUID feedOwnerId,String feedName, String commenterName, String comment) {

        User user = userRepository.findById(feedOwnerId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        Notification notification = notificationFactory.feedCommented(user,feedName, commenterName,comment);

        return saveAndPublish(notification);
    }

    @Transactional
    public List<Notification> notifyWeatherAlert(List<UUID> receiverIds, String region1, String region2 , String weatherAlert) {
        //user id 리스트를  이미 줌
        if (receiverIds == null || receiverIds.isEmpty()) {
            return List.of();
        }

        List<Notification> notifications = receiverIds.stream()
                .distinct()
                .map(userRepository::getReferenceById)
                .map(user -> notificationFactory.weatherAlert(user, region1, region2, weatherAlert))
                .toList();

        return saveAllAndPublish(notifications);
    }

    @Transactional
    public List<Notification> notifyFollowerNewFeed(UUID followeeId, String writerName, String feedName
    ) {

        List<UUID> followerIds = followRepository.findFollowerIdsByFolloweeId(followeeId);

        if (followerIds == null || followerIds.isEmpty()) {
            return List.of();
        }

        List<Notification> notifications = followerIds.stream()
                .distinct()
                .map(userRepository::getReferenceById)
                .map(user -> notificationFactory.followerNewFeed(user, writerName, feedName))
                .toList();

        return saveAllAndPublish(notifications);
    }

    @Transactional
    public List<Notification> notifyAttributeAdded(String attributeName,AttributeAction action) {

        List<User> users = userRepository.findAll();

        if (users.isEmpty()) {
            return List.of();
        }

        List<Notification> notifications = users.stream()
                .map(user -> notificationFactory.attributeAdded(user, attributeName, action))
                .toList();

        return saveAllAndPublish(notifications);
    }


    @Transactional(readOnly = true)
    public NotificationPageResponse getNotifications(NotificationPageRequest request) {

        Slice<Notification> search = notificationRepository.search(request);

        long totalElements = notificationRepository.countByUserId(request.userId());

        return NotificationPageResponse.from(search, totalElements);
    }

    @Transactional
    public void delete(NotificationDeleteRequest request) {

        // 해당유저의 알림ID 유무 판별
        boolean exists = notificationRepository
                .existsByIdAndUserId(request.notificationId(), request.userId());

        if (!exists) {
            throw new NotificationBadRequestException(
                    request.notificationId(),
                    request.userId()
            );
        }

        notificationRepository.deleteById(request.notificationId());
    }

    //단건
    private Notification saveAndPublish(Notification notification) {

        Notification saved = notificationRepository.save(notification);

        log.info("Notification saved type : {} id: {}", saved.getType(),saved.getId());

        eventPublisher.publishEvent(NotificationCreatedEvent.from(saved));

        log.info("Notification publish CreateEvent type : {} id: {}", saved.getType(),saved.getId());
        return saved;
    }
    //다건
    private List<Notification> saveAllAndPublish(List<Notification> notifications) {
        List<Notification> savedAll = notificationRepository.saveAll(notifications);

        log.info("Notifications savedAll count={}", savedAll.size());

        savedAll.forEach(notification ->
                eventPublisher.publishEvent(NotificationCreatedEvent.from(notification))
        );
        log.info("Publishing notification events count={}", savedAll.size());
        return savedAll;
    }



}
