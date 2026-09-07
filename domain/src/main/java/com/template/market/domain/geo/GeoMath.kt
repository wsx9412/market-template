package com.template.market.domain.geo

import com.template.market.domain.model.GeoPoint
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/** 지구 반지름(km). */
private const val EARTH_RADIUS_KM = 6371.0088

object GeoMath {

    /**
     * 두 지점 사이의 대권거리(km). Haversine 공식.
     *
     * 수 km~수십 km 범위의 동네 반경 판정에는 오차가 무시할 수준이라 이 정도면 충분합니다.
     */
    fun distanceKm(from: GeoPoint, to: GeoPoint): Double {
        val dLat = (to.latitude - from.latitude).toRadians()
        val dLon = (to.longitude - from.longitude).toRadians()
        val lat1 = from.latitude.toRadians()
        val lat2 = to.latitude.toRadians()

        val a = sin(dLat / 2).let { it * it } +
            sin(dLon / 2).let { it * it } * cos(lat1) * cos(lat2)
        return 2 * EARTH_RADIUS_KM * asin(min(1.0, sqrt(a)))
    }

    /** [center] 에서 [radiusKm] 안에 [target] 이 들어오는지. */
    fun isWithin(center: GeoPoint, target: GeoPoint, radiusKm: Double): Boolean =
        distanceKm(center, target) <= radiusKm

    /**
     * 반경을 감싸는 사각 범위. DB 에서 인덱스를 태워 1차로 걸러낼 때 씁니다.
     *
     * 사각형이라 반경보다 조금 넓게 잡히므로, 통과한 결과는 [distanceKm] 로 한 번 더 걸러야 합니다.
     */
    fun boundingBox(center: GeoPoint, radiusKm: Double): BoundingBox {
        val latDelta = radiusKm / 111.32
        // 위도가 높아질수록 경도 1도의 실제 거리가 짧아지므로 cos 으로 보정합니다.
        val lonDelta = radiusKm / (111.32 * max(0.01, cos(center.latitude.toRadians())))
        return BoundingBox(
            minLat = max(-90.0, center.latitude - latDelta),
            maxLat = min(90.0, center.latitude + latDelta),
            minLon = max(-180.0, center.longitude - lonDelta),
            maxLon = min(180.0, center.longitude + lonDelta),
        )
    }

    private fun Double.toRadians(): Double = this * PI / 180.0
}

data class BoundingBox(
    val minLat: Double,
    val maxLat: Double,
    val minLon: Double,
    val maxLon: Double,
)
