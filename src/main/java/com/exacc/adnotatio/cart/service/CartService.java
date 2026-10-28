
package com.exacc.adnotatio.cart.service;

import com.exacc.adnotatio.cart.repository.CartRepository;
import com.exacc.adnotatio.cart.mapper.CartMapper;
import com.exacc.adnotatio.cart.dto.CartItemRequest;
import com.exacc.adnotatio.cart.dto.CartMutationResponse;
import com.exacc.adnotatio.cart.dto.CartQuantityRequest;
import com.exacc.adnotatio.common.ResourceNotFoundException;
import com.exacc.adnotatio.customer.service.CustomerService;
import com.exacc.adnotatio.customer.entity.Customer;
import com.exacc.adnotatio.product.entity.Product;
import com.exacc.adnotatio.product.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.exacc.adnotatio.cart.dto.CartItemResponse;
import com.exacc.adnotatio.cart.dto.CartPageResponse;
import com.exacc.adnotatio.cart.entity.CartItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CustomerService customerService;
    private final ProductRepository productRepository;
    private final CartMapper cartMapper;


    @Transactional(readOnly = true)
    public CartPageResponse getItems(
            Long customerId,
            int page,
            int size,
            String sort,
            String name
    ) {
        // 1. Validasi customer
        customerService.getActiveCustomerEntityById(customerId);

        // 2. Validasi pagination
        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page must be zero or greater"
            );
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Size must be between 1 and 100"
            );
        }

        // 3. Siapkan pagination
        if (!"name".equalsIgnoreCase(sort)) {
            throw new IllegalArgumentException("Sort must be name");
        }

        if (cartRepository.countByCustomer_Id(customerId) == 0) {
            return new CartPageResponse(List.of(), new CartPageResponse.PageMetadata(size, page, 0, 0));
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("product.name").ascending());

        // 4. Ambil data dari repository
        Page<CartItem> cartPage =
                cartRepository.findCartItemsByCustomerId(
                        customerId,
                        name == null || name.isBlank() ? null : name.trim(),
                        pageable
                );

        // 5. Ubah entity menjadi response DTO
        var content = cartPage.getContent().stream().map(cartMapper::toItemResponse).toList();

        // 6. Kembalikan data beserta metadata pagination
        return new CartPageResponse(
                content,
                new CartPageResponse.PageMetadata(
                        cartPage.getSize(),
                        cartPage.getNumber(),
                        cartPage.getTotalElements(),
                        cartPage.getTotalPages()
                )
        );
    }

    public CartMutationResponse addItem(Long customerId, CartItemRequest request) {
        Customer customer = customerService.lockActiveCustomerEntityById(customerId);
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product dengan ID " + request.productId() + " tidak ditemukan", HttpStatus.NOT_FOUND));
        ensureAvailable(product);

        CartItem item = cartRepository.findByCustomer_IdAndProduct_Id(customerId, product.getId())
                .orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setCustomer(customer);
                    newItem.setProduct(product);
                    newItem.setAddedDate(LocalDateTime.now());
                    return newItem;
                });
        item.setQuantity(request.quantity());
        CartItem saved = cartRepository.save(item);
        return new CartMutationResponse(cartMapper.toCustomerResponse(customer), cartMapper.toItemResponse(saved));
    }

    public CartMutationResponse updateItem(Long customerId, Long productId, CartQuantityRequest request) {
        Customer customer = customerService.lockActiveCustomerEntityById(customerId);
        CartItem item = findCartItem(customerId, productId);
        item.setQuantity(request.quantity());
        CartItem saved = cartRepository.save(item);
        return new CartMutationResponse(cartMapper.toCustomerResponse(customer), cartMapper.toItemResponse(saved));
    }

    public CartMutationResponse removeItem(Long customerId, Long productId) {
        Customer customer = customerService.lockActiveCustomerEntityById(customerId);
        CartItem item = findCartItem(customerId, productId);
        CartMutationResponse response = new CartMutationResponse(
                cartMapper.toCustomerResponse(customer), cartMapper.toItemResponse(item));
        cartRepository.delete(item);
        return response;
    }

    private CartItem findCartItem(Long customerId, Long productId) {
        return cartRepository.findCartItemForCustomer(customerId, productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart item untuk product ID " + productId + " tidak ditemukan", HttpStatus.NOT_FOUND));
    }

    private void ensureAvailable(Product product) {
        if (Boolean.TRUE.equals(product.getDeactivated())
                || product.getUnitsInStock() == null || product.getUnitsInStock() < 1) {
            throw new ResourceNotFoundException(
                    "Product dengan ID " + product.getId() + " tidak tersedia", HttpStatus.CONFLICT);
        }
    }


}
