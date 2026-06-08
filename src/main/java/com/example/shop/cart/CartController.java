package com.example.shop.cart;

import com.example.shop.common.ApiResponse;
import com.example.shop.common.CurrentUserContext;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ApiResponse<CartSummary> summary() {
        return ApiResponse.ok(cartService.summary(CurrentUserContext.getRequired().getId()));
    }

    @PostMapping
    public ApiResponse<CartSummary> add(@RequestBody CartService.CartRequest request) {
        return ApiResponse.ok(cartService.add(CurrentUserContext.getRequired().getId(), request.productId(), request.quantity()));
    }

    @DeleteMapping("/{productId}")
    public ApiResponse<CartSummary> remove(@PathVariable Long productId) {
        return ApiResponse.ok(cartService.remove(CurrentUserContext.getRequired().getId(), productId));
    }
}
