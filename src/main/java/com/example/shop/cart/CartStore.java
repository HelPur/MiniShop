package com.example.shop.cart;

import java.util.Map;

public interface CartStore {
    void put(Long userId, Long productId, int quantity);

    void remove(Long userId, Long productId);

    Map<Long, Integer> load(Long userId);

    void clear(Long userId);
}
