package com.ootd.fitme.infrastructure.external.kakao.location;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record KakaoCoord2RegionResponse(
        Meta meta,
        List<Document> documents
) {
    public record Meta(@JsonProperty("total_count") Integer totalCount) {
    }

    public record Document(
            @JsonProperty("region_type") String regionType,
            @JsonProperty("address_name") String addressName,
            @JsonProperty("region_1depth_name") String region1depthName,
            @JsonProperty("region_2depth_name") String region2depthName,
            @JsonProperty("region_3depth_name") String region3depthName,
            @JsonProperty("region_4depth_name") String region4depthName,
            @JsonProperty("code") String code,
            @JsonProperty("x") Double x, // longitude
            @JsonProperty("y") Double y // latitude
    ) {
    }
}
