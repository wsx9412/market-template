# 서버 연동 규격 (REST 백엔드)

`gradle.properties` 에서 `backend=REST` 로 두면 앱은 이 문서의 규격대로 서버를 호출합니다.
서버가 아래 5개 엔드포인트만 만족하면 앱은 수정 없이 붙습니다.

- Base URL: `gradle.properties` 의 `market.api.baseUrl` (끝에 `/` 필수)
- 요청/응답 본문: `application/json; charset=utf-8`
- 인증: 필요하면 `Authorization: Bearer <token>` 헤더가 실립니다.
  토큰 발급 방식은 앱의 `AuthTokenProvider` 구현에 달려 있고, 기본값은 "토큰 없음"입니다.

---

## 1. 게시글 목록 조회

```
GET /posts
```

| 파라미터 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `keyword` | string | 아니오 | 제목·내용 부분 일치 검색어. 없으면 전체 |
| `category` | string | 아니오 | 품목 키. 아래 품목 표 참고. 없으면 전체 |
| `lat` | number | 아니오 | 사용자가 등록한 동네 중심 위도 |
| `lng` | number | 아니오 | 동네 중심 경도 |
| `radiusKm` | number | 아니오 | 이 거리(km) 안의 글만 |
| `includeSold` | boolean | 아니오 | `false` 면 거래완료 글 제외 |
| `limit` | number | 아니오 | 한 번에 돌려줄 최대 개수 |
| `geoHash` | string 배열 | 아니오 | 반경을 덮는 geohash prefix 목록 (반복 파라미터) |

**반경 처리 방식은 서버가 고르면 됩니다.**

- 위경도 계산이 가능한 DB(PostGIS, MySQL 8 ST_Distance_Sphere 등) → `lat`/`lng`/`radiusKm` 사용
- 계산 함수가 없는 저장소(Firestore, DynamoDB, 단순 RDB) → `geoHash` prefix 로 `LIKE 'xxxx%'` 질의

어느 쪽이든 **넓게 줘도 괜찮습니다.** 앱이 받은 결과에 대해 정확한 거리(Haversine)를 다시 계산해
반경 밖의 글을 잘라내고 가까운 순으로 정렬합니다.

응답:

```json
{
  "items": [
    {
      "id": "0f1c9e2a-...",
      "title": "아이패드 프로 11인치",
      "body": "케이스 씌워 써서 기스 없습니다.",
      "price": 780000,
      "category": "digital",
      "imageUrls": ["https://cdn.example.com/a.jpg"],
      "siDo": "강원특별자치도",
      "siGunGu": "홍천군",
      "eupMyeonDong": "동면",
      "lat": 37.6912,
      "lng": 127.8881,
      "geoHash": "wydq7k2m",
      "sellerId": "3Kq9x...",
      "sellerName": "이웃123",
      "createdAt": 1757130000000,
      "status": "on_sale",
      "chatCount": 3,
      "likeCount": 12,
      "distanceKm": 1.4
    }
  ],
  "nextCursor": null
}
```

- `sellerId` 는 글쓴이의 사용자 식별자입니다. 채팅 상대를 특정하는 값이므로 **저장하고 그대로 돌려주셔야** 합니다.
  (로그인이 설정되지 않은 상태에서 쓴 글은 빈 문자열이고, 그 글에는 채팅을 걸 수 없습니다)
- `createdAt` 은 **epoch milliseconds (Long)** 입니다. ISO 문자열이 아닙니다.
- `distanceKm` 은 선택 항목입니다. 서버가 안 주면 앱이 직접 계산합니다.
- `limit` 은 "더 보기" 방식에 쓰입니다. 앱은 목록 끝에 닿을 때마다 `limit` 을 10 씩 키워 다시 요청합니다.
  커서를 주고받지 않고 상한만 키우는 방식이라, 그 사이 새 글이 올라와도 목록이 어긋나거나 중복되지 않습니다.
  서버는 조건에 맞는 글을 **가까운 순으로 정렬해 앞에서 `limit` 개만** 돌려주면 됩니다.
- `nextCursor` 는 자리만 잡아 둔 필드입니다. 현재 앱은 사용하지 않습니다.
- 모르는 필드가 응답에 섞여 있어도 앱은 무시합니다(`ignoreUnknownKeys = true`).

## 2. 게시글 단건 조회

```
GET /posts/{id}
```

응답: 위 `items` 의 원소와 동일한 객체 하나.
없는 글이면 404 를 주면 되고, 앱은 "삭제되었거나 없는 게시글" 안내를 띄웁니다.

## 3. 게시글 등록

```
POST /posts
```

요청 본문은 응답과 같은 형태의 객체입니다. 주의할 점:

- `id` 는 **앱이 UUID 로 만들어서 보냅니다.** 서버가 자체 ID 를 쓰려면 응답에 새 `id` 를 담아 주세요.
  앱은 응답의 `id` 를 최종값으로 사용합니다.
- `imageUrls` 는 이미 4번 업로드를 마친 URL 목록입니다.
- `geoHash` 는 앱이 계산해 보냅니다(정밀도 8). 서버는 그대로 저장해 두었다가 1번 질의에 쓰면 됩니다.

응답: 저장된 게시글 객체.

## 4. 이미지 업로드

```
POST /images
Content-Type: multipart/form-data
```

- 파트 이름: `files` (여러 개 반복)
- 각 파트는 `image/jpeg`

응답:

```json
{ "urls": ["https://cdn.example.com/a.jpg", "https://cdn.example.com/b.jpg"] }
```

**`urls` 의 순서는 보낸 파일 순서와 같아야 합니다.** 앱이 첫 번째 URL 을 목록 썸네일로 씁니다.

## 5. 게시글 삭제

```
DELETE /posts/{id}
```

본문 없이 2xx 면 성공으로 봅니다.

---

## 품목(category) 키

키는 저장용 식별자라 바뀌면 안 됩니다. 화면에 보이는 이름만 앱에서 바꿀 수 있습니다.

| 키 | 표시 이름 |
|---|---|
| `digital` | 디지털기기 |
| `appliance` | 생활가전 |
| `furniture` | 가구/인테리어 |
| `kids` | 유아동 |
| `clothes` | 의류 |
| `book` | 도서 |
| `sports` | 스포츠/레저 |
| `hobby` | 취미/게임/음반 |
| `beauty` | 뷰티/미용 |
| `pet` | 반려동물용품 |
| `plant` | 식물 |
| `kitchen` | 생활/주방 |
| `car` | 자동차/공구 |
| `ticket` | 티켓/교환권 |
| `etc` | 기타 중고물품 |

## 거래 상태(status) 키

| 키 | 표시 이름 |
|---|---|
| `on_sale` | 판매중 |
| `reserved` | 예약중 |
| `sold` | 거래완료 |

---

## 경로·필드 이름이 다를 때

이미 정해진 서버 규격이 있다면 서버를 고칠 필요 없이 앱 쪽 두 파일만 바꾸면 됩니다.

- 경로·쿼리 파라미터: `data-remote/.../api/PostApi.kt`
- 필드 이름: `data-remote/.../api/PostDto.kt` 의 `@SerialName`

화면, 도메인, 로컬 백엔드는 전혀 영향을 받지 않습니다.
