
package com.exacc.adnotatio.cart.dto;

import java.time.LocalDateTime;
import java.math.BigDecimal;

public record CartItemResponse(
        Long productId,
        String productName,
        BigDecimal price,
        Integer quantity,
        LocalDateTime addedDate
) {}
