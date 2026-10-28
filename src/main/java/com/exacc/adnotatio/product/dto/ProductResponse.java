package com.exacc.adnotatio.product.dto;

public record ProductResponse(
        Long id,
        String name,
        Double unitPrice,
        Integer unitsInStock,
        Boolean deactivated
) {}