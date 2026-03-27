package com.ootd.fitme.domain.selectablevalue.entity;

import com.ootd.fitme.domain.attribute.entity.Attribute;
import com.ootd.fitme.domain.base.BaseDeletableEntity;
import com.ootd.fitme.domain.selectablevalue.exception.SelectableValueException;
import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.global.exception.FitmeException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "selectable_values")
public class SelectableValue extends BaseDeletableEntity {

    @JoinColumn(name = "attribute_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Attribute attribute;

    @Column(name = "type", nullable = false, length = 100)
    private String type;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    private SelectableValue(String type, int displayOrder, Attribute attribute) {
        validateType(type);
        this.type = type;
        this.displayOrder = displayOrder;
        this.attribute = attribute;
    }

    public static SelectableValue create(String type, int displayOrder, Attribute attribute) {
        return new SelectableValue(type, displayOrder, attribute);
    }

    private void validateType(String type) {
        if (type == null || type.isBlank()) {
            throw new SelectableValueException(ErrorCode.SELECTABLE_VALUE_INVALID);
        }
    }
}
