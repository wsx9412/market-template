package com.template.market.domain.repository

import com.template.market.domain.model.Region
import kotlinx.coroutines.flow.Flow

/** 사용자가 "등록해 둔 내 동네"와 노출 반경 설정. */
interface UserPreferences {
    /** 등록된 동네. 아직 등록 전이면 null 을 흘립니다. */
    val myRegion: Flow<Region?>

    /** 글을 보여줄 반경(km). */
    val radiusKm: Flow<Double>

    val nickname: Flow<String>

    suspend fun setMyRegion(region: Region)
    suspend fun setRadiusKm(radiusKm: Double)
    suspend fun setNickname(nickname: String)
}
