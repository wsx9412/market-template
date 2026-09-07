package com.template.market.domain.model

/** 등록이 끝난 중고거래 게시글. */
data class Post(
    val id: String,
    val title: String,
    val body: String,
    val price: Long,
    val category: Category,
    /** 백엔드가 돌려준 이미지 주소. 로컬 백엔드면 file:// , 서버 백엔드면 https:// 가 됩니다. */
    val imageUrls: List<String>,
    val region: Region,
    /** 반경 검색을 서버에서 처리할 때 쓰는 geohash. 클라이언트는 [com.template.market.domain.geo.GeoHash] 로 채웁니다. */
    val geoHash: String,
    /**
     * 판매자의 사용자 식별자. 채팅 상대를 특정하는 값입니다.
     * 로그인 수단이 설정되지 않은 상태에서 쓴 글은 비어 있고, 그 글에는 채팅을 걸 수 없습니다.
     */
    val sellerId: String,
    val sellerName: String,
    val createdAt: Long,
    val status: TradeStatus = TradeStatus.ON_SALE,
    val chatCount: Int = 0,
    val likeCount: Int = 0,
) {
    val thumbnailUrl: String? get() = imageUrls.firstOrNull()
}

/** 아직 저장되지 않은 작성 중인 글. 이미지는 기기 로컬 URI 상태입니다. */
data class PostDraft(
    val title: String,
    val body: String,
    val price: Long,
    val category: Category,
    /** 사진 선택기에서 받은 content:// URI 문자열. 저장 시 [com.template.market.domain.repository.ImageStorage] 가 변환합니다. */
    val localImageUris: List<String>,
    val region: Region,
    val sellerId: String,
    val sellerName: String,
)

/** 목록에 뿌릴 때 쓰는, 내 위치로부터의 거리가 붙은 글. */
data class PostWithDistance(
    val post: Post,
    val distanceKm: Double,
)
