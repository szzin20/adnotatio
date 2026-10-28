
package com.exacc.adnotatio.cart.dto;

import java.util.List;

public record CartPageResponse(
        List<CartItemResponse> content,
        PageMetadata page
) {
    public record PageMetadata(
            int size,
            int number,
            long totalElements,
            int totalPages
    ) {
    }
}
