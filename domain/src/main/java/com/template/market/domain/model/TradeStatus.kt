package com.template.market.domain.model

enum class TradeStatus(val key: String, val label: String) {
    ON_SALE("on_sale", "판매중"),
    RESERVED("reserved", "예약중"),
    SOLD("sold", "거래완료");

    companion object {
        fun fromKey(key: String?): TradeStatus = entries.firstOrNull { it.key == key } ?: ON_SALE
    }
}
