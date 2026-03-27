package com.ootd.fitme.global.security.auth;

import com.ootd.fitme.domain.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
public class CustomUserPrincipal implements UserDetails {
    private final UUID userId;
    private final String email;
    private final boolean locked;
    private final Collection<? extends GrantedAuthority> authorities;

    private CustomUserPrincipal(User user) {
        this.userId = user.getId();
        this.email = user.getEmail();
        this.locked = user.isLocked();
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    public static CustomUserPrincipal from(User user) {
        return new CustomUserPrincipal(user);
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
