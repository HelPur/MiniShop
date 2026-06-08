package com.example.shop.cart;

import java.math.BigDecimal;

public record CartLine(Long productId, String productName, BigDecimal price, Integer quantity, BigDecimal lineTotal) {
}
