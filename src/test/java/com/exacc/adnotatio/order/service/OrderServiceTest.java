package com.exacc.adnotatio.order.service;

import com.exacc.adnotatio.cart.entity.CartItem;
import com.exacc.adnotatio.cart.repository.CartRepository;
import com.exacc.adnotatio.customer.entity.Customer;
import com.exacc.adnotatio.customer.service.CustomerService;
import com.exacc.adnotatio.delivery.entity.Delivery;
import com.exacc.adnotatio.delivery.repository.DeliveryRepository;
import com.exacc.adnotatio.order.dto.OrderDetailResponse;
import com.exacc.adnotatio.order.entity.Order;
import com.exacc.adnotatio.order.mapper.OrderMapper;
import com.exacc.adnotatio.order.repository.OrderRepository;
import com.exacc.adnotatio.product.entity.Product;
import com.exacc.adnotatio.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import com.exacc.adnotatio.common.BusinessRuleException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock OrderMapper orderMapper;
    @Mock CustomerService customerService;
    @Mock CartRepository cartRepository;
    @Mock ProductRepository productRepository;
    @Mock DeliveryRepository deliveryRepository;

    private OrderService orderService;
    private Customer customer;
    private Product product;
    private CartItem cartItem;
    private Delivery delivery;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, orderMapper, customerService,
                cartRepository, productRepository, deliveryRepository);
        customer = new Customer();
        customer.setId(1L);
        product = new Product();
        product.setId(2L);
        product.setName("Keyboard");
        product.setUnitPrice(new BigDecimal("400000.00"));
        product.setUnitsInStock(5);
        product.setDeactivated(false);
        cartItem = new CartItem();
        cartItem.setCustomer(customer);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        delivery = new Delivery();
        delivery.setId("EXD");
        delivery.setCost(new BigDecimal("5000.00"));
        when(customerService.lockActiveCustomerEntityById(1L)).thenReturn(customer);
    }

    @Test
    void checkoutSnapshotsPricesTotalsAndClearsCart() {
        when(cartRepository.findAllByCustomerIdForUpdate(1L)).thenReturn(List.of(cartItem));
        when(productRepository.findAllByIdForUpdate(List.of(2L))).thenReturn(List.of(product));
        when(deliveryRepository.findById("EXD")).thenReturn(Optional.of(delivery));
        when(orderRepository.save(any(Order.class))).thenAnswer(call -> call.getArgument(0));
        OrderDetailResponse response = new OrderDetailResponse(1L, 1L, "Alice Johnson", null,
                new BigDecimal("5000.00"), new BigDecimal("805000.00"), List.of());
        when(orderMapper.toDetail(any(Order.class))).thenReturn(response);

        orderService.checkout(1L, "EXD");

        assertEquals(3, product.getUnitsInStock());
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();
        assertEquals(new BigDecimal("805000.00"), savedOrder.getTotalAmount());
        assertEquals("Keyboard", savedOrder.getOrderItems().getFirst().getProductName());
        assertEquals(new BigDecimal("400000.00"), savedOrder.getOrderItems().getFirst().getUnitPrice());
        verify(cartRepository).deleteAll(List.of(cartItem));
        verify(productRepository).saveAll(any());
    }

    @Test
    void insufficientStockLeavesOrderAndCartUntouched() {
        product.setUnitsInStock(1);
        when(cartRepository.findAllByCustomerIdForUpdate(1L)).thenReturn(List.of(cartItem));
        when(productRepository.findAllByIdForUpdate(List.of(2L))).thenReturn(List.of(product));

        BusinessRuleException exception = assertThrows(BusinessRuleException.class,
                () -> orderService.checkout(1L, "EXD"));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals(1, product.getUnitsInStock());
        verify(orderRepository, never()).save(any(Order.class));
        verify(cartRepository, never()).deleteAll(any(Iterable.class));
    }
}
