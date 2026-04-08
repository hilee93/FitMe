package com.ootd.fitme.domain.useractivelocation.entity;

import com.ootd.fitme.domain.base.BaseUpdateEntity;
import com.ootd.fitme.domain.region.entity.Region;
import com.ootd.fitme.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_active_locations")
public class UserActiveLocation extends BaseUpdateEntity {

    @Column(name = "last_active_at", nullable = false)
    private Instant lastActiveAt;

    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    @JoinColumn(name = "region_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Region region;

    private UserActiveLocation(Instant lastActiveAt, User user, Region region) {
        this.lastActiveAt = lastActiveAt;
        this.user = user;
        this.region = region;
    }

    public static UserActiveLocation create(Instant lastActiveAt, User user, Region region) {
        return new UserActiveLocation(lastActiveAt, user, region);
    }

    public void update(Instant lastActiveAt, Region region) {
        this.lastActiveAt = lastActiveAt;
        this.region = region;
    }


}
