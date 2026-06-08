package com.example.shop.product;

import com.example.shop.common.BusinessException;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, ProductCategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<Product> listOnShelf() {
        return productRepository.findByStatus(ProductStatus.ON_SHELF);
    }

    @Transactional
    public Product create(CreateProductRequest request) {
        ProductCategory category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new BusinessException("Category not found"));
        Product product = new Product();
        product.setCategory(category);
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStock(request.stock());
        return productRepository.save(product);
    }

    @Transactional
    public Product updatePrice(Long productId, BigDecimal price) {
        Product product = get(productId);
        product.setPrice(price);
        product.touch();
        return product;
    }

    @Transactional
    public Product updateStock(Long productId, int stock) {
        Product product = get(productId);
        product.setStock(stock);
        product.touch();
        return product;
    }

    @Transactional
    public Product changeStatus(Long productId, ProductStatus status) {
        Product product = get(productId);
        product.setStatus(status);
        product.touch();
        return product;
    }

    public Product get(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("Product not found"));
    }

    public record CreateProductRequest(Long categoryId, String name, String description, BigDecimal price, Integer stock) {
    }

    public record PriceRequest(BigDecimal price) {
    }

    public record StockRequest(Integer stock) {
    }
}
