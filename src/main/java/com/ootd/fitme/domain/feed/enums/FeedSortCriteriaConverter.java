package com.ootd.fitme.domain.feed.enums;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class FeedSortCriteriaConverter implements Converter<String, FeedSortCriteria> {

    @Override
    public FeedSortCriteria convert(String source) {
        return FeedSortCriteria.from(source);
    }
}
