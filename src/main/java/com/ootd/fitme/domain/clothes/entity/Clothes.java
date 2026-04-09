package com.ootd.fitme.domain.clothes.entity;

import com.ootd.fitme.domain.base.BaseUpdateEntity;
import com.ootd.fitme.domain.clothes.enums.ClothesType;
import com.ootd.fitme.domain.clothes.exception.ClothesException;
import com.ootd.fitme.domain.clothesattribute.entity.ClothesAttribute;
import com.ootd.fitme.domain.selectablevalue.entity.SelectableValue;
import com.ootd.fitme.domain.user.entity.User;
import com.ootd.fitme.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "clothes")
public class Clothes extends BaseUpdateEntity {

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ClothesType clothesType;

    @JoinColumn(name = "user_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "clothes", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClothesAttribute> attributes = new ArrayList<>();

    private Clothes(String name, ClothesType clothesType, String imageUrl, User user) {
        validateClothesInfo(name, clothesType, user);

        this.name = name;
        this.clothesType = clothesType;
        this.imageUrl = imageUrl;
        this.user = user;

    }

    public static Clothes create(String name, ClothesType clothesType, User user) {
        return new Clothes(name, clothesType, null, user);
    }

    public static Clothes createWithImage(String name, ClothesType clothesType, User user, String imageUrl) {
        return new Clothes(name, clothesType, imageUrl, user);
    }

    public void replaceAttributes(List<ClothesAttribute> newAttributes) {
        this.attributes.clear();
        this.attributes.addAll(newAttributes);
    }

    public void updateClothesInfo(String name, ClothesType clothesType, String imageUrl) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (clothesType != null) {
            this.clothesType = clothesType;
        }
        if (!Objects.equals(this.imageUrl, imageUrl)) {
            this.imageUrl = imageUrl;
        }
    }

    public void updateAttributes(List<ClothesAttribute> incomingAttributes) {

        this.attributes.removeIf(existingAttr ->
                incomingAttributes.stream().noneMatch(incoming ->
                        incoming.getAttribute().getId().equals(existingAttr.getAttribute().getId())
                )
        );

        for (ClothesAttribute incoming : incomingAttributes) {
            Optional<ClothesAttribute> existingOpt = this.attributes.stream()
                    .filter(attr -> attr.getAttribute().getId().equals(incoming.getAttribute().getId()))
                    .findFirst();

            if (existingOpt.isPresent()) {
                ClothesAttribute existing = existingOpt.get();
                SelectableValue incomingOption = incoming.getClothesAttributeSelectableValue().getSelectableValue();

                if (!existing.getClothesAttributeSelectableValue().getSelectableValue().getId().equals(incomingOption.getId())) {
                    existing.getClothesAttributeSelectableValue().changeSelectableValue(incomingOption);
                }
            } else {
                this.attributes.add(incoming);
            }
        }
    }
    private void validateClothesInfo(String name, ClothesType clothesType, User user) {
        if (name == null || name.isBlank()) {
            throw new ClothesException(ErrorCode.CLOTHES_NAME_INVALID);
        }
        if (clothesType == null) {
            throw new ClothesException(ErrorCode.CLOTHES_TYPE_INVALID);
        }
        if (user == null) {
            throw new ClothesException(ErrorCode.CLOTHES_USER_INVALID);
        }
    }

}
