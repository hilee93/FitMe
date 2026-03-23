package com.ootd.fitme.domain.feed.enums;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class SortCriteriaConverter implements Converter<String, SortCriteria> {

    @Override
    public SortCriteria convert(String source) {
        return SortCriteria.from(source);
    }
}
