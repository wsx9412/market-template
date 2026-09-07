package com.template.market.domain.usecase

import com.template.market.domain.location.LocationSource
import com.template.market.domain.location.RegionResolver
import com.template.market.domain.model.Region
import com.template.market.domain.repository.UserPreferences
import javax.inject.Inject

/**
 * GPS 를 잡아 동/읍/면까지 알아내고, 그 결과를 "내 동네"로 등록합니다.
 * 위치 권한 요청은 UI 의 책임이고, 여기서는 이미 권한이 있다고 가정합니다.
 */
class ResolveMyRegionUseCase @Inject constructor(
    private val locationSource: LocationSource,
    private val regionResolver: RegionResolver,
    private val preferences: UserPreferences,
) {
    suspend operator fun invoke(persist: Boolean = true): Result<Region> {
        val point = locationSource.currentLocation()
            ?: return Result.failure(IllegalStateException("현재 위치를 가져오지 못했습니다. 위치 권한과 GPS 를 확인해 주세요."))

        val region = regionResolver.resolve(point)
            ?: return Result.failure(IllegalStateException("좌표에 해당하는 동네 이름을 찾지 못했습니다."))

        if (persist) preferences.setMyRegion(region)
        return Result.success(region)
    }
}
