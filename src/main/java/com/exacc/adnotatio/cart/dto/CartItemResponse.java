
package com.exacc.adnotatio.cart.dto;

import java.time.LocalDateTime;

public record CartItemResponse(
        Long productId,
        String productName,
        Double price,
        Integer quantity,
        LocalDateTime addedDate
) {}
