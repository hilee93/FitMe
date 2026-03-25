package com.ootd.fitme.domain.clothesattributeselectablevalue.entity;

import com.ootd.fitme.domain.base.BaseEntity;
import com.ootd.fitme.domain.clothesattribute.entity.ClothesAttribute;
import com.ootd.fitme.domain.selectablevalue.entity.SelectableValue;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "clothes_attributes_selectable_values")
public class ClothesAttributeSelectableValue extends BaseEntity {


    @JoinColumn(name = "clothes_attr_id", nullable = false, unique = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ClothesAttribute clothesAttribute;

    @JoinColumn(name = "value_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private SelectableValue selectableValue;

    private ClothesAttributeSelectableValue(ClothesAttribute clothesAttribute, SelectableValue selectableValue) {
        this.clothesAttribute = clothesAttribute;
        this.selectableValue = selectableValue;
    }

    public static ClothesAttributeSelectableValue create(ClothesAttribute clothesAttribute, SelectableValue selectableValue) {
        return new ClothesAttributeSelectableValue(clothesAttribute, selectableValue);
    }
}
