
package com.exacc.adnotatio.cart.entity;

import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@RequiredArgsConstructor
public class CartId implements Serializable {

    private Long customer;
    private Long product;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof CartId cartId)) return false;

        return Objects.equals(customer, cartId.customer)
                && Objects.equals(product, cartId.product);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customer, product);
    }
}
