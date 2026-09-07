package com.template.market.domain.usecase

import com.template.market.domain.model.Post
import com.template.market.domain.repository.AuthRepository
import com.template.market.domain.repository.ChatRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * 게시글에서 "채팅하기" 를 눌렀을 때의 절차를 한곳에 모읍니다.
 *  1) 로그인 여부 확인 (없으면 익명으로 확보)
 *  2) 이 글 + 나에 해당하는 방을 열거나 이미 있던 방을 찾음
 */
class OpenChatRoomUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
    private val ensureSignedIn: EnsureSignedInUseCase,
) {
    suspend operator fun invoke(post: Post): Result<String> {
        if (!chatRepository.isEnabled) {
            return Result.failure(IllegalStateException("채팅 기능이 설정되어 있지 않습니다."))
        }
        val me = authRepository.currentUser.first()
            ?: ensureSignedIn()
            ?: return Result.failure(IllegalStateException("로그인이 필요합니다."))

        return chatRepository.openRoom(post, me)
    }
}
