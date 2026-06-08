package com.example.shop.lock;

import java.time.Duration;
import java.util.function.Supplier;

public interface DistributedLock {
    <T> T execute(String lockKey, Duration ttl, Supplier<T> action);
}
