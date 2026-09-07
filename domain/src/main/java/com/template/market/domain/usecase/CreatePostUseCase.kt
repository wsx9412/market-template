package com.template.market.domain.usecase

import com.template.market.domain.geo.GeoHash
import com.template.market.domain.model.Post
import com.template.market.domain.model.PostDraft
import com.template.market.domain.model.TradeStatus
import com.template.market.domain.repository.ImageStorage
import com.template.market.domain.repository.PostRepository
import java.util.UUID
import javax.inject.Inject

/**
 * 글 등록 절차를 한 곳에 모읍니다.
 *  1) 사진을 백엔드가 읽을 수 있는 영구 주소로 변환
 *  2) 등록 위치의 geohash 계산 (서버 반경 질의용)
 *  3) 저장
 *
 * 어느 백엔드를 쓰든 이 순서는 동일하므로, 구현체가 바뀌어도 이 파일은 그대로입니다.
 */
class CreatePostUseCase @Inject constructor(
    private val repository: PostRepository,
    private val imageStorage: ImageStorage,
) {
    suspend operator fun invoke(draft: PostDraft): Result<Post> {
        if (draft.title.isBlank()) {
            return Result.failure(IllegalArgumentException("제목을 입력해 주세요."))
        }
        if (draft.body.isBlank()) {
            return Result.failure(IllegalArgumentException("자세한 설명을 입력해 주세요."))
        }

        val imageUrls = imageStorage.persist(draft.localImageUris)
            .getOrElse { return Result.failure(it) }

        val post = Post(
            id = UUID.randomUUID().toString(),
            title = draft.title.trim(),
            body = draft.body.trim(),
            price = draft.price.coerceAtLeast(0),
            category = draft.category,
            imageUrls = imageUrls,
            region = draft.region,
            geoHash = GeoHash.encode(draft.region.center),
            sellerId = draft.sellerId,
            sellerName = draft.sellerName.ifBlank { "익명" },
            createdAt = System.currentTimeMillis(),
            status = TradeStatus.ON_SALE,
        )
        return repository.create(post)
    }
}
