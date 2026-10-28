package com.exacc.adnotatio.product.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank(message = "Product name is required")
        @Size(max = 255, message = "Product name must not exceed 255 characters")
        String name,

        @NotNull(message = "Unit price is required")
        @DecimalMin(value = "0.0", message = "Unit price must be at least 0")
        @DecimalMax(
                value = "9999999999.0",
                message = "Unit price must not exceed 9999999999"
        )
        BigDecimal unitPrice,

        @NotNull(message = "Units in stock is required")
        @Min(value = 0, message = "Units in stock must be at least 0")
        Integer unitsInStock,

        @NotNull(message = "Deactivated status is required")
        Boolean deactivated
) {}
