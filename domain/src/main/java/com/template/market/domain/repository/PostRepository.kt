package com.template.market.domain.repository

import com.template.market.domain.model.Post
import com.template.market.domain.model.PostDraft
import com.template.market.domain.model.PostQuery
import com.template.market.domain.model.PostWithDistance
import kotlinx.coroutines.flow.Flow

/**
 * ★ 백엔드 교체 지점 ★
 *
 * 게시글 저장소 계약. 이 인터페이스만 만족하면 어떤 백엔드든 붙습니다.
 * 현재 구현체:
 *  - `:data-local`  RoomPostRepository  (기기 내부 DB, 서버 불필요)
 *  - `:data-remote` RestPostRepository  (REST 서버)
 * 새 백엔드(Firebase 등)를 붙일 때는 이 파일을 고치지 말고 구현체 모듈만 추가하세요.
 */
interface PostRepository {

    /**
     * 조건에 맞는 글을 가까운 순으로 흘려보냅니다.
     *
     * 구현체는 [PostQuery.center] 와 [PostQuery.radiusKm] 를 반드시 반영해야 하며,
     * 거리 계산 결과를 [PostWithDistance.distanceKm] 에 담아야 합니다.
     */
    fun observeNearby(query: PostQuery): Flow<List<PostWithDistance>>

    /** 상세 화면용 단건 조회. 없으면 null. */
    suspend fun getById(id: String): Post?

    /**
     * 새 글 등록. 이미지는 이미 [ImageStorage] 를 거쳐 영구 URL 이 된 상태로 들어옵니다.
     * geoHash 채우기는 구현체가 아니라 UseCase 가 담당합니다.
     */
    suspend fun create(post: Post): Result<Post>

    suspend fun delete(id: String): Result<Unit>

    /** 데모/개발용 시드 데이터 주입. 서버 백엔드에서는 아무것도 하지 않아도 됩니다. */
    suspend fun seedIfEmpty(around: com.template.market.domain.model.Region) {}
}

/**
 * ★ 백엔드 교체 지점 ★
 *
 * 사진 저장 계약. 사진 선택기에서 받은 기기 로컬 URI 를,
 * 그 백엔드가 나중에 다시 읽을 수 있는 영구 주소로 바꿔 돌려줍니다.
 *  - 로컬 구현 : 앱 내부 저장소로 복사하고 file:// 경로 반환
 *  - 서버 구현 : multipart 업로드 후 https:// URL 반환
 */
interface ImageStorage {
    suspend fun persist(localUris: List<String>): Result<List<String>>
}
