package com.exacc.adnotatio.order.service;

import com.exacc.adnotatio.common.ResourceNotFoundException;
import com.exacc.adnotatio.common.BusinessRuleException;
import com.exacc.adnotatio.cart.entity.CartItem;
import com.exacc.adnotatio.cart.repository.CartRepository;
import com.exacc.adnotatio.customer.service.CustomerService;
import com.exacc.adnotatio.delivery.entity.Delivery;
import com.exacc.adnotatio.delivery.repository.DeliveryRepository;
import com.exacc.adnotatio.order.dto.OrderDetailResponse;
import com.exacc.adnotatio.order.dto.OrderPageResponse;
import com.exacc.adnotatio.order.dto.OrderSummaryResponse;
import com.exacc.adnotatio.order.entity.Order;
import com.exacc.adnotatio.order.entity.OrderItem;
import com.exacc.adnotatio.order.mapper.OrderMapper;
import com.exacc.adnotatio.order.repository.OrderRepository;
import com.exacc.adnotatio.product.entity.Product;
import com.exacc.adnotatio.product.repository.ProductRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.math.BigDecimal;

@Service
@Transactional
@AllArgsConstructor
public class OrderService {

    private static final String DEFAULT_DELIVERY_ID = "EXD";

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final CustomerService customerService;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final DeliveryRepository deliveryRepository;

    @Transactional(readOnly = true)
    public OrderPageResponse getOrders(
            int page,
            int size,
            String sort,
            Long id,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        if (page < 0) throw new IllegalArgumentException("page must be zero or greater");
        if (size < 1 || size > 100) throw new IllegalArgumentException("size must be between 1 and 100");
        Set<String> sortableFields = Set.of("id", "orderDate", "deliveryFee", "totalAmount");
        if (!sortableFields.contains(sort)) throw new IllegalArgumentException("sort must be id, orderDate, deliveryFee, or totalAmount");
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("fromDate must be on or before toDate");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).ascending());
        Specification<Order> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (id != null) predicates.add(cb.equal(root.get("id"), id));
            if (fromDate != null) predicates.add(cb.greaterThanOrEqualTo(root.get("orderDate"), fromDate.atStartOfDay()));
            if (toDate != null) predicates.add(cb.lessThan(root.get("orderDate"), toDate.plusDays(1).atStartOfDay()));
            return cb.and(predicates.toArray(Predicate[]::new));
        };

        Page<Order> result = orderRepository.findAll(specification, pageable);
        List<OrderSummaryResponse> content = result.getContent().stream().map(orderMapper::toSummary).toList();
        return new OrderPageResponse(content, new OrderPageResponse.PageMetadata(
                result.getSize(), result.getNumber(), result.getTotalElements(), result.getTotalPages()));
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse getOrder(Long id) {
        Order order = orderRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order dengan ID " + id + " tidak ditemukan", HttpStatus.NOT_FOUND));
        return orderMapper.toDetail(order);
    }

    public OrderDetailResponse checkout(Long customerId, String deliveryId) {
        var customer = customerService.lockActiveCustomerEntityById(customerId);
        List<CartItem> cartItems = cartRepository.findAllByCustomerIdForUpdate(customerId);
        if (cartItems.isEmpty()) {
            throw new BusinessRuleException("Cart customer kosong; tidak ada yang dapat di-checkout", HttpStatus.BAD_REQUEST);
        }

        List<Long> productIds = cartItems.stream().map(item -> item.getProduct().getId()).distinct().sorted().toList();
        Map<Long, Product> products = productRepository.findAllByIdForUpdate(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        for (CartItem cartItem : cartItems) {
            Product product = products.get(cartItem.getProduct().getId());
            if (product == null) {
                throw new ResourceNotFoundException("Product dalam cart tidak ditemukan", HttpStatus.NOT_FOUND);
            }
            if (Boolean.TRUE.equals(product.getDeactivated())) {
                throw new BusinessRuleException("Product " + product.getId() + " sudah tidak aktif", HttpStatus.CONFLICT);
            }
            if (product.getUnitsInStock() == null || product.getUnitsInStock() < cartItem.getQuantity()) {
                throw new BusinessRuleException("Stok product " + product.getName() + " tidak mencukupi", HttpStatus.CONFLICT);
            }
        }

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Delivery dengan ID " + deliveryId + " tidak ditemukan", HttpStatus.NOT_FOUND));

        Order order = new Order();
        order.setCustomer(customer);
        order.setDelivery(delivery);
        order.setOrderDate(LocalDateTime.now());
        order.setDeliveryFee(delivery.getCost());

        BigDecimal subtotalTotal = BigDecimal.ZERO;
        for (CartItem cartItem : cartItems) {
            Product product = products.get(cartItem.getProduct().getId());
            BigDecimal unitPrice = product.getUnitPrice();
            Integer quantity = cartItem.getQuantity();
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setProductName(product.getName());
            orderItem.setUnitPrice(unitPrice);
            orderItem.setQuantity(quantity);
            order.addItem(orderItem);
            subtotalTotal = subtotalTotal.add(unitPrice.multiply(BigDecimal.valueOf(quantity)));
            product.setUnitsInStock(product.getUnitsInStock() - quantity);
        }
        order.setTotalAmount(subtotalTotal.add(delivery.getCost()));

        Order saved = orderRepository.save(order);
        productRepository.saveAll(products.values());
        cartRepository.deleteAll(cartItems);
        return orderMapper.toDetail(saved);
    }

    public OrderDetailResponse checkout(Long customerId) {
        return checkout(customerId, DEFAULT_DELIVERY_ID);
    }
}
