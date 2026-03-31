package com.ootd.fitme.domain.clothesattribute.entity;

import com.ootd.fitme.domain.attribute.entity.Attribute;
import com.ootd.fitme.domain.base.BaseEntity;
import com.ootd.fitme.domain.clothes.entity.Clothes;
import com.ootd.fitme.domain.clothesattributeselectablevalue.entity.ClothesAttributeSelectableValue;
import com.ootd.fitme.domain.selectablevalue.entity.SelectableValue;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "clothes_attributes")
public class ClothesAttribute extends BaseEntity {

    @JoinColumn(name = "clothes_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Clothes clothes;

    @JoinColumn(name = "attribute_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Attribute attribute;

    @OneToOne(mappedBy = "clothesAttribute", cascade = CascadeType.ALL, orphanRemoval = true)
    private ClothesAttributeSelectableValue clothesAttributeSelectableValue;

    private ClothesAttribute(Clothes clothes, Attribute attribute) {
        this.clothes = clothes;
        this.attribute = attribute;
    }

    public static ClothesAttribute create(Clothes clothes, Attribute attribute) {
        return new ClothesAttribute(clothes, attribute);
    }

    public void assignOption(SelectableValue selectableValue) {
        this.clothesAttributeSelectableValue = ClothesAttributeSelectableValue.create(this, selectableValue);
    }
}
