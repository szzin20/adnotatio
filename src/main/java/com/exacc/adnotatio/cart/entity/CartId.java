
package com.exacc.adnotatio.cart.entity;

import java.io.Serializable;
import java.util.Objects;

public class CartId implements Serializable {

    private Long customer;
    private Long product;

    public CartId() {
    }

    public CartId(Long customer, Long product) {
        this.customer = customer;
        this.product = product;
    }

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
