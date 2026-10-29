package com.exacc.adnotatio.customer.controller;

import com.exacc.adnotatio.common.ApiResponse;
import com.exacc.adnotatio.cart.dto.CartItemRequest;
import com.exacc.adnotatio.cart.dto.CartMutationResponse;
import com.exacc.adnotatio.cart.dto.CartPageResponse;
import com.exacc.adnotatio.cart.dto.CartQuantityRequest;
import com.exacc.adnotatio.cart.service.CartService;
import com.exacc.adnotatio.customer.dto.CustomerRequest;
import com.exacc.adnotatio.customer.dto.CustomerResponse;
import com.exacc.adnotatio.customer.service.CustomerService;
import com.exacc.adnotatio.user.security.UserPrincipal;
import com.exacc.adnotatio.user.service.CustomerOwnershipService;
import com.exacc.adnotatio.order.dto.OrderDetailResponse;
import com.exacc.adnotatio.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/customers")
@AllArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final CartService cartService;
    private final OrderService orderService;
    private final CustomerOwnershipService customerOwnershipService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CustomerResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "lastName") String sort,
            @RequestParam(required = false) String fullName
    ) {
        Page<CustomerResponse> customers = customerService.getAll(page, size, sort, fullName);
        return ResponseEntity.ok(ApiResponse.success("Berhasil menampilkan customer", customers));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Customer berhasil ditemukan", customerService.getById(id)
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponse>> create(
            @Valid @RequestBody CustomerRequest request
    ) {
        CustomerResponse customer = customerService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Customer berhasil dibuat", customer));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Customer berhasil diupdate", customerService.update(id, request)
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/cart/items")
    public ResponseEntity<ApiResponse<CartPageResponse>> getCartItems(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(required = false) String name
    ) {
        customerOwnershipService.requireOwnedCustomer(principal.getUserId(), id);
        return ResponseEntity.ok(ApiResponse.success("Berhasil menampilkan cart",
                cartService.getItems(id, page, size, sort, name)));
    }

    @PostMapping("/{id}/cart/items")
    public ResponseEntity<ApiResponse<CartMutationResponse>> addCartItem(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CartItemRequest request
    ) {
        customerOwnershipService.requireOwnedCustomer(principal.getUserId(), id);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Produk berhasil ditambahkan ke cart", cartService.addItem(id, request)));
    }

    @DeleteMapping("/{id}/cart/items/{productId}")
    public ResponseEntity<ApiResponse<CartMutationResponse>> removeCartItem(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long productId
    ) {
        customerOwnershipService.requireOwnedCustomer(principal.getUserId(), id);
        return ResponseEntity.ok(ApiResponse.success(
                "Produk berhasil dihapus dari cart", cartService.removeItem(id, productId)));
    }

    @PatchMapping("/{id}/cart/items/{productId}")
    public ResponseEntity<ApiResponse<CartMutationResponse>> updateCartItem(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long productId,
            @Valid @RequestBody CartQuantityRequest request
    ) {
        customerOwnershipService.requireOwnedCustomer(principal.getUserId(), id);
        return ResponseEntity.ok(ApiResponse.success(
                "Quantity cart berhasil diubah", cartService.updateItem(id, productId, request)));
    }

    @PostMapping("/{id}/orders")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> checkout(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        customerOwnershipService.requireOwnedCustomer(principal.getUserId(), id);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Checkout berhasil", orderService.checkout(id)));
    }
}
