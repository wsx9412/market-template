package com.template.market.domain.location

import com.template.market.domain.model.GeoPoint
import com.template.market.domain.model.Region

/** 기기의 현재 GPS 좌표를 얻는 계약. */
interface LocationSource {
    /** 권한이 없거나 위치를 못 잡으면 null. */
    suspend fun currentLocation(): GeoPoint?
}

/**
 * ★ 교체 지점 ★
 *
 * 좌표 → 행정구역(동/읍/면) 변환 계약.
 * 기본 구현은 안드로이드 내장 Geocoder(`GeocoderRegionResolver`)이고,
 * 정확도가 더 필요하면 카카오/네이버 로컬 API 구현체로 갈아끼우면 됩니다.
 */
interface RegionResolver {
    suspend fun resolve(point: GeoPoint): Region?
}
