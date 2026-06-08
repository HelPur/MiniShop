package com.example.shop.mq;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("rocketmq")
public class RocketMqOrderEventPublisher implements OrderEventPublisher {
    private final RocketMQTemplate rocketMQTemplate;
    private final String topic;

    public RocketMqOrderEventPublisher(RocketMQTemplate rocketMQTemplate, @Value("${app.mq.topic}") String topic) {
        this.rocketMQTemplate = rocketMQTemplate;
        this.topic = topic;
    }

    @Override
    public void publish(OrderEvent event) {
        rocketMQTemplate.convertAndSend(topic, event);
    }
}
