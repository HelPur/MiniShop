package com.example.shop.payment;

import com.example.shop.common.BusinessException;
import com.example.shop.order.OrderService;
import com.example.shop.order.OrderStatus;
import com.example.shop.order.ShopOrder;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderService orderService;

    public PaymentService(PaymentRepository paymentRepository, OrderService orderService) {
        this.paymentRepository = paymentRepository;
        this.orderService = orderService;
    }

    @Transactional
    public PaymentRecord createPayment(Long userId, PayRequest request) {
        ShopOrder order = orderService.get(request.orderId());
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("Order does not belong to current user");
        }
        if (order.getStatus() != OrderStatus.CREATED) {
            throw new BusinessException("Only CREATED order can start payment");
        }
        PaymentRecord payment = new PaymentRecord();
        payment.setOrderId(order.getId());
        payment.setUserId(userId);
        payment.setMethod(request.method());
        payment.setAmount(order.getTotalAmount());
        payment.setTradeNo("PAY-" + UUID.randomUUID());
        return paymentRepository.save(payment);
    }

    @Transactional
    public PaymentRecord callback(CallbackRequest request) {
        PaymentRecord payment = paymentRepository.findByTradeNo(request.tradeNo())
                .orElseThrow(() -> new BusinessException("Payment not found"));
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return payment;
        }
        if (request.success()) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaidAt(LocalDateTime.now());
            orderService.markPaid(payment.getOrderId());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
        }
        return payment;
    }

    public record PayRequest(Long orderId, PaymentMethod method) {
    }

    public record CallbackRequest(String tradeNo, boolean success, String channelMessage) {
    }
}
