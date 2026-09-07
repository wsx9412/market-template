/**
 * 예산 초과 시 결제를 자동으로 끄는 비상 브레이크.
 *
 * Google Cloud 의 "예산" 기능은 기본적으로 **알려 줄 뿐 막지는 않습니다.**
 * 실제로 지출을 멈추려면 예산 알림을 Pub/Sub 으로 받아, 이 함수가
 * 프로젝트의 결제 계정 연결을 해제해야 합니다. 구글이 안내하는 방식입니다.
 *
 * 걸리면 이렇게 됩니다.
 *  - Cloud Functions 가 멈춥니다 → 채팅 알림이 오지 않습니다
 *  - Firestore / Authentication 은 무료 한도 안에서 계속 동작합니다 → 채팅 자체는 살아 있습니다
 *  - 다시 쓰려면 콘솔에서 결제 계정을 수동으로 다시 연결해야 합니다
 *
 * 한계: 결제 데이터가 실시간이 아니라 몇 시간 늦게 집계됩니다.
 * 정확히 한도에서 끊기지 않고 조금 넘어설 수 있습니다.
 *
 * 설정 방법은 ../README.md 의 "요금 상한 걸기" 를 보세요.
 */

const { onMessagePublished } = require("firebase-functions/v2/pubsub");
const { CloudBillingClient } = require("@google-cloud/billing");

/** 예산 알림을 받을 Pub/Sub 주제 이름. 콘솔에서 예산을 만들 때 지정한 이름과 같아야 합니다. */
const BUDGET_TOPIC = "billing-alerts";

const billing = new CloudBillingClient();

exports.stopBillingOnBudgetExceeded = onMessagePublished(
  {
    topic: BUDGET_TOPIC,
    region: "asia-northeast3",
    maxInstances: 1,
    memory: "256MiB",
    timeoutSeconds: 60,
  },
  async (event) => {
    const notice = event.data.message.json;
    const spent = Number(notice.costAmount ?? 0);
    const budget = Number(notice.budgetAmount ?? 0);
    const projectName = `projects/${process.env.GCLOUD_PROJECT}`;

    if (!budget || spent <= budget) {
      // 예산 이내일 때도 결제 API 를 한 번 읽어 봅니다.
      //
      // 이 브레이크는 평소에 아무 일도 하지 않다가 딱 한 번 동작해야 하는데,
      // 그때 권한이 없어 실패하면 손쓸 방법이 없습니다. 그래서 예산 알림이 올 때마다
      // (50%, 90% 같은 중간 임계값 포함) 결제 API 에 닿는지 미리 확인해 로그를 남깁니다.
      // 여기서 오류가 보이면 실제로 한도를 넘겼을 때도 못 끕니다.
      try {
        const [check] = await billing.getProjectBillingInfo({ name: projectName });
        console.log(
          `예산 이내입니다. ${spent} / ${budget} ` +
            `(결제 API 연결 정상, billingEnabled=${check.billingEnabled})`
        );
      } catch (error) {
        console.error(
          "결제 API 를 읽지 못했습니다. 한도를 넘겨도 결제를 끄지 못합니다. " +
            "서비스 계정에 결제 계정 관리자 역할이 있는지 확인하세요.",
          error
        );
      }
      return;
    }

    const [info] = await billing.getProjectBillingInfo({ name: projectName });

    if (!info.billingEnabled) {
      console.log("이미 결제가 꺼져 있습니다.");
      return;
    }

    // 빈 문자열을 넣으면 결제 계정 연결이 끊어집니다.
    await billing.updateProjectBillingInfo({
      name: projectName,
      projectBillingInfo: { billingAccountName: "" },
    });

    console.warn(
      `예산(${budget})을 넘어(${spent}) 결제를 껐습니다. ` +
        "다시 쓰려면 Google Cloud 콘솔에서 결제 계정을 다시 연결하세요."
    );
  }
);
