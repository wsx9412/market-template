package com.template.market.domain.geo

import com.template.market.domain.model.GeoPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 반경 필터의 근거가 되는 계산을 검증합니다.
 * 이 계산이 틀리면 "근처 글만 보이기"라는 앱의 핵심 규칙이 통째로 무너지므로 테스트를 붙여 둡니다.
 */
class GeoTest {

    private val seoulCityHall = GeoPoint(37.5665, 126.9780)
    private val gangnamStation = GeoPoint(37.4979, 127.0276)

    @Test
    fun `서울시청과 강남역 거리는 약 8km 대`() {
        val distance = GeoMath.distanceKm(seoulCityHall, gangnamStation)
        assertTrue("실제 계산값=" + distance, distance in 8.0..9.5)
    }

    @Test
    fun `같은 지점의 거리는 0`() {
        assertEquals(0.0, GeoMath.distanceKm(seoulCityHall, seoulCityHall), 0.0001)
    }

    @Test
    fun `반경 안팎 판정`() {
        assertTrue(GeoMath.isWithin(seoulCityHall, gangnamStation, radiusKm = 10.0))
        assertTrue(!GeoMath.isWithin(seoulCityHall, gangnamStation, radiusKm = 5.0))
    }

    @Test
    fun `사각 범위는 반경을 모두 감싼다`() {
        val box = GeoMath.boundingBox(seoulCityHall, radiusKm = 5.0)
        // 반경 5km 안의 점은 반드시 사각 범위 안에도 들어와야 합니다(1차 추림에서 누락되면 안 됨).
        val northOf = GeoPoint(seoulCityHall.latitude + 0.04, seoulCityHall.longitude)
        assertTrue(northOf.latitude in box.minLat..box.maxLat)
        assertTrue(northOf.longitude in box.minLon..box.maxLon)
    }

    @Test
    fun `가까운 두 지점은 geohash prefix 를 공유한다`() {
        val a = GeoHash.encode(seoulCityHall, precision = 5)
        val nearby = GeoPoint(seoulCityHall.latitude + 0.001, seoulCityHall.longitude + 0.001)
        val b = GeoHash.encode(nearby, precision = 5)
        assertEquals(a, b)
    }

    @Test
    fun `먼 두 지점은 geohash 가 다르다`() {
        val seoul = GeoHash.encode(seoulCityHall, precision = 5)
        val busan = GeoHash.encode(GeoPoint(35.1796, 129.0756), precision = 5)
        assertTrue(seoul != busan)
    }

    @Test
    fun `반경을 덮는 prefix 목록에 중심 셀이 포함된다`() {
        val prefixes = GeoHash.coveringPrefixes(seoulCityHall, radiusKm = 3.0)
        val precision = GeoHash.precisionFor(3.0)
        assertTrue(prefixes.contains(GeoHash.encode(seoulCityHall, precision)))
    }
}
