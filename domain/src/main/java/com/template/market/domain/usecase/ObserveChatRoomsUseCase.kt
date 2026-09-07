package com.template.market.domain.usecase

import com.template.market.domain.model.ChatRoom
import com.template.market.domain.repository.AuthRepository
import com.template.market.domain.repository.ChatRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

/** 로그인한 사용자가 바뀌면 구독도 따라 바뀝니다. */
class ObserveChatRoomsUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<List<ChatRoom>> =
        authRepository.currentUser.flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else chatRepository.observeRooms(user.id)
        }
}
