package com.exacc.adnotatio.order.mapper;

import com.exacc.adnotatio.order.dto.OrderDetailResponse;
import com.exacc.adnotatio.order.dto.OrderItemResponse;
import com.exacc.adnotatio.order.dto.OrderSummaryResponse;
import com.exacc.adnotatio.order.entity.Order;
import com.exacc.adnotatio.order.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", expression = "java(order.getCustomer().getFirstName() + \" \" + order.getCustomer().getLastName())")
    OrderSummaryResponse toSummary(Order order);

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", expression = "java(order.getCustomer().getFirstName() + \" \" + order.getCustomer().getLastName())")
    OrderDetailResponse toDetail(Order order);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "subtotal", expression = "java(subtotal(item))")
    OrderItemResponse toItemResponse(OrderItem item);

    default BigDecimal subtotal(OrderItem item) {
        return item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
    }
}
