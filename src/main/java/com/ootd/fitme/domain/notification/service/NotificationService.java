package com.ootd.fitme.domain.notification.service;

import com.ootd.fitme.domain.follow.repository.FollowRepository;
import com.ootd.fitme.domain.notification.dto.request.NotificationDeleteRequest;
import com.ootd.fitme.domain.notification.dto.request.NotificationPageRequest;
import com.ootd.fitme.domain.notification.dto.response.NotificationDto;
import com.ootd.fitme.domain.notification.dto.response.NotificationPageResponse;
import com.ootd.fitme.domain.notification.entity.Notification;
import com.ootd.fitme.domain.notification.entity.NotificationFactory;
import com.ootd.fitme.domain.notification.enums.AttributeAction;
import com.ootd.fitme.domain.notification.exception.NotificationBadRequestException;
import com.ootd.fitme.domain.notification.mapper.NotificationMapper;
import com.ootd.fitme.domain.notification.repository.NotificationProfileRepository;
import com.ootd.fitme.domain.notification.repository.NotificationRepository;
import com.ootd.fitme.domain.profile.entity.Profile;
import com.ootd.fitme.domain.profile.exception.ProfileException;
import com.ootd.fitme.domain.profile.repository.ProfileRepository;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.domain.user.exception.user.UserException;
import com.ootd.fitme.domain.user.repository.UserRepository;
import com.ootd.fitme.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
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
    private final FollowRepository followRepository;
    private final NotificationSseService notificationSseService;
    //private final ProfileRepository profileRepository; TODO : 나중에 추가하신다고 일단 내 레포로 대체
    private final NotificationProfileRepository notificationProfileRepository;
    private final ProfileRepository profileRepository;


    @Transactional
    public Notification notifyDirectMessage(UUID receiverId, String senderName, String message) {

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        Notification notification = notificationFactory.dm(receiver, senderName, message);

        Notification saved = notificationRepository.save(notification);

        NotificationDto notificationDto = NotificationMapper.toDto(saved);

        notificationSseService.send(receiverId, notificationDto);

        return saved;
    }

    @Transactional
    public Notification notifyFollowed(UUID followeeId, String followerName) {

        User user = userRepository.findById(followeeId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));


        Notification notification = notificationFactory.followed(user, followerName);

        Notification saved = notificationRepository.save(notification);

        NotificationDto notificationDto = NotificationMapper.toDto(saved);

        notificationSseService.send(followeeId, notificationDto);

        return saved;
    }

    @Transactional
    public List<Notification> notifyWeatherAlert(List<UUID> receiverIds, String region1, String region2, String weatherAlert) {
        if (receiverIds == null || receiverIds.isEmpty()) {
            return List.of();
        }

        List<UUID> uniqueReceiverIds = receiverIds.stream().distinct().toList();
        List<User> users = userRepository.findAllById(uniqueReceiverIds);

        if (users.isEmpty()) {
            return List.of();
        }

        List<Notification> notifications = users.stream()
                .map(user -> notificationFactory.weatherAlert(user, region1, region2, weatherAlert))
                .toList();

        List<Notification> saveds = notificationRepository.saveAll(notifications);

        for (Notification saved : saveds) {
            NotificationDto dto = NotificationMapper.toDto(saved);
            notificationSseService.send(saved.getUser().getId(), dto);
        }

        return saveds;
    }

    @Transactional
    public Notification notifyFeedLiked(UUID targetUserId, String content, UUID likerId) {

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        Profile likerProfile = profileRepository.findByUserId(likerId).orElseThrow(() -> new ProfileException(ErrorCode.PROFILE_NOT_FOUND));

        Notification notification = notificationFactory.feedLiked(targetUser, content, likerProfile.getName());

        Notification saved = notificationRepository.save(notification);

        NotificationDto notificationDto = NotificationMapper.toDto(saved);

        notificationSseService.send(targetUserId, notificationDto);

        return saved;
    }

    @Transactional
    public Notification notifyFeedCommented(UUID feedOwnerId, String content, UUID commenterId, String comment) {

        User user = userRepository.findById(feedOwnerId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        Profile commenterProfile = profileRepository.findByUserId(commenterId).orElseThrow(() -> new ProfileException(ErrorCode.PROFILE_NOT_FOUND));


        Notification notification = notificationFactory.feedCommented(user, content, commenterProfile.getName(), comment);

        Notification saved = notificationRepository.save(notification);

        NotificationDto notificationDto = NotificationMapper.toDto(saved);

        notificationSseService.send(feedOwnerId, notificationDto);

        return saved;
    }

    @Transactional
    public List<Notification> notifyFollowerNewFeed(UUID followeeId, String content
    ) {

        List<UUID> followerIds = followRepository.findFollowerIdsByFolloweeId(followeeId);

        List<User> followers = userRepository.findAllById(followerIds);

        Profile profile = profileRepository.findByUserId(followeeId).orElseThrow();

        List<Notification> notifications = followers.stream()
                .map(user -> notificationFactory.followerNewFeed(user, profile.getName(), content))
                .toList();

        List<Notification> saved = notificationRepository.saveAll(notifications);

        saved.forEach(notification ->
                notificationSseService.send(
                        notification.getUser().getId(),
                        NotificationMapper.toDto(notification)
                )
        );

        return saved;
    }


    @Transactional
    public List<Notification> notifyAttributeAdded(String attributeName, AttributeAction action) {

        List<User> users = userRepository.findAll();

        List<Notification> notifications = users.stream()
                .map(user -> notificationFactory.attributeAdded(user, attributeName, action))
                .toList();

        List<Notification> saved = notificationRepository.saveAll(notifications);


        NotificationDto notificationDto = NotificationMapper.toDto(saved.get(0));

        notificationSseService.sendAll(notificationDto);

        return saved;
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


}
