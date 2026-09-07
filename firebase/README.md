# 서버 측 설정 (Firebase)

앱의 로그인·채팅을 켜려면 아래 4단계가 필요합니다.

## 1. 프로젝트 만들기

1. [Firebase 콘솔](https://console.firebase.google.com) 에서 프로젝트 생성
2. Android 앱 추가 — 패키지 이름은 `app/build.gradle.kts` 의 `applicationId` 와 같아야 합니다 (기본값 `com.template.market`)
3. 받은 **`google-services.json` 을 `app/` 폴더에 넣습니다**

이 파일이 있으면 로그인·채팅이 자동으로 켜집니다. 없으면 그 두 기능만 꺼진 채로 앱이 동작합니다.

## 2. 로그인 방법 켜기

콘솔 > Authentication > Sign-in method 에서

- **익명** — 필수. 앱이 가입 절차 없이 사용자 식별자를 발급받는 데 씁니다.
- **Google** — 선택. 켜는 경우 콘솔의 웹 클라이언트 ID(`client_type: 3`)를
  `gradle.properties` 의 `market.google.webClientId` 에 넣어야 버튼이 나타납니다.
  기기 지문(SHA-1)도 콘솔에 등록해야 합니다.

## 3. 데이터베이스와 보안 규칙

콘솔 > Firestore Database 를 만든 뒤, **반드시 `firestore.rules` 의 내용을 적용**하세요.
어느 모드로 만들든 기본 규칙은 그대로 쓸 수 없습니다.

| 만들 때 고른 모드 | 기본 규칙 | 그대로 두면 |
|---|---|---|
| 프로덕션 모드 | `allow read, write: if false;` | 앱이 아무것도 읽고 쓰지 못합니다 |
| 테스트 모드 | 기한부 전체 허용 | 누구나 남의 대화를 볼 수 있고, 기한이 지나면 전부 막힙니다 |

적용 방법은 둘 중 하나입니다.

- **콘솔** — Firestore Database > 규칙 탭에서 기존 내용을 **전부 지우고** `firestore.rules` 내용을 붙여 넣은 뒤 "게시"
- **CLI** — `firebase deploy --only firestore:rules`

이 규칙은 "방에 참여한 두 사람만 그 방과 메시지를 읽고 쓸 수 있다"를 강제하고,
메시지의 `senderId` 를 남의 것으로 위조하지 못하게 막습니다.

## 4. 푸시 알림

알림 발송은 앱이 아니라 서버가 합니다. 발송 권한을 앱에 넣으면
누구나 다른 사용자에게 알림을 보낼 수 있게 되기 때문입니다.

**이 단계를 건너뛰어도 채팅은 정상 동작합니다.** 앱이 꺼져 있을 때 알림만 오지 않습니다.

### 준비

| 항목 | 확인 |
|---|---|
| Node.js 20 이상 | `node -v` |
| Firebase CLI | `npm install -g firebase-tools` |
| 로그인 | `firebase login` |
| **요금제** | **Blaze(종량제) 필요.** 무료 Spark 요금제에서는 Cloud Functions 를 배포할 수 없습니다 |

Blaze 는 종량제라 **결제 수단 등록을 피할 수 없습니다.** 요금이 부담되신다면
아래 "요금 없이 알림 보내기" 를 먼저 읽어 보세요.

### 배포

명령은 **이 `firebase/` 폴더 안에서** 실행합니다. 여기에 `firebase.json` 과 `.firebaserc` 가 있어
어느 프로젝트에 무엇을 올릴지 CLI 가 알아냅니다.

```bash
cd firebase
npm --prefix functions install
firebase deploy --only functions
```

보안 규칙도 같은 자리에서 올릴 수 있습니다.

```bash
firebase deploy --only firestore:rules
```

### 다른 Firebase 프로젝트에 올릴 때

`.firebaserc` 의 프로젝트 이름을 바꾸거나, 아래 명령으로 전환합니다.

```bash
firebase use --add
```

### 지역을 맞춰야 합니다

`functions/index.js` 위쪽의 `REGION` 값은 **Firestore 를 만들 때 고른 위치와 같아야** 합니다.
다르면 배포가 실패합니다. 기본값은 서울(`asia-northeast3`)입니다.

### 배포 후 확인

Firebase 콘솔 > Functions 에 `onChatMessageCreated` 가 보이면 완료입니다.
메시지를 보냈을 때 상대에게 알림이 가지 않는다면 콘솔의 함수 로그를 먼저 확인하세요.
받는 사람의 `users/{userId}` 문서에 `pushToken` 이 없으면 알림을 보낼 대상이 없는 상태입니다.

---

## 5. 요금 없이 알림 보내기

**알림 서비스(FCM) 자체는 무료입니다.** 요금이 붙는 건 Cloud Functions 뿐입니다.
그래서 "메시지가 생겼을 때 알림을 쏘는 주체" 를 다른 곳으로 옮기면 Blaze 가 필요 없습니다.

### 방법 A — 귀사 서버에서 발송 (권장)

게시글용 서버(REST)를 이미 운영하신다면, 그 서버가 알림도 보내면 됩니다.

1. Firebase 콘솔 > 프로젝트 설정 > 서비스 계정에서 **비공개 키(JSON)** 를 내려받아 **서버에만** 둡니다
2. 앱이 메시지를 보낼 때 서버에도 알려 주도록 연동합니다
3. 서버가 받는 사람의 `users/{userId}.pushToken` 값으로 FCM HTTP v1 API 를 호출합니다

이 키는 **절대 앱에 넣으면 안 됩니다.** 키를 가진 쪽은 누구에게든 알림을 보낼 수 있습니다.

### 방법 B — 알림을 쓰지 않음

**채팅은 알림 없이도 완전히 동작합니다.** 앱을 켜 두고 있으면 메시지가 실시간으로 도착하고,
안 읽은 개수도 정상적으로 표시됩니다. 앱이 꺼져 있을 때 알려 주지 못할 뿐입니다.
검토 단계나 소규모 시범 운영에서는 이 상태로 충분합니다.

### 방법 C — Blaze 를 쓰되 상한을 건다

결제 수단은 등록하되, 지출이 정해 둔 금액을 넘으면 **결제를 자동으로 꺼서** 멈춥니다.
아래 6번 항목에 설정 방법이 있습니다.

정확한 무료 사용량과 단가는 요금제 페이지에서 확인하세요:
<https://firebase.google.com/pricing>

---

## 6. 요금 상한 걸기 (기본 $3)

Google Cloud 의 예산 기능은 기본적으로 **알려 줄 뿐 막지 않습니다.**
실제로 멈추려면 예산 알림을 함수로 받아 결제를 꺼야 하고, 그 함수를
`functions/stopBilling.js` 에 넣어 두었습니다.

### 이미 되어 있는 것

- 채팅 알림 함수의 동시 실행 5개 제한 (`functions/index.js` 의 `COST_LIMITS`)
- 예산 초과 시 결제를 끄는 함수 (`functions/stopBilling.js`)

### 해 주셔야 하는 것

**1) Pub/Sub 주제 만들기**

Google Cloud 콘솔 > Pub/Sub > 주제 만들기 → 이름을 **`billing-alerts`** 로 지정합니다.
(다른 이름을 쓰시려면 `functions/stopBilling.js` 의 `BUDGET_TOPIC` 도 같이 바꾸세요)

**2) 예산 만들기**

Google Cloud 콘솔 > **결제** > 결제 계정 선택 > 왼쪽 메뉴 **예산 및 알림** > **예산 만들기**

결제 계정 ID 를 안다면 바로 갈 수 있습니다.

```
https://console.cloud.google.com/billing/<결제계정ID>/budgets
```

화면이 3~4단계로 나뉩니다.

*1단계 · 범위*

| 항목 | 값 |
|---|---|
| 이름 | 아무 이름 (예: `직거래 한도`) |
| 프로젝트 | **이 프로젝트만 선택** (비워 두면 결제 계정 전체가 대상이 됩니다) |
| 서비스 | **모든 서비스** (건드리지 마세요) |

서비스를 하나만 고르면 **그 서비스 요금만** 예산에 합산됩니다.
예를 들어 Cloud Run Functions 만 고르면 Firestore·네트워크 요금은 한도 계산에서 빠져,
실제 청구액이 한도를 넘어도 브레이크가 걸리지 않습니다.

*2단계 · 금액*

| 항목 | 값 |
|---|---|
| 예산 유형 | 지정된 금액 |
| 대상 금액 | **3** |

> **통화를 반드시 확인하세요.** 금액 칸에는 통화 기호가 잘 보이지 않아 실수하기 쉽습니다.
> 결제 계정이 원화(KRW)인데 "3" 을 넣으면 **3달러가 아니라 3원** 이 됩니다.
> 그러면 지출이 조금만 생겨도 즉시 한도를 넘겨 결제가 꺼집니다.

통화는 아래로 확인합니다.

```bash
gcloud billing accounts describe <결제계정ID> --format="value(currencyCode)"
```

원화 계정이라면 $3 에 해당하는 금액(대략 **4,000원**)으로 잡으세요.
정확한 환산보다 "이 선을 넘으면 멈춘다"는 기준이 중요합니다.

### 명령으로 만들기 (콘솔 UI 가 헷갈릴 때)

콘솔에는 "예산" 과 "지출 한도" 가 섞여 있어 원하는 화면을 찾기 어려울 수 있습니다.
지출 한도 쪽으로 들어가면 서비스를 하나만 골라야 해서 전체 예산을 만들 수 없습니다.
그럴 때는 명령 한 줄이 확실합니다.

```bash
gcloud billing budgets create   --billing-account=<결제계정ID>   --display-name="프로젝트 한도"   --budget-amount=4000KRW   --filter-projects=projects/<프로젝트id>   --threshold-rule=percent=0.5   --threshold-rule=percent=0.9   --threshold-rule=percent=1.0   --notifications-rule-pubsub-topic=projects/<프로젝트id>/topics/billing-alerts
```

만든 뒤 확인:

```bash
gcloud billing budgets list --billing-account=<결제계정ID>
```

*3단계 · 작업(알림)*

| 항목 | 값 |
|---|---|
| 임계값 | 50% / 90% / 100% (기본값 그대로 두면 됩니다) |
| **이 예산에 Pub/Sub 주제 연결** | **체크** |
| 프로젝트 | `carrottemplate` 등 함수를 배포한 프로젝트 |
| 주제 | **`billing-alerts`** |

Pub/Sub 항목은 접혀 있는 경우가 많습니다. **"알림 관리 옵션"** 같은 펼치기를 눌러야 나옵니다.

**이 체크를 켜지 않으면 이메일 알림만 오고 함수는 호출되지 않습니다.**
즉 상한이 걸리지 않습니다. 이 항목 하나가 브레이크의 전부입니다.

저장한 뒤에는 위 "설정이 제대로 됐는지 확인하기" 로 배선을 점검하세요.

**3) 결제 API 와 권한 열기**

Cloud Billing API 를 사용 설정한 뒤, 함수가 쓰는 서비스 계정에 **결제 계정 관리자** 역할을 줍니다.

*어떤 계정인지 확인하는 법*

2세대 함수는 **Compute Engine 기본 서비스 계정**으로 실행됩니다.

```
<프로젝트번호>-compute@developer.gserviceaccount.com
```

프로젝트 번호는 `google-services.json` 의 `project_number` 값이고,
확실하게 보려면 아래 명령의 출력에서 확인할 수 있습니다.

```bash
firebase functions:list --project <프로젝트id> --debug | grep serviceAccountEmail
```

*어디에서 주는지가 중요합니다*

**결제 계정 관리자는 프로젝트가 아니라 "결제 계정" 에 주는 역할입니다.**
프로젝트의 IAM 화면에서는 목록에 나오지 않아 찾을 수 없습니다.

화면이 잘 안 보이는 편이라 경로를 정확히 적습니다.

1. <https://console.cloud.google.com/billing> 에서 **결제 계정** 선택
   (프로젝트의 "결제" 메뉴가 아닙니다. 그쪽에는 권한 화면이 없습니다)
2. 왼쪽 메뉴 **계정 관리(Account management)** 열기
3. 화면 **오른쪽 위 "정보 패널 표시"(SHOW INFO PANEL)** 를 누릅니다
   → 여기를 안 누르면 권한 화면이 아예 나타나지 않습니다
4. 오른쪽에 열린 패널에서 **주 구성원 추가** → 서비스 계정 주소 붙여넣기
5. 역할 **결제 계정 관리자 (Billing Account Administrator)** 선택 후 저장

결제 계정 ID 를 알고 있다면 아래 주소로 바로 갈 수 있습니다.

```
https://console.cloud.google.com/billing/<결제계정ID>/manage
```

**"권한" 이나 "정보 패널 표시" 가 아예 없다면** 지금 로그인한 계정에
그 결제 계정의 관리자 권한이 없는 것입니다.
결제 계정을 만든 사람에게 요청하거나, 그 계정으로 로그인해서 진행하세요.

프로젝트 IAM 목록에서 서비스 계정 자체가 안 보이는 경우는
목록 오른쪽 위의 **"Google 제공 역할 부여 포함"** 을 켜면 나타납니다.

권한이 없으면 예산을 넘겨도 결제를 끄지 못하고 함수 로그에 오류만 남습니다.
설정을 마친 뒤 함수 로그에서 실제로 동작하는지 확인해 두는 편이 안전합니다.

**4) 배포**

```bash
cd firebase
npm --prefix functions install
firebase deploy --only functions
```

### 설정이 제대로 됐는지 확인하기

실제로 한도를 넘겨 보는 건 결제가 꺼지므로 곤란합니다.
대신 **한도 이내인 가짜 알림**을 보내 배선만 확인할 수 있습니다.

1. Google Cloud 콘솔 > **Pub/Sub** > 주제 **`billing-alerts`** 선택
2. **메시지 게시** 를 누르고 본문에 아래를 넣습니다

```json
{"costAmount": 0.1, "budgetAmount": 3, "budgetDisplayName": "test"}
```

3. Firebase 콘솔 > **Functions** > `stopBillingOnBudgetExceeded` 로그 확인

기대하는 로그:

```
예산 이내입니다. 0.1 / 3 (결제 API 연결 정상, billingEnabled=true)
```

이 줄이 보이면 **예산 → 토픽 → 함수 → 결제 API** 까지 모두 연결된 것입니다.
반대로 아래가 보이면 서비스 계정에 결제 계정 관리자 역할이 빠진 것입니다.

```
결제 API 를 읽지 못했습니다. 한도를 넘겨도 결제를 끄지 못합니다.
```

한도 이내 값을 보냈으므로 **결제는 꺼지지 않습니다.** 안심하고 눌러도 됩니다.

같은 점검이 실제 예산 알림(50%, 90% 임계값)이 올 때마다 자동으로 실행되므로,
평소 로그만 봐도 브레이크가 살아 있는지 알 수 있습니다.

### 걸리면 어떻게 되나

- Cloud Functions 가 멈춥니다 → **채팅 알림이 오지 않습니다**
- Firestore / Authentication 은 무료 한도 안에서 계속 동작합니다 → **채팅 자체는 살아 있습니다**
- 다시 쓰려면 콘솔에서 **결제 계정을 수동으로 다시 연결**해야 합니다

### 한계 (중요)

결제 데이터는 실시간이 아니라 몇 시간 늦게 집계됩니다.
그래서 정확히 $3 에서 끊기지 않고 **조금 넘어설 수 있습니다.**
"절대 $3 을 넘지 않는다" 는 보장은 Blaze 요금제에 존재하지 않습니다.
금액이 한 푼도 나가면 안 되는 상황이라면 방법 A 나 B 를 쓰세요.

---

## 저장되는 데이터

```
users/{userId}
  displayName, isAnonymous, pushToken, updatedAt

chatRooms/{게시글id_구매자id}
  postId, postTitle, postThumbnailUrl,
  sellerId, buyerId,
  participantIds: [판매자, 구매자],
  participantNames: { id: 이름 },
  lastMessage, lastMessageAt,
  unread: { 사용자id: 안읽은수 }

chatRooms/{roomId}/messages/{messageId}
  senderId, text, sentAt
```

방 문서 id 를 `게시글id_구매자id` 로 고정했기 때문에,
같은 글에 같은 사람이 다시 들어와도 방이 새로 생기지 않습니다.
