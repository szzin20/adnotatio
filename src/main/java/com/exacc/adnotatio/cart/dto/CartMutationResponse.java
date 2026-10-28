
package com.exacc.adnotatio.cart.dto;

public record CartMutationResponse(
        CartCustomerResponse customer,
        CartItemResponse item
) {}
