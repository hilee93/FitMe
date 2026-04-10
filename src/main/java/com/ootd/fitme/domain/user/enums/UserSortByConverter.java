package com.ootd.fitme.domain.user.enums;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class UserSortByConverter implements Converter<String, UserSortBy> {
    @Override
    public UserSortBy convert(String source) {
        return UserSortBy.from(source);
    }
}
