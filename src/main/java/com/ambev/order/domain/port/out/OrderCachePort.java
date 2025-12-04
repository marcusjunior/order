package com.ambev.order.domain.port.out;

public interface OrderCachePort {
    boolean exists(String key);
    void store(String key, String value, long ttl);
    String get(String key);
    void delete(String key);
}

