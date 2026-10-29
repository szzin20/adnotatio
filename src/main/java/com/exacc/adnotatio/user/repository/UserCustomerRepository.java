package com.exacc.adnotatio.user.repository;

import com.exacc.adnotatio.user.entity.UserCustomer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCustomerRepository extends JpaRepository<UserCustomer, Long> {
    Optional<UserCustomer> findByUser_Id(Long userId);

    void deleteByUser_Id(Long userId);
}
