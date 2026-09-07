package com.template.market.domain.model

/**
 * 로그인한 사용자.
 *
 * [id] 는 채팅에서 "누가 누구에게"를 정하는 유일한 근거입니다.
 * 표시 이름은 바뀔 수 있지만 id 는 바뀌지 않습니다.
 */
data class AppUser(
    val id: String,
    val displayName: String,
    /** 익명 로그인 여부. 익명 계정은 앱을 지우면 복구되지 않습니다. */
    val isAnonymous: Boolean,
)
