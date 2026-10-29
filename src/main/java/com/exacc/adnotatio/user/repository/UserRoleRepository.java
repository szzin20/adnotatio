package com.exacc.adnotatio.user.repository;

import com.exacc.adnotatio.user.entity.Role;
import com.exacc.adnotatio.user.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    Optional<UserRole> findByUser_Id(Long userId);

    boolean existsByRole(Role role);
}
