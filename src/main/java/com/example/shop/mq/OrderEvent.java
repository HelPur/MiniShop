package com.example.shop.mq;

import java.math.BigDecimal;

public record OrderEvent(Long orderId, Long userId, BigDecimal amount, String type) {
}
