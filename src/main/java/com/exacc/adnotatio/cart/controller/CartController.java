package com.exacc.adnotatio.cart.controller;

import com.exacc.adnotatio.cart.dto.CartItemRequest;
import com.exacc.adnotatio.cart.dto.CartMutationResponse;
import com.exacc.adnotatio.cart.dto.CartPageResponse;
import com.exacc.adnotatio.cart.dto.CartQuantityRequest;
import com.exacc.adnotatio.cart.service.CartService;
import com.exacc.adnotatio.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customers/{id}/cart/items")
@AllArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartPageResponse>> getItems(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(required = false) String name
    ) {
        return ResponseEntity.ok(ApiResponse.success("Berhasil menampilkan cart", 
                cartService.getItems(id, page, size, sort, name)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CartMutationResponse>> addItem(
            @PathVariable Long id,
            @Valid @RequestBody CartItemRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Produk berhasil ditambahkan ke cart", cartService.addItem(id, request)));
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<ApiResponse<CartMutationResponse>> updateItem(
            @PathVariable Long id,
            @PathVariable Long productId,
            @Valid @RequestBody CartQuantityRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Quantity cart berhasil diubah", cartService.updateItem(id, productId, request)));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<CartMutationResponse>> removeItem(
            @PathVariable Long id,
            @PathVariable Long productId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Produk berhasil dihapus dari cart", cartService.removeItem(id, productId)));
    }
}
