package com.template.market.domain.usecase

import com.template.market.domain.repository.AuthRepository
import com.template.market.domain.repository.ChatRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SendChatMessageUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(roomId: String, text: String): Result<Unit> {
        val me = authRepository.currentUser.first()
            ?: return Result.failure(IllegalStateException("로그인이 필요합니다."))
        return chatRepository.send(roomId, me, text)
    }
}
