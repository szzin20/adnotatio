package com.exacc.adnotatio.order.dto;

import java.util.List;

public record OrderPageResponse(List<OrderSummaryResponse> content, PageMetadata page) {
    public record PageMetadata(int size, int number, long totalElements, int totalPages) {
    }
}
