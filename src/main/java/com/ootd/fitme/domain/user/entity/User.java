package com.ootd.fitme.domain.user.entity;

import com.ootd.fitme.domain.base.BaseUpdateEntity;
import com.ootd.fitme.domain.user.enums.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseUpdateEntity {

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", length = 100)
    private String password;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    @Column(name = "locked", nullable = false)
    private boolean locked = false;

    @Column(name = "provider")
    private String provider;

    @Column(name = "provider_id", length = 300)
    private String providerId;

    private User(String email, String password, String provider, String providerId) {
        this.email = email;
        this.password = password;
        this.provider = provider;
        this.providerId = providerId;
    }

    public static User create(String email, String password) {
        return new User(email, password, null, null);
    }

    public static User createWithOAuth(String email, String provider, String providerId) {
        return new User(email, null, provider, providerId);
    }

    public void updateRole(Role role) {
        this.role = role;
    }

    public void updateLocked(boolean locked) {
        this.locked = locked;
    }

}
