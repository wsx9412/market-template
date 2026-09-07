package com.template.market.domain.model

/** 위도/경도 한 쌍. Android Location 에 의존하지 않기 위한 순수 모델입니다. */
data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
) {
    init {
        require(latitude in -90.0..90.0) { "위도 범위를 벗어났습니다: $latitude" }
        require(longitude in -180.0..180.0) { "경도 범위를 벗어났습니다: $longitude" }
    }
}
