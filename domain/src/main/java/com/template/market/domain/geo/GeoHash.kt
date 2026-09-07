package com.template.market.domain.geo

import com.template.market.domain.model.GeoPoint

/**
 * geohash 인코더.
 *
 * 게시글에 geohash 를 함께 저장해 두면, 반경 검색을 지원하지 않는 백엔드(Firestore, 단순 REST 등)에서도
 * "prefix 가 이 목록 중 하나로 시작하는 글"이라는 단순 질의만으로 근처 글을 1차 추림할 수 있습니다.
 * 정확한 반경 판정은 받아온 뒤 [GeoMath.distanceKm] 로 마무리합니다.
 */
object GeoHash {

    private const val BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz"

    /** 자리수별 대략적인 셀 크기(km). 반경에 맞는 자리수를 고를 때 씁니다. */
    private val CELL_WIDTH_KM = doubleArrayOf(
        5000.0, // 1
        1250.0, // 2
        156.0,  // 3
        39.1,   // 4
        4.89,   // 5
        1.22,   // 6
        0.153,  // 7
        0.0382, // 8
    )

    fun encode(point: GeoPoint, precision: Int = 8): String {
        require(precision in 1..12) { "precision 은 1..12 여야 합니다." }
        var minLat = -90.0
        var maxLat = 90.0
        var minLon = -180.0
        var maxLon = 180.0

        val hash = StringBuilder(precision)
        var isEven = true
        var bit = 0
        var current = 0

        while (hash.length < precision) {
            if (isEven) {
                val mid = (minLon + maxLon) / 2
                if (point.longitude > mid) {
                    current = (current shl 1) or 1
                    minLon = mid
                } else {
                    current = current shl 1
                    maxLon = mid
                }
            } else {
                val mid = (minLat + maxLat) / 2
                if (point.latitude > mid) {
                    current = (current shl 1) or 1
                    minLat = mid
                } else {
                    current = current shl 1
                    maxLat = mid
                }
            }
            isEven = !isEven

            if (bit < 4) {
                bit++
            } else {
                hash.append(BASE32[current])
                bit = 0
                current = 0
            }
        }
        return hash.toString()
    }

    /**
     * [radiusKm] 를 담기에 적당한 geohash 자리수.
     * 셀 하나가 반경보다 커지는 첫 자리수를 고릅니다.
     */
    fun precisionFor(radiusKm: Double): Int {
        for (i in CELL_WIDTH_KM.indices.reversed()) {
            if (CELL_WIDTH_KM[i] >= radiusKm) return i + 1
        }
        return 1
    }

    /**
     * 반경을 덮는 geohash prefix 목록(자기 셀 + 인접 8칸).
     * 서버에는 `geoHash LIKE '<prefix>%'` 또는 `startAt/endAt` 범위 질의로 넘기면 됩니다.
     */
    fun coveringPrefixes(center: GeoPoint, radiusKm: Double): List<String> {
        val precision = precisionFor(radiusKm)
        val box = GeoMath.boundingBox(center, radiusKm)
        val corners = listOf(
            center,
            GeoPoint(box.minLat, box.minLon),
            GeoPoint(box.minLat, center.longitude),
            GeoPoint(box.minLat, box.maxLon),
            GeoPoint(center.latitude, box.minLon),
            GeoPoint(center.latitude, box.maxLon),
            GeoPoint(box.maxLat, box.minLon),
            GeoPoint(box.maxLat, center.longitude),
            GeoPoint(box.maxLat, box.maxLon),
        )
        return corners.map { encode(it, precision) }.distinct()
    }
}
