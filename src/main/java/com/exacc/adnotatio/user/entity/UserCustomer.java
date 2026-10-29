package com.exacc.adnotatio.user.entity;

import com.exacc.adnotatio.customer.entity.Customer;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_customers", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_customers_user_id", columnNames = "user_id"),
        @UniqueConstraint(name = "uk_user_customers_customer_id", columnNames = "customer_id")
})
@Getter
@Setter
@NoArgsConstructor
public class UserCustomer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false, unique = true)
    private Customer customer;

    public UserCustomer(User user, Customer customer) {
        this.user = user;
        this.customer = customer;
    }
}
