
package com.exacc.adnotatio.cart.entity;

import com.exacc.adnotatio.customer.entity.Customer;
import com.exacc.adnotatio.product.entity.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "carts")
@IdClass(CartId.class)
@org.hibernate.annotations.Check(constraints = "quantity >= 1")
@Getter
@Setter
@RequiredArgsConstructor
public class CartItem {

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "added_date", nullable = false)
    private LocalDateTime addedDate;

}
