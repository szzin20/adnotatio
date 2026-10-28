package com.exacc.adnotatio.cart.service;

import com.exacc.adnotatio.cart.dto.CartItemRequest;
import com.exacc.adnotatio.cart.dto.CartPageResponse;
import com.exacc.adnotatio.cart.dto.CartQuantityRequest;
import com.exacc.adnotatio.cart.entity.CartItem;
import com.exacc.adnotatio.cart.mapper.CartMapper;
import com.exacc.adnotatio.cart.repository.CartRepository;
import com.exacc.adnotatio.common.ResourceNotFoundException;
import com.exacc.adnotatio.customer.entity.Customer;
import com.exacc.adnotatio.customer.service.CustomerService;
import com.exacc.adnotatio.product.entity.Product;
import com.exacc.adnotatio.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock CartRepository cartRepository;
    @Mock CustomerService customerService;
    @Mock ProductRepository productRepository;
    @Mock CartMapper cartMapper;

    private CartService cartService;
    private Customer customer;
    private Product product;

    @BeforeEach
    void setUp() {
        cartService = new CartService(cartRepository, customerService, productRepository, cartMapper);
        customer = new Customer();
        customer.setId(7L);
        product = new Product();
        product.setId(12L);
        product.setName("Keyboard");
        product.setUnitsInStock(3);
        product.setDeactivated(false);
    }

    @Test
    void addingExistingProductUpdatesQuantityInsteadOfAddingDuplicate() {
        when(customerService.lockActiveCustomerEntityById(7L)).thenReturn(customer);
        CartItem existing = new CartItem();
        existing.setCustomer(customer);
        existing.setProduct(product);
        existing.setQuantity(1);
        when(productRepository.findById(12L)).thenReturn(Optional.of(product));
        when(cartRepository.findByCustomer_IdAndProduct_Id(7L, 12L)).thenReturn(Optional.of(existing));
        when(cartRepository.save(existing)).thenReturn(existing);

        cartService.addItem(7L, new CartItemRequest(12L, 2));

        assertEquals(2, existing.getQuantity());
        verify(cartRepository).save(existing);
    }

    @Test
    void patchCannotUpdateAnItemOwnedByAnotherCustomer() {
        when(customerService.lockActiveCustomerEntityById(7L)).thenReturn(customer);
        when(cartRepository.findCartItemForCustomer(7L, 12L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> cartService.updateItem(7L, 12L, new CartQuantityRequest(4)));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        verify(cartRepository, never()).save(any(CartItem.class));
    }

    @Test
    void addingUnavailableProductReturnsConflict() {
        when(customerService.lockActiveCustomerEntityById(7L)).thenReturn(customer);
        product.setUnitsInStock(0);
        when(productRepository.findById(12L)).thenReturn(Optional.of(product));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> cartService.addItem(7L, new CartItemRequest(12L, 1)));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(cartRepository, never()).save(any(CartItem.class));
    }

    @Test
    void emptyCartReturnsAnEmptyPage() {
        when(customerService.getActiveCustomerEntityById(7L)).thenReturn(customer);
        when(cartRepository.countByCustomer_Id(7L)).thenReturn(0L);

        CartPageResponse response = cartService.getItems(7L, 0, 10, "name", null);

        assertTrue(response.content().isEmpty());
        assertEquals(0, response.page().totalElements());
        assertEquals(0, response.page().totalPages());
        verify(cartRepository, never()).findCartItemsByCustomerId(any(), any(), any());
    }
}
