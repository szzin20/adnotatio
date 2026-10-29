package com.exacc.adnotatio.order.controller;

import com.exacc.adnotatio.common.ApiResponse;
import com.exacc.adnotatio.order.dto.OrderDetailResponse;
import com.exacc.adnotatio.order.dto.OrderPageResponse;
import com.exacc.adnotatio.order.service.OrderService;
import com.exacc.adnotatio.user.security.UserPrincipal;
import com.exacc.adnotatio.user.service.CustomerOwnershipService;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/orders")
@AllArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CustomerOwnershipService customerOwnershipService;

    @GetMapping
    public ResponseEntity<ApiResponse<OrderPageResponse>> getOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long customerId = customerOwnershipService.getOwnedCustomerId(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Berhasil menampilkan orders",
                orderService.getOrdersForCustomer(customerId, page, size, sort, id, fromDate, toDate)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long customerId = customerOwnershipService.getOwnedCustomerId(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Order berhasil ditemukan",
                orderService.getOrderForCustomer(id, customerId)));
    }

}
