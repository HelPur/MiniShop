package com.example.shop.mq;

public interface OrderEventPublisher {
    void publish(OrderEvent event);
}
