package com.exacc.adnotatio.cart.repository;

import com.exacc.adnotatio.cart.entity.CartId;
import com.exacc.adnotatio.cart.entity.CartItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface CartRepository extends JpaRepository<CartItem, CartId> {

    long countByCustomer_Id(Long customerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ci FROM CartItem ci WHERE ci.customer.id = :customerId")
    java.util.List<CartItem> findAllByCustomerIdForUpdate(@Param("customerId") Long customerId);

    Optional<CartItem> findByCustomer_IdAndProduct_Id(
            Long customerId,
            Long productId
    );


    @Query(
            value = """
            SELECT ci
            FROM CartItem ci
            JOIN FETCH ci.product
            WHERE ci.customer.id = :customerId
              AND (
                  :name IS NULL
                  OR LOWER(ci.product.name)
                     LIKE LOWER(CONCAT('%', :name, '%'))
              )
            """,
            countQuery = """
            SELECT COUNT(ci)
            FROM CartItem ci
            WHERE ci.customer.id = :customerId
              AND (
                  :name IS NULL
                  OR LOWER(ci.product.name)
                     LIKE LOWER(CONCAT('%', :name, '%'))
              )
            """
    )
    Page<CartItem> findCartItemsByCustomerId(
            @Param("customerId") Long customerId,
            @Param("name") String name,
            Pageable pageable
    );

    @Query("SELECT ci FROM CartItem ci WHERE ci.customer.id = :customerId AND ci.product.id = :productId")
    Optional<CartItem> findCartItemForCustomer(
            @Param("customerId") Long customerId,
            @Param("productId") Long productId
    );


}
