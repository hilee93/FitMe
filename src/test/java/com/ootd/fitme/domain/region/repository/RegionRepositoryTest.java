package com.ootd.fitme.domain.region.repository;

import com.ootd.fitme.domain.region.entity.Region;
import com.ootd.fitme.global.config.JpaAuditingConfig;
import com.ootd.fitme.global.config.QuerydslConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({JpaAuditingConfig.class, QuerydslConfig.class})
public class RegionRepositoryTest {
    @Autowired
    private RegionRepository regionRepository;

    @Test
    @DisplayName("findByRegionCode - 존재하면 Region을 반환한다")
    void findByRegionCode_exists_returnRegion() {
        Region saved = regionRepository.save(Region.create(
                "1111061500",
                "서울 종로구 청운효자동",
                "서울",
                "종로구",
                "청운효자동",
                null,
                126.9707,
                37.5841,
                38,
                127
        ));

        Optional<Region> result = regionRepository.findByRegionCode("1111061500");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(saved.getId());
        assertThat(result.get().getRegionFullName()).isEqualTo("서울 종로구 청운효자동");
    }

    @Test
    @DisplayName("findByRegionCode - 없으면 empty를 반환")
    void findByRegionCode_notExists_returnEmpty() {
        Optional<Region> result = regionRepository.findByRegionCode("9999999999");
        assertThat(result).isEmpty();
    }
}
