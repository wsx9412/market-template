package com.template.market.domain.model

/**
 * 게시글 조회 조건. 검색·품목 필터·반경 필터가 하나로 합쳐진 값입니다.
 *
 * 백엔드 구현체는 이 조건을 각자의 방식으로 번역합니다.
 * - Room  : SQL WHERE + 앱 단 거리 계산
 * - REST  : 쿼리 파라미터
 * - 그 외 : geohash prefix 범위 질의 등
 */
data class PostQuery(
    /** 제목·내용에서 찾을 검색어. 비어 있으면 전체. */
    val keyword: String = "",
    /** 품목 필터. null 이면 전체 품목. */
    val category: Category? = null,
    /** 내 현재 위치(또는 등록해 둔 동네의 중심). null 이면 거리 필터를 적용하지 않습니다. */
    val center: GeoPoint? = null,
    /** 이 거리(km) 안의 글만 보여줍니다. */
    val radiusKm: Double = DEFAULT_RADIUS_KM,
    val includeSold: Boolean = true,
    /**
     * 한 번에 가져올 최대 개수.
     *
     * 목록을 끝까지 내리면 화면이 이 값을 늘려 다시 조회합니다.
     * "다음 페이지"를 따로 요청하지 않고 상한만 늘리는 방식이라,
     * 새 글이 중간에 끼어들어도 목록이 어긋나거나 중복되지 않습니다.
     */
    val limit: Int = PAGE_SIZE,
) {
    companion object {
        const val DEFAULT_RADIUS_KM = 5.0

        /**
         * 한 번에 더 불러오는 개수.
         *
         * 목록 한 줄이 사진을 포함해 꽤 높아서 화면에 6~7건이 들어옵니다.
         * 한 화면보다 조금 넉넉한 정도로 잡아, 끝에 닿기 전에 다음 묶음이 준비되게 했습니다.
         */
        const val PAGE_SIZE = 10

        /** 화면에서 고를 수 있는 반경 선택지. */
        val RADIUS_OPTIONS = listOf(1.0, 3.0, 5.0, 10.0, 20.0)
    }
}
