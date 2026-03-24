package com.ootd.fitme.domain.user.service;

public interface UserService {
    String encodePassword(String password);
    boolean matchesPassword(String password, String encodedPassword);
}
