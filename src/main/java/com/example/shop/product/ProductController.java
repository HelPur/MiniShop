package com.example.shop.product;

import com.example.shop.common.ApiResponse;
import com.example.shop.config.RequireRole;
import com.example.shop.user.UserRole;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ApiResponse<List<Product>> list() {
        return ApiResponse.ok(productService.listOnShelf());
    }

    @PostMapping
    @RequireRole(UserRole.ADMIN)
    public ApiResponse<Product> create(@RequestBody ProductService.CreateProductRequest request) {
        return ApiResponse.ok(productService.create(request));
    }

    @PatchMapping("/{id}/price")
    @RequireRole(UserRole.ADMIN)
    public ApiResponse<Product> updatePrice(@PathVariable Long id, @RequestBody ProductService.PriceRequest request) {
        return ApiResponse.ok(productService.updatePrice(id, request.price()));
    }

    @PatchMapping("/{id}/stock")
    @RequireRole(UserRole.ADMIN)
    public ApiResponse<Product> updateStock(@PathVariable Long id, @RequestBody ProductService.StockRequest request) {
        return ApiResponse.ok(productService.updateStock(id, request.stock()));
    }

    @PatchMapping("/{id}/status/{status}")
    @RequireRole(UserRole.ADMIN)
    public ApiResponse<Product> changeStatus(@PathVariable Long id, @PathVariable ProductStatus status) {
        return ApiResponse.ok(productService.changeStatus(id, status));
    }
}
