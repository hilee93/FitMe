package com.ootd.fitme.domain.notiication.service;

import com.ootd.fitme.domain.notiication.entity.Notification;
import com.ootd.fitme.domain.notiication.entity.NotificationFactory;
import com.ootd.fitme.domain.notiication.repository.NotificationRepository;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationFactory notificationFactory;
    private final UserRepository userRepository;


    @Transactional
    public Notification notifyDirectMessage(UUID userId, String senderName,String message) {

        User receiver = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        Notification notification = notificationFactory.dm(receiver, senderName,message);
        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification notifyFollowed(UUID userId, String followerName) {

        User user = userRepository.findById(userId)
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
    public Notification notifyFeedLiked(UUID userId, String likerName) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        Notification notification = notificationFactory.feedLiked(user, likerName);
        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification notifyFeedCommented(UUID userId, String commenterName, String comment) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        Notification notification = notificationFactory.feedCommented(user, commenterName,comment);
        return notificationRepository.save(notification);
    }

    @Transactional
    public List<Notification> notifyFollowerNewFeed(UUID userId, String writerName, String feedName) {

        List<User> users = userRepository.findAll();

        List<Notification> notifications = users.stream()
                .map(user -> notificationFactory.followerNewFeed(user,writerName,feedName))
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



    @Transactional
    public void delete(UUID notificationId) {

        notificationRepository.deleteById(notificationId);
    }
}
