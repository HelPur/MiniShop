package com.example.shop.order;

import com.example.shop.cart.CartService;
import com.example.shop.common.BusinessException;
import com.example.shop.lock.DistributedLock;
import com.example.shop.mq.OrderEvent;
import com.example.shop.mq.OrderEventPublisher;
import com.example.shop.product.Product;
import com.example.shop.product.ProductRepository;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class OrderService {
    private final CartService cartService;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final DistributedLock distributedLock;
    private final OrderEventPublisher orderEventPublisher;
    private final TransactionTemplate transactionTemplate;

    public OrderService(
            CartService cartService,
            ProductRepository productRepository,
            OrderRepository orderRepository,
            DistributedLock distributedLock,
            OrderEventPublisher orderEventPublisher,
            TransactionTemplate transactionTemplate) {
        this.cartService = cartService;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.distributedLock = distributedLock;
        this.orderEventPublisher = orderEventPublisher;
        this.transactionTemplate = transactionTemplate;
    }

    public ShopOrder createFromCart(Long userId) {
        return distributedLock.execute("lock:order:user:" + userId, Duration.ofSeconds(10),
                () -> transactionTemplate.execute(status -> createFromCartInTransaction(userId)));
    }

    private ShopOrder createFromCartInTransaction(Long userId) {
        Map<Long, Integer> cart = cartService.loadRaw(userId);
        if (cart.isEmpty()) {
            throw new BusinessException("Cart is empty");
        }
        ShopOrder order = new ShopOrder();
        order.setUserId(userId);
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> entry : cart.entrySet()) {
            Product product = productRepository.findById(entry.getKey())
                    .orElseThrow(() -> new BusinessException("Product not found"));
            if (product.getStock() < entry.getValue()) {
                throw new BusinessException("Insufficient stock for " + product.getName());
            }
            product.setStock(product.getStock() - entry.getValue());
            product.touch();

            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setUnitPrice(product.getPrice());
            item.setQuantity(entry.getValue());
            order.addItem(item);
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(entry.getValue())));
        }
        order.setTotalAmount(total);
        ShopOrder saved = orderRepository.save(order);
        cartService.clear(userId);
        orderEventPublisher.publish(new OrderEvent(saved.getId(), userId, saved.getTotalAmount(), "ORDER_CREATED"));
        return saved;
    }

    public List<ShopOrder> listMine(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public ShopOrder markPaid(Long orderId) {
        ShopOrder order = get(orderId);
        if (order.getStatus() != OrderStatus.CREATED) {
            throw new BusinessException("Only CREATED order can be paid");
        }
        order.setStatus(OrderStatus.PAID);
        order.setPaidAt(LocalDateTime.now());
        orderEventPublisher.publish(new OrderEvent(order.getId(), order.getUserId(), order.getTotalAmount(), "ORDER_PAID"));
        return order;
    }

    @Transactional
    public ShopOrder ship(Long orderId) {
        ShopOrder order = get(orderId);
        if (order.getStatus() != OrderStatus.PAID) {
            throw new BusinessException("Only PAID order can be shipped");
        }
        order.setStatus(OrderStatus.SHIPPED);
        order.setShippedAt(LocalDateTime.now());
        return order;
    }

    @Transactional
    public ShopOrder receive(Long orderId, Long userId) {
        ShopOrder order = get(orderId);
        requireOwner(order, userId);
        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new BusinessException("Only SHIPPED order can be received");
        }
        order.setStatus(OrderStatus.RECEIVED);
        order.setReceivedAt(LocalDateTime.now());
        return order;
    }

    @Transactional
    public ShopOrder requestRefund(Long orderId, Long userId) {
        ShopOrder order = get(orderId);
        requireOwner(order, userId);
        if (order.getStatus() != OrderStatus.PAID && order.getStatus() != OrderStatus.SHIPPED && order.getStatus() != OrderStatus.RECEIVED) {
            throw new BusinessException("Current order status cannot request refund");
        }
        order.setStatus(OrderStatus.REFUND_REQUESTED);
        return order;
    }

    @Transactional
    public ShopOrder approveRefund(Long orderId) {
        ShopOrder order = get(orderId);
        if (order.getStatus() != OrderStatus.REFUND_REQUESTED) {
            throw new BusinessException("Refund was not requested");
        }
        order.setStatus(OrderStatus.REFUNDED);
        return order;
    }

    public ShopOrder get(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("Order not found"));
    }

    private void requireOwner(ShopOrder order, Long userId) {
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("Order does not belong to current user");
        }
    }
}
