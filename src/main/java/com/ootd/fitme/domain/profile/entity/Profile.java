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

}