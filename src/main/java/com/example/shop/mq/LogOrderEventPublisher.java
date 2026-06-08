package com.example.shop.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!rocketmq")
public class LogOrderEventPublisher implements OrderEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(LogOrderEventPublisher.class);

    @Override
    public void publish(OrderEvent event) {
        log.info("mock publish order event: {}", event);
    }
}
