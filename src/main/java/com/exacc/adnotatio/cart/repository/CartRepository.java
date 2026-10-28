package com.exacc.adnotatio.cart.repository;

import com.exacc.adnotatio.cart.entity.CartId;
import com.exacc.adnotatio.cart.entity.CartItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface CartRepository extends JpaRepository<CartItem, CartId> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ci FROM CartItem ci WHERE ci.customer.id = :customerId")
    java.util.List<CartItem> findAllByCustomerIdForUpdate(@Param("customerId") Long customerId);

    Optional<CartItem> findByCustomer_IdAndProduct_Id(
            Long customerId,
            Long productId
    );


    @EntityGraph(attributePaths = "product")
    @Query(
            value = """
            SELECT ci
            FROM CartItem ci
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

    @EntityGraph(attributePaths = {"customer", "product"})
    @Query("SELECT ci FROM CartItem ci WHERE ci.customer.id = :customerId AND ci.product.id = :productId")
    Optional<CartItem> findCartItemForCustomer(
            @Param("customerId") Long customerId,
            @Param("productId") Long productId
    );


}
