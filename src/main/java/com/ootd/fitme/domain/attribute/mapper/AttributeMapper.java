package com.ootd.fitme.domain.attribute.mapper;

import com.ootd.fitme.domain.attribute.dto.response.ClothesAttributeDefDto;
import com.ootd.fitme.domain.attribute.entity.Attribute;
import com.ootd.fitme.domain.selectablevalue.entity.SelectableValue;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AttributeMapper {

    public ClothesAttributeDefDto toDto(Attribute attribute) {
        List<String> stringValues = attribute.getSelectableValues().stream()
                .map(SelectableValue::getType)
                .toList();

        return new ClothesAttributeDefDto(
                attribute.getId(),
                attribute.getName(),
                stringValues,
                attribute.getCreatedAt()
        );
    }
}