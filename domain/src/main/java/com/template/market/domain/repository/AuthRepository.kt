package com.template.market.domain.repository

import com.template.market.domain.model.AppUser
import kotlinx.coroutines.flow.Flow

/**
 * ★ 교체 지점 ★
 *
 * 로그인 계약. 현재 구현은 Firebase Authentication 이지만,
 * 자체 회원 시스템으로 바꿀 때도 이 인터페이스만 만족시키면 화면은 그대로입니다.
 *
 * [isEnabled] 가 false 면 로그인 수단이 설정되지 않은 상태입니다.
 * (Firebase 구현에서는 google-services.json 이 없는 경우)
 * 이때 앱은 로그인이 필요한 기능만 막고 나머지는 정상 동작해야 합니다.
 */
interface AuthRepository {

    val isEnabled: Boolean

    /** 로그인한 사용자. 로그인 전이면 null 을 흘립니다. */
    val currentUser: Flow<AppUser?>

    /**
     * 별도 가입 절차 없이 사용자 식별자만 발급받습니다.
     * 채팅이 성립하려면 최소한 이것은 있어야 하므로 앱 시작 시 자동으로 호출합니다.
     */
    suspend fun signInAnonymously(): Result<AppUser>

    /** Google 계정 연결. UI 에서 받아 온 ID 토큰을 넘깁니다. */
    suspend fun signInWithGoogle(idToken: String): Result<AppUser>

    suspend fun updateDisplayName(name: String): Result<Unit>

    suspend fun signOut()
}
