package com.ootd.fitme.domain.profile.entity;

import com.ootd.fitme.domain.base.BaseUpdateEntity;
import com.ootd.fitme.domain.profile.enums.Gender;
import com.ootd.fitme.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "profiles")
public class Profile extends BaseUpdateEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "x")
    private Integer x;

    @Column(name = "y")
    private Integer y;

    @Column(name = "region_1depth_name", length = 50)
    private String regionOneDepthName;

    @Column(name = "region_2depth_name", length = 50)
    private String regionTwoDepthName;

    @Column(name = "region_3depth_name", length = 50)
    private String regionThreeDepthName;

    @Column(name = "gender")
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "temperature_sensitivity", nullable = false)
    private int temperatureSensitivity = 3;

    @Column(name = "profile_image_url", length = 1000)
    private String profileImageUrl;

    @Column(name = "follower_count", nullable = false)
    private int followerCount = 0;

    @Column(name = "followee_count", nullable = false)
    private int followeeCount = 0;

    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    private Profile(String name,
                    Double longitude,
                    Double latitude,
                    Integer x,
                    Integer y,
                    String regionOneDepthName,
                    String regionTwoDepthName,
                    String regionThreeDepthName,
                    Gender gender, LocalDate birthDate,
                    String profileImageUrl,
                    User user
    ) {
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
        this.x = x;
        this.y = y;
        this.regionOneDepthName = regionOneDepthName;
        this.regionTwoDepthName = regionTwoDepthName;
        this.regionThreeDepthName = regionThreeDepthName;
        this.gender = gender;
        this.birthDate = birthDate;
        this.profileImageUrl = profileImageUrl;
        this.user = user;
    }

    public static Profile create(
            String name,
            Double longitude,
            Double latitude,
            Integer x,
            Integer y,
            String regionOneDepthName,
            String regionTwoDepthName,
            String regionThreeDepthName,
            Gender gender,
            LocalDate birthDate,
            String profileImageUrl,
            User user
    ) {
        return new Profile(
                name,
                longitude,
                latitude,
                x,
                y,
                regionOneDepthName,
                regionTwoDepthName,
                regionThreeDepthName,
                gender,
                birthDate,
                profileImageUrl,
                user
        );
    }

    public void increaseFollowerCount() {
        this.followerCount++;
    }

    public void decreaseFollowerCount() {
        // TODO : 커스텀 예외 처리
        if (this.followerCount <= 0) {
            throw new IllegalStateException("팔로워 수가 이미 0명 입니다.");
        }
        this.followerCount--;
    }

    public void increaseFolloweeCount() {
        this.followeeCount++;
    }

    public void decreaseFolloweeCount() {
        // TODO : 커스텀 예외 처리
        if (this.followeeCount <= 0) {
            throw new IllegalStateException("팔로우 수가 이미 0명 입니다.");
        }
        this.followeeCount--;
    }

}
