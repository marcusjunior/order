package com.ambev.order.domain.port.out;

import com.ambev.order.domain.model.OrderDomain;

public interface OrderPublisherPort {
    void publishOrder(OrderDomain order);
}

