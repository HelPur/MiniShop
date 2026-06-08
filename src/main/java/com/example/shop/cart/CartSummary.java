package com.example.shop.cart;

import java.math.BigDecimal;
import java.util.List;

public record CartSummary(List<CartLine> lines, BigDecimal totalAmount) {
}
