package com.exacc.adnotatio.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderSummaryResponse(
        Long id,
        Long customerId,
        String customerName,
        LocalDateTime orderDate,
        BigDecimal deliveryFee,
        BigDecimal totalAmount
) {
}
