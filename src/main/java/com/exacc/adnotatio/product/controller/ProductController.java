package com.exacc.adnotatio.product.controller;

import com.exacc.adnotatio.common.ApiResponse;
import com.exacc.adnotatio.product.dto.ProductRequest;
import com.exacc.adnotatio.product.dto.ProductResponse;
import com.exacc.adnotatio.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@AllArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> create(
            @Valid @RequestBody ProductRequest request){

        ProductResponse product = productService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product berhasil dibuat", product));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) Integer minStock,
            @RequestParam(required = false) Integer maxStock,
            @RequestParam(required = false) Boolean deactivated
    ) {
        Page<ProductResponse> products = productService.getAll(
                page, size, sortBy, direction, name,
                minPrice, maxPrice, minStock, maxStock, deactivated
        );

        return ResponseEntity.ok(
                ApiResponse.success("Berhasil menampilkan product", products)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getByid(@PathVariable Long id){
        ProductResponse product = productService.getById(id);

        return ResponseEntity.ok(
                ApiResponse.success("Product berhasil ditemukan", product)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> update(
            @PathVariable Long id, @Valid @RequestBody ProductRequest request){

        ProductResponse product = productService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Product berhasil diupdate", product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
