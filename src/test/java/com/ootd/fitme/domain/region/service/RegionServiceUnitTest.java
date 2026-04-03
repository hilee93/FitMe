package com.ootd.fitme.domain.region.service;

import com.ootd.fitme.domain.region.entity.Region;
import com.ootd.fitme.domain.region.repository.RegionRepository;
import com.ootd.fitme.domain.weatherforecast.dto.response.WeatherAPILocation;
import com.ootd.fitme.infrastructure.external.kakao.location.KakaoLocalClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
public class RegionServiceUnitTest {
    @Mock
    private KakaoLocalClient kakaoLocalClient;

    @Mock
    private RegionRepository regionRepository;

    @InjectMocks
    private RegionServiceImpl regionService;

    @Nested
    @DisplayName("resolveAndUpsert")
    class ResolveAndUpsertTest {
        @Test
        @DisplayName("신규 region_code면 생성 후 저장한다")
        void create_whenNotExist() {
            KakaoLocalClient.RegionInfo info = new KakaoLocalClient.RegionInfo(
                    "1111061500",
                    "서울 종로구 청운효자동",
                    "서울",
                    "종로구",
                    "청운효자동",
                    null,
                    126.9707,
                    37.5841
            );

            given(kakaoLocalClient.resolveRegion(126.9707, 37.5841)).willReturn(info);
            given(regionRepository.findByRegionCode("1111061500")).willReturn(Optional.empty());
            given(regionRepository.save(any(Region.class))).willAnswer(inv -> inv.getArgument(0));

            Region result = regionService.resolveAndUpsert(126.9707, 37.5841);

            assertThat(result.getRegionCode()).isEqualTo("1111061500");
            assertThat(result.getRegionFullName()).isEqualTo("서울 종로구 청운효자동");
            assertThat(result.getX()).isEqualTo((int) Math.round(126.9707));
            assertThat(result.getY()).isEqualTo((int) Math.round(37.5841));

            ArgumentCaptor<Region> captor = ArgumentCaptor.forClass(Region.class);
            verify(regionRepository).save(captor.capture());
            assertThat(captor.getValue().getRegionCode()).isEqualTo("1111061500");
        }

        @Test
        @DisplayName("기존 region_code면 update만 수행하고 save는 호출하지 않는다")
        void update_whenExist() {
            Region existing = Region.create(
                    "1111061500",
                    "기존 주소",
                    "기존시도",
                    "기존시군구",
                    "기존읍면동",
                    null,
                    127.0,
                    37.0,
                    37,
                    127
            );

            KakaoLocalClient.RegionInfo info = new KakaoLocalClient.RegionInfo(
                    "1111061500",
                    "서울 종로구 청운효자동",
                    "서울",
                    "종로구",
                    "청운효자동",
                    null,
                    126.9707,
                    37.5841
            );

            given(kakaoLocalClient.resolveRegion(126.9707, 37.5841)).willReturn(info);
            given(regionRepository.findByRegionCode("1111061500")).willReturn(Optional.of(existing));

            Region result = regionService.resolveAndUpsert(126.9707, 37.5841);

            assertThat(result).isSameAs(existing);
            assertThat(existing.getRegionFullName()).isEqualTo("서울 종로구 청운효자동");
            assertThat(existing.getRegion1depthName()).isEqualTo("서울");
            assertThat(existing.getX()).isEqualTo((int) Math.round(126.9707));
            assertThat(existing.getY()).isEqualTo((int) Math.round(37.5841));

            verify(regionRepository, never()).save(any(Region.class));
        }
    }

    @Test
    @DisplayName("resolveLocation은 WeatherAPILocation을 구성해서 반환")
    void resolveLocation_success() {
        KakaoLocalClient.RegionInfo info = new KakaoLocalClient.RegionInfo(
                "1111061500",
                "서울 종로구 청운효자동",
                "서울",
                "종로구",
                "청운효자동",
                null,
                126.9707,
                37.5841
        );

        given(kakaoLocalClient.resolveRegion(126.9707, 37.5841)).willReturn(info);
        given(regionRepository.findByRegionCode("1111061500")).willReturn(Optional.empty());
        given(regionRepository.save(any(Region.class))).willAnswer(inv -> inv.getArgument(0));

        WeatherAPILocation location = regionService.resolveLocation(126.9707, 37.5841);

        assertThat(location.latitude()).isEqualTo(37.5841);
        assertThat(location.longitude()).isEqualTo(126.9707);
        assertThat(location.x()).isEqualTo((int) Math.round(126.9707));
        assertThat(location.y()).isEqualTo((int) Math.round(37.5841));
        assertThat(location.locationNames()).containsExactly("서울", "종로구", "청운효자동");
    }
}
