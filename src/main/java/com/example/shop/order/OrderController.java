package com.example.shop.order;

import com.example.shop.common.ApiResponse;
import com.example.shop.common.CurrentUserContext;
import com.example.shop.config.RequireRole;
import com.example.shop.user.UserRole;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ApiResponse<ShopOrder> create() {
        return ApiResponse.ok(orderService.createFromCart(CurrentUserContext.getRequired().getId()));
    }

    @GetMapping
    public ApiResponse<List<ShopOrder>> listMine() {
        return ApiResponse.ok(orderService.listMine(CurrentUserContext.getRequired().getId()));
    }

    @PatchMapping("/{id}/ship")
    @RequireRole(UserRole.ADMIN)
    public ApiResponse<ShopOrder> ship(@PathVariable Long id) {
        return ApiResponse.ok(orderService.ship(id));
    }

    @PatchMapping("/{id}/receive")
    public ApiResponse<ShopOrder> receive(@PathVariable Long id) {
        return ApiResponse.ok(orderService.receive(id, CurrentUserContext.getRequired().getId()));
    }

    @PatchMapping("/{id}/refund")
    public ApiResponse<ShopOrder> requestRefund(@PathVariable Long id) {
        return ApiResponse.ok(orderService.requestRefund(id, CurrentUserContext.getRequired().getId()));
    }

    @PatchMapping("/{id}/refund/approve")
    @RequireRole(UserRole.ADMIN)
    public ApiResponse<ShopOrder> approveRefund(@PathVariable Long id) {
        return ApiResponse.ok(orderService.approveRefund(id));
    }
}
