package com.ootd.fitme.domain.catalog.entity;

import com.ootd.fitme.domain.base.BaseEntity;
import com.ootd.fitme.domain.clothes.enums.ClothesType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CatalogClothes extends BaseEntity {

    @Column(name = "original_url", nullable = false, length = 512, unique = true)
    private String originalUrl;

    @Column(nullable = false)
    private String name;

    @Column(name = "image_url")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ClothesType type;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> attributes = new HashMap<>();

    private CatalogClothes(String originalUrl, String name, String imageUrl, ClothesType type, Map<String, Object> attributes) {
        this.originalUrl = originalUrl;
        this.name = name;
        this.imageUrl = imageUrl;
        this.type = type;
        this.attributes = attributes != null ? attributes : new HashMap<>();
    }

    public static CatalogClothes create(String originalUrl, String name, String imageUrl, ClothesType type, Map<String, Object> attributes) {
        return new CatalogClothes(originalUrl, name, imageUrl, type, attributes);
    }
}