package com.ootd.fitme.domain.notification.repository;

import com.ootd.fitme.domain.profile.entity.Profile;
import com.ootd.fitme.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface NotificationProfileRepository extends JpaRepository<Profile, UUID> {

    @Query("""
    select p.user
    from Profile p
    where p.regionOneDepthName = :region1
      and p.regionTwoDepthName = :region2
    """)
    List<User> findUsersByRegion1AndRegion2(String region1, String region2);

}
