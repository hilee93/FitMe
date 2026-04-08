package com.ootd.fitme.domain.region.entity;

import com.ootd.fitme.domain.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "regions")
public class Region extends BaseEntity {

    @Column(name = "region_code", nullable = false, length = 10, unique = true)
    private String regionCode;

    @Column(name = "region_full_name", nullable = false, unique = true)
    private String regionFullName;

    @Column(name = "region_1depth_name", nullable = false, length = 50)
    private String region1depthName;

    @Column(name = "region_2depth_name", nullable = false, length = 50)
    private String region2depthName;

    @Column(name = "region_3depth_name", nullable = false, length = 50)
    private String region3depthName;

    @Column(name = "region_4depth_name", length = 50)
    private String region4depthName;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "latitude", nullable = false)
    private Double latitude;


    @Column(name = "x", nullable = false)
    private Integer x;

    @Column(name = "y", nullable = false)
    private Integer y;


    private Region(
            String regionCode,
            String regionFullName,
            String region1depthName,
            String region2depthName,
            String region3depthName,
            String region4depthName,
            Double longitude,
            Double latitude,
            Integer x,
            Integer y
    ) {
        this.regionCode = regionCode;
        this.regionFullName = regionFullName;
        this.region1depthName = region1depthName;
        this.region2depthName = region2depthName;
        this.region3depthName = region3depthName;
        this.region4depthName = region4depthName;
        this.longitude = longitude;
        this.latitude = latitude;
        this.x = x;
        this.y = y;
    }

    public static Region create(
            String regionCode,
            String regionFullName,
            String region1depthName,
            String region2depthName,
            String region3depthName,
            String region4depthName,
            Double longitude,
            Double latitude,
            Integer x,
            Integer y
    ) {
        return new Region(
                regionCode,
                regionFullName,
                region1depthName,
                region2depthName,
                region3depthName,
                region4depthName,
                longitude,
                latitude,
                x,
                y
        );
    }

    public void update(
            String regionFullName,
            String region1depthName,
            String region2depthName,
            String region3depthName,
            String region4depthName,
            Double longitude,
            Double latitude,
            Integer x,
            Integer y
    ) {
        this.regionFullName = regionFullName;
        this.region1depthName = region1depthName;
        this.region2depthName = region2depthName;
        this.region3depthName = region3depthName;
        this.region4depthName = region4depthName;
        this.longitude = longitude;
        this.latitude = latitude;
        this.x = x;
        this.y = y;
    }


}