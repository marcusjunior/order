package com.ambev.order.domain.port.in;

import com.ambev.order.domain.model.OrderDomain;

public interface CreateOrderUseCase {
    OrderDomain createOrder(OrderDomain order);
}

