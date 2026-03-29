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

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "attributes")
public class Attribute extends BaseUpdateEntity {

    @Column(name = "name", nullable = false, length = 100, unique = true)
    private String name;

    @OneToMany(mappedBy = "attribute", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<SelectableValue> selectableValues = new ArrayList<>();

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
        this.selectableValues.clear();
        this.addValues(newTypes);
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new AttributeException(ErrorCode.ATTRIBUTE_NAME_INVALID);
        }
    }
}
