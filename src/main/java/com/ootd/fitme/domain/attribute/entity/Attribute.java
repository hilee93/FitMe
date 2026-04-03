package com.ootd.fitme.domain.attribute.entity;

import com.ootd.fitme.domain.attribute.exception.AttributeException;
import com.ootd.fitme.domain.base.BaseUpdateEntity;
import com.ootd.fitme.domain.selectablevalue.entity.SelectableValue;
import com.ootd.fitme.global.exception.ErrorCode;
import com.ootd.fitme.global.exception.FitmeException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "attributes")
public class Attribute extends BaseUpdateEntity {

    @Column(name = "name", nullable = false, length = 100, unique = true)
    private String name;

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "attribute", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private final List<SelectableValue> selectableValues = new ArrayList<>();

    private Attribute(String name) {
        validateName(name);
        this.name = name;
    }

    public static Attribute create(String name) {
        return new Attribute(name);
    }

    public void addValues(List<String> types) {
        if (types == null || types.isEmpty()) return;
        for (int i = 0; i < types.size(); i++) {
            this.selectableValues.add(SelectableValue.create(types.get(i), i, this));
        }
    }

    public void updateAttribute(String newName, List<String> newTypes) {
        validateName(newName);
        this.name = newName;

        this.selectableValues.removeIf(existing -> !newTypes.contains(existing.getType()));

        for (int i = 0; i < newTypes.size(); i++) {
            String targetType = newTypes.get(i);
            int newOrder = i;

            Optional<SelectableValue> existingOpt = this.selectableValues.stream()
                    .filter(v -> v.getType().equals(targetType))
                    .findFirst();

            if (existingOpt.isPresent()) {
                existingOpt.get().updateDisplayOrder(newOrder);
            } else {
                this.selectableValues.add(SelectableValue.create(targetType, newOrder, this));
            }
        }
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new AttributeException(ErrorCode.ATTRIBUTE_NAME_INVALID);
        }
    }

}
