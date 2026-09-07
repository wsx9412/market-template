package com.template.market.domain.usecase

import com.template.market.domain.model.AppUser
import com.template.market.domain.repository.AuthRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * 앱을 열 때 사용자 식별자를 확보합니다.
 *
 * 채팅은 "누가 누구에게"가 정해져야 성립하는데, 가입 화면을 먼저 띄우면
 * 그냥 둘러보려던 사용자까지 막게 됩니다. 그래서 가입 절차 없는 익명 로그인으로 식별자만 만들어 두고,
 * 정식 계정(Google) 연결은 사용자가 원할 때 하도록 미룹니다.
 *
 * 로그인 수단이 설정되지 않았으면(Firebase 미설정) 아무 일도 하지 않고 null 을 돌려줍니다.
 */
class EnsureSignedInUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): AppUser? {
        if (!authRepository.isEnabled) return null
        authRepository.currentUser.first()?.let { return it }
        return authRepository.signInAnonymously().getOrNull()
    }
}
