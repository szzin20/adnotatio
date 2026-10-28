
package com.exacc.adnotatio.cart.mapper;

import com.exacc.adnotatio.cart.dto.CartCustomerResponse;
import com.exacc.adnotatio.cart.dto.CartItemResponse;
import com.exacc.adnotatio.cart.entity.CartItem;
import com.exacc.adnotatio.customer.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "price", source = "product.unitPrice")
    CartItemResponse toItemResponse(CartItem cartItem);

    CartCustomerResponse toCustomerResponse(Customer customer);
}
