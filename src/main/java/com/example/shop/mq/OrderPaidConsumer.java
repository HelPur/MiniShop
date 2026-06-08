package com.example.shop.mq;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("rocketmq")
@RocketMQMessageListener(topic = "${app.mq.topic}", consumerGroup = "mini-shop-paid-consumer")
public class OrderPaidConsumer implements RocketMQListener<OrderEvent> {
    private static final Logger log = LoggerFactory.getLogger(OrderPaidConsumer.class);

    @Override
    public void onMessage(OrderEvent event) {
        log.info("rocketmq consumed order event: {}", event);
    }
}
