package com.template.market.domain.repository

import com.template.market.domain.model.AppUser
import com.template.market.domain.model.ChatMessage
import com.template.market.domain.model.ChatRoom
import com.template.market.domain.model.Post
import kotlinx.coroutines.flow.Flow

/**
 * ★ 교체 지점 ★
 *
 * 채팅 계약. 현재 구현은 Firestore 실시간 구독입니다.
 *
 * 게시글 저장소(PostRepository)와 달리 채팅은 "실시간 수신"이 필수라
 * 단순 REST 폴링으로는 대체할 수 없습니다. 자체 서버로 옮긴다면
 * WebSocket 이나 SSE 구현체를 이 인터페이스에 맞춰 새로 만들어야 합니다.
 */
interface ChatRepository {

    val isEnabled: Boolean

    /** 내가 참여한 방 목록. 최근 대화 순. */
    fun observeRooms(userId: String): Flow<List<ChatRoom>>

    /** 방 하나의 정보(상대 이름, 게시글 제목 등). 없으면 null. */
    fun observeRoom(roomId: String): Flow<ChatRoom?>

    /** 방 하나의 메시지. 오래된 것부터. */
    fun observeMessages(roomId: String): Flow<List<ChatMessage>>

    /**
     * 이 게시글에 대한 나와 판매자의 방을 열고 방 id 를 돌려줍니다.
     * 이미 있으면 그 방을, 없으면 새로 만듭니다.
     */
    suspend fun openRoom(post: Post, me: AppUser): Result<String>

    suspend fun send(roomId: String, sender: AppUser, text: String): Result<Unit>

    /** 방에 들어갔을 때 내 안 읽은 수를 0 으로 만듭니다. */
    suspend fun markRead(roomId: String, userId: String)

    /** 푸시 알림 수신용 기기 토큰 등록. 서버가 이 값으로 알림을 보냅니다. */
    suspend fun registerPushToken(userId: String, token: String)

    /** 이 기기의 알림 토큰을 발급받아 등록합니다. 로그인 직후 한 번 호출합니다. */
    suspend fun syncPushToken(userId: String)
}
