package com.ootd.fitme.domain.notification.service;

import com.ootd.fitme.domain.follow.repository.FollowRepository;
import com.ootd.fitme.domain.notification.dto.request.NotificationPageRequest;
import com.ootd.fitme.domain.notification.dto.response.NotificationPageResponse;
import com.ootd.fitme.domain.notification.dto.response.NotificationDto;
import com.ootd.fitme.domain.notification.entity.Notification;
import com.ootd.fitme.domain.notification.entity.NotificationFactory;
import com.ootd.fitme.domain.notification.mapper.NotificationMapper;
import com.ootd.fitme.domain.notification.repository.NotificationRepository;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationFactory notificationFactory;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;


    @Transactional
    public Notification notifyDirectMessage(UUID receiverId, String senderName,String message) {

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        Notification notification = notificationFactory.dm(receiver, senderName,message);
        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification notifyFollowed(UUID followeeId, String followerName) {

        User user = userRepository.findById(followeeId)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        Notification notification = notificationFactory.followed(user, followerName);
        return notificationRepository.save(notification);
    }

    @Transactional
    public List<Notification> notifyWeatherAlert(String weatherAlert) {
        List<User> users = userRepository.findAll();

        List<Notification> notifications = users.stream()
                .map(user -> notificationFactory.weatherAlert(user, weatherAlert))
                .toList();

        return notificationRepository.saveAll(notifications);

    }

    @Transactional
    public Notification notifyFeedLiked(UUID likedId, String likerName) {

        User user = userRepository.findById(likedId)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        Notification notification = notificationFactory.feedLiked(user, likerName);
        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification notifyFeedCommented(UUID feedOwnerId, String commenterName, String comment) {

        User user = userRepository.findById(feedOwnerId)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        Notification notification = notificationFactory.feedCommented(user, commenterName,comment);
        return notificationRepository.save(notification);
    }

    @Transactional
    public List<Notification> notifyFollowerNewFeed(UUID followeeId, String writerName, String feedName) {


        List<UUID> followerIds = followRepository.findFollowerIdsByFolloweeId(followeeId);

        List<User> followers = userRepository.findAllById(followerIds);

        List<Notification> notifications = followers.stream()
                .map(user -> notificationFactory.followerNewFeed(user, writerName, feedName))
                .toList();

        return notificationRepository.saveAll(notifications);
    }


    @Transactional
    public List<Notification> notifyAttributeAdded(String attributeName) {

        List<User> users = userRepository.findAll();

        List<Notification> notifications = users.stream()
                .map(user -> notificationFactory.attributeAdded(user,attributeName))
                .toList();

        return notificationRepository.saveAll(notifications);
    }


    @Transactional(readOnly = true)
    public NotificationPageResponse getNotifications(NotificationPageRequest request) {

        Slice<Notification> search = notificationRepository.search(request);

        long totalElements = notificationRepository.countByUserId(request.userId());

        List<NotificationDto> pageDtoList = search.getContent()
                .stream()
                .map(NotificationMapper::toDto)
                .toList();

        List<Notification> content = search.getContent();
        String nextCursor = null;
        String nextIdAfter = null;

        if (search.hasNext() && !content.isEmpty()) {
            Notification last = content.get(content.size() - 1);
            nextCursor = last.getCreatedAt().toString();
            nextIdAfter = last.getId().toString();
        }

        return new NotificationPageResponse(
                pageDtoList,
                nextCursor,
                nextIdAfter,
                search.hasNext(),
                totalElements,
                "createdAt",
                "DESCENDING");
    }

    @Transactional
    public void delete(UUID notificationId) {

        notificationRepository.deleteById(notificationId);
    }
}
