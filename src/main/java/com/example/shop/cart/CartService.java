package com.example.shop.cart;

import com.example.shop.common.BusinessException;
import com.example.shop.product.Product;
import com.example.shop.product.ProductService;
import com.example.shop.product.ProductStatus;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class CartService {
    private final CartStore cartStore;
    private final ProductService productService;

    public CartService(CartStore cartStore, ProductService productService) {
        this.cartStore = cartStore;
        this.productService = productService;
    }

    public CartSummary add(Long userId, Long productId, int quantity) {
        validateQuantity(quantity);
        Product product = productService.get(productId);
        if (product.getStatus() != ProductStatus.ON_SHELF) {
            throw new BusinessException("Product is off shelf");
        }
        cartStore.put(userId, productId, quantity);
        return summary(userId);
    }

    public CartSummary remove(Long userId, Long productId) {
        cartStore.remove(userId, productId);
        return summary(userId);
    }

    public CartSummary summary(Long userId) {
        Map<Long, Integer> items = cartStore.load(userId);
        List<CartLine> lines = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> entry : items.entrySet()) {
            Product product = productService.get(entry.getKey());
            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(entry.getValue()));
            lines.add(new CartLine(product.getId(), product.getName(), product.getPrice(), entry.getValue(), lineTotal));
            total = total.add(lineTotal);
        }
        return new CartSummary(lines, total);
    }

    public Map<Long, Integer> loadRaw(Long userId) {
        return cartStore.load(userId);
    }

    public void clear(Long userId) {
        cartStore.clear(userId);
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new BusinessException("Quantity must be greater than 0");
        }
    }

    public record CartRequest(Long productId, Integer quantity) {
    }
}
