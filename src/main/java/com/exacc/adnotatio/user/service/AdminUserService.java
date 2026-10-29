package com.exacc.adnotatio.user.service;

import com.exacc.adnotatio.common.ResourceNotFoundException;
import com.exacc.adnotatio.customer.entity.Customer;
import com.exacc.adnotatio.customer.repository.CustomerRepository;
import com.exacc.adnotatio.user.dto.UserAdminResponse;
import com.exacc.adnotatio.user.dto.UserCreateRequest;
import com.exacc.adnotatio.user.dto.UserRoleUpdateRequest;
import com.exacc.adnotatio.user.entity.Role;
import com.exacc.adnotatio.user.entity.User;
import com.exacc.adnotatio.user.entity.UserCustomer;
import com.exacc.adnotatio.user.entity.UserRole;
import com.exacc.adnotatio.user.repository.UserCustomerRepository;
import com.exacc.adnotatio.user.repository.UserRepository;
import com.exacc.adnotatio.user.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminUserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserCustomerRepository userCustomerRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAdminResponse create(UserCreateRequest request) {
        validateRoleCustomer(request.role(), request.customerId());
        User user = new User();
        user.setUsername(request.username().trim());
        user.setPassword(passwordEncoder.encode(request.password()));
        User saved = userRepository.save(user);
        userRoleRepository.save(new UserRole(saved, request.role()));
        if (request.role() == Role.CUSTOMER) {
            userCustomerRepository.save(new UserCustomer(saved, requireCustomer(request.customerId())));
        }
        return toResponse(saved, request.role());
    }

    @Transactional(readOnly = true)
    public List<UserAdminResponse> getAll() {
        return userRepository.findAll().stream().map(user -> {
            Role role = userRoleRepository.findByUser_Id(user.getId())
                    .map(UserRole::getRole)
                    .orElseThrow(() -> new IllegalStateException("User has no assigned role"));
            return toResponse(user, role);
        }).toList();
    }

    public UserAdminResponse updateRole(Long userId, Long actingUserId, UserRoleUpdateRequest request) {
        if (userId.equals(actingUserId)) {
            throw new AccessDeniedException("Admin tidak dapat mengubah role akunnya sendiri");
        }
        validateRoleCustomer(request.role(), request.customerId());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User dengan ID " + userId + " tidak ditemukan", HttpStatus.NOT_FOUND));
        UserRole mapping = userRoleRepository.findByUser_Id(userId)
                .orElseGet(() -> new UserRole(user, request.role()));
        mapping.setRole(request.role());
        userRoleRepository.save(mapping);

        if (request.role() == Role.CUSTOMER) {
            Customer customer = requireCustomer(request.customerId());
            UserCustomer customerMapping = userCustomerRepository.findByUser_Id(userId)
                    .orElseGet(() -> new UserCustomer(user, customer));
            customerMapping.setCustomer(customer);
            userCustomerRepository.save(customerMapping);
        } else {
            userCustomerRepository.deleteByUser_Id(userId);
        }
        return toResponse(user, request.role());
    }

    private void validateRoleCustomer(Role role, Long customerId) {
        if (role == Role.CUSTOMER && customerId == null) {
            throw new IllegalArgumentException("customerId wajib diisi untuk role CUSTOMER");
        }
        if (role != Role.CUSTOMER && customerId != null) {
            throw new IllegalArgumentException("customerId hanya boleh diisi untuk role CUSTOMER");
        }
    }

    private Customer requireCustomer(Long customerId) {
        return customerRepository.findByIdAndDeletedFalse(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer dengan ID " + customerId + " tidak ditemukan", HttpStatus.NOT_FOUND));
    }

    private UserAdminResponse toResponse(User user, Role role) {
        Long customerId = userCustomerRepository.findByUser_Id(user.getId())
                .map(mapping -> mapping.getCustomer().getId())
                .orElse(null);
        return new UserAdminResponse(user.getId(), user.getUsername(), role, customerId);
    }
}
