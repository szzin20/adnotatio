package com.exacc.adnotatio.product.dto;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        BigDecimal unitPrice,
        Integer unitsInStock,
        Boolean deactivated
) {}
