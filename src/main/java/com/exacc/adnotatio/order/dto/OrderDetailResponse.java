package com.exacc.adnotatio.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailResponse(
        Long id,
        Long customerId,
        String customerName,
        LocalDateTime orderDate,
        BigDecimal deliveryFee,
        BigDecimal totalAmount,
        List<OrderItemResponse> orderItems
) {
}
