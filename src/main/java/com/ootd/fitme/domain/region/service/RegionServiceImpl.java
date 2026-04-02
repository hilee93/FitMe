package com.ootd.fitme.domain.region.service;

import com.ootd.fitme.domain.region.entity.Region;
import com.ootd.fitme.domain.region.repository.RegionRepository;
import com.ootd.fitme.domain.weatherforecast.dto.response.WeatherAPILocation;
import com.ootd.fitme.infrastructure.external.kakao.location.KakaoLocalClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegionServiceImpl implements RegionService {
    private final KakaoLocalClient kakaoLocalClient;
    private final RegionRepository regionRepository;

    @Override
    @Transactional
    public Region resolveAndUpsert(double longitude, double latitude) {
        KakaoLocalClient.RegionInfo info = kakaoLocalClient.resolveRegion(longitude, latitude);

        Integer x = (int) Math.round(latitude);
        Integer y = (int) Math.round(longitude);

        return regionRepository.findByRegionCode(info.regionCode())
                .map(existing -> {
                    existing.update(
                            info.addressName(),
                            info.region1depthName(),
                            info.region2depthName(),
                            info.region3depthName(),
                            info.region4depthName(),
                            info.longitude(),
                            info.latitude(),
                            x,
                            y
                    );
                    return existing;
                })
                .orElseGet(() -> regionRepository.save(Region.create(
                        info.regionCode(),
                        info.addressName(),
                        info.region1depthName(),
                        info.region2depthName(),
                        info.region3depthName(),
                        info.region4depthName(),
                        info.longitude(),
                        info.latitude(),
                        x,
                        y
                )));
    }

    @Override
    @Transactional
    public WeatherAPILocation resolveLocation(double longitude, double latitude) {
        Region region = resolveAndUpsert(longitude, latitude);

        List<String> names = Stream.of(
                        region.getRegion1depthName(),
                        region.getRegion2depthName(),
                        region.getRegion3depthName(),
                        region.getRegion4depthName()
                )
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .toList();

        return new WeatherAPILocation(
                region.getLatitude(),
                region.getLongitude(),
                region.getX(),
                region.getY(),
                names
        );
    }
}
