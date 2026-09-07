package com.template.market.domain.model

/**
 * 물품 품목. 검색 필터의 기준이 됩니다.
 *
 * [key] 는 저장소(DB/서버)에 저장되는 불변 식별자입니다.
 * 화면 표기를 바꾸더라도 [key] 는 바꾸지 마세요. 기존 데이터가 매칭되지 않습니다.
 */
enum class Category(val key: String, val label: String) {
    DIGITAL("digital", "디지털기기"),
    APPLIANCE("appliance", "생활가전"),
    FURNITURE("furniture", "가구/인테리어"),
    KIDS("kids", "유아동"),
    CLOTHES("clothes", "의류"),
    BOOK("book", "도서"),
    SPORTS("sports", "스포츠/레저"),
    HOBBY("hobby", "취미/게임/음반"),
    BEAUTY("beauty", "뷰티/미용"),
    PET("pet", "반려동물용품"),
    PLANT("plant", "식물"),
    KITCHEN("kitchen", "생활/주방"),
    CAR("car", "자동차/공구"),
    TICKET("ticket", "티켓/교환권"),
    ETC("etc", "기타 중고물품");

    companion object {
        fun fromKey(key: String?): Category = entries.firstOrNull { it.key == key } ?: ETC
    }
}
