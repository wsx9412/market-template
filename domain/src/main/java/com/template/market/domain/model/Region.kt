package com.template.market.domain.model

/**
 * 행정구역 단위의 "내 동네".
 *
 * [eupMyeonDong] 이 사용자에게 보여줄 최소 단위(동/읍/면)입니다.
 * 예) 시도="강원특별자치도", 시군구="홍천군", 읍면동="동면"
 */
data class Region(
    val siDo: String,
    val siGunGu: String,
    val eupMyeonDong: String,
    val center: GeoPoint,
) {
    /** 목록·상세에 표기하는 짧은 이름. 읍면동이 비면 시군구로 대체합니다. */
    val shortName: String
        get() = eupMyeonDong.ifBlank { siGunGu.ifBlank { siDo } }

    /** "강원특별자치도 홍천군 동면" 형태의 전체 이름. */
    val fullName: String
        get() = listOf(siDo, siGunGu, eupMyeonDong)
            .filter { it.isNotBlank() }
            .joinToString(" ")

    companion object {
        val UNKNOWN = Region("", "", "", GeoPoint(37.5665, 126.9780))
    }
}
