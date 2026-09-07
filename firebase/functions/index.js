/**
 * 채팅 푸시 알림 발송.
 *
 * 알림을 앱에서 직접 보내지 않는 이유:
 * 발송에는 서버 권한이 필요하고, 그 권한을 앱에 넣으면 누구나 다른 사용자에게
 * 알림을 보낼 수 있게 됩니다. 그래서 서버(이 함수)가 발송을 맡습니다.
 *
 * 배포:
 *   cd firebase
 *   firebase init functions      # 이미 되어 있으면 생략
 *   firebase deploy --only functions
 *
 * 동작:
 *   chatRooms/{roomId}/messages 에 새 문서가 생기면 실행되어,
 *   방의 상대방 사용자 문서에 저장된 pushToken 으로 알림을 보냅니다.
 */

const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");

initializeApp();

const db = getFirestore();

/**
 * 함수를 배포할 지역.
 *
 * Firestore 트리거는 **데이터베이스와 같은 지역**이어야 합니다.
 * 콘솔에서 Firestore 를 만들 때 고른 위치와 다르면 배포가 실패합니다.
 * (서울로 만들었다면 asia-northeast3, 미국 중부면 us-central1)
 */
const REGION = "asia-northeast3";

/**
 * 비용 안전장치.
 *
 * Blaze 요금제는 사용한 만큼 과금되므로, 예상치 못한 폭주를 막기 위해
 * 동시 실행 개수를 제한합니다. 알림 발송은 가볍고 급하지 않은 작업이라
 * 몇 개로 묶어도 체감 지연이 없고, 잘못된 코드나 무한 루프가 생겨도
 * 요금이 무한정 늘어나지 않습니다.
 */
const COST_LIMITS = {
  maxInstances: 5,
  memory: "256MiB",
  timeoutSeconds: 30,
};

exports.onChatMessageCreated = onDocumentCreated(
  {
    document: "chatRooms/{roomId}/messages/{messageId}",
    region: REGION,
    ...COST_LIMITS,
  },
  async (event) => {
    const message = event.data?.data();
    if (!message) return;

    const roomId = event.params.roomId;
    const roomSnapshot = await db.collection("chatRooms").doc(roomId).get();
    if (!roomSnapshot.exists) return;

    const room = roomSnapshot.data();
    const participants = room.participantIds || [];

    // 보낸 사람 본인에게는 알림을 보내지 않습니다.
    const recipientId = participants.find((id) => id !== message.senderId);
    if (!recipientId) return;

    const recipientSnapshot = await db.collection("users").doc(recipientId).get();
    const pushToken = recipientSnapshot.exists ? recipientSnapshot.data().pushToken : null;
    if (!pushToken) return;

    const senderName = (room.participantNames || {})[message.senderId] || "이웃";

    try {
      await getMessaging().send({
        token: pushToken,
        notification: {
          title: senderName,
          body: message.text || "새 메시지가 도착했습니다.",
        },
        data: {
          roomId: roomId,
        },
        android: {
          priority: "high",
        },
      });
    } catch (error) {
      // 앱을 지웠거나 토큰이 만료되면 발송이 실패합니다.
      // 죽은 토큰을 지워 두어야 다음부터 헛수고를 하지 않습니다.
      const code = error && error.errorInfo ? error.errorInfo.code : "";
      if (
        code === "messaging/registration-token-not-registered" ||
        code === "messaging/invalid-registration-token"
      ) {
        await db.collection("users").doc(recipientId).update({ pushToken: null });
      } else {
        console.error("푸시 발송 실패", error);
      }
    }
  }
);

// 예산 초과 시 결제를 끄는 비상 브레이크. 설정은 ../README.md 참고.
exports.stopBillingOnBudgetExceeded =
  require("./stopBilling").stopBillingOnBudgetExceeded;
