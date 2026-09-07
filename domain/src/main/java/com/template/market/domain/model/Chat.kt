package com.template.market.domain.model

/**
 * 게시글 하나에 대해 구매자 한 명과 판매자 사이에 열리는 대화방.
 *
 * 같은 게시글에 같은 구매자가 다시 들어오면 새 방을 만들지 않고 기존 방을 엽니다.
 */
data class ChatRoom(
    val id: String,
    val postId: String,
    val postTitle: String,
    val postThumbnailUrl: String?,
    /** 판매자와 구매자 두 명. */
    val participantIds: List<String>,
    /** id → 표시 이름. 상대 이름을 보여 주려고 방 문서에 같이 저장합니다. */
    val participantNames: Map<String, String>,
    val lastMessage: String,
    val lastMessageAt: Long,
    /** 나에게 안 읽은 메시지 수. */
    val unreadCount: Int,
) {
    fun opponentId(myId: String): String? = participantIds.firstOrNull { it != myId }

    fun opponentName(myId: String): String =
        opponentId(myId)?.let { participantNames[it] } ?: "알 수 없음"
}

data class ChatMessage(
    val id: String,
    val senderId: String,
    val text: String,
    val sentAt: Long,
) {
    fun isMine(myId: String): Boolean = senderId == myId
}
