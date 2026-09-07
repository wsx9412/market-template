package com.template.market.domain.usecase

import com.template.market.domain.model.PostQuery
import com.template.market.domain.model.PostWithDistance
import com.template.market.domain.repository.PostRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 검색어 + 품목 + 반경을 한 번에 반영한 목록을 관찰합니다.
 * 화면(홈 / 검색)은 [PostQuery] 만 바꿔 넘기면 되고, 필터 조합 규칙은 여기서 끝납니다.
 */
class ObserveNearbyPostsUseCase @Inject constructor(
    private val repository: PostRepository,
) {
    operator fun invoke(query: PostQuery): Flow<List<PostWithDistance>> =
        repository.observeNearby(query.normalized())

    private fun PostQuery.normalized(): PostQuery = copy(keyword = keyword.trim())
}
