package com.exacc.adnotatio.user.service;

import com.exacc.adnotatio.user.repository.UserCustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerOwnershipService {

    private final UserCustomerRepository userCustomerRepository;

    public Long requireOwnedCustomer(Long userId, Long requestedCustomerId) {
        Long ownedCustomerId = userCustomerRepository.findByUser_Id(userId)
                .map(mapping -> mapping.getCustomer().getId())
                .orElseThrow(() -> new AccessDeniedException("Akun tidak terhubung ke customer"));
        if (!ownedCustomerId.equals(requestedCustomerId)) {
            throw new AccessDeniedException("Customer bukan milik akun yang sedang login");
        }
        return ownedCustomerId;
    }

    public Long getOwnedCustomerId(Long userId) {
        return userCustomerRepository.findByUser_Id(userId)
                .map(mapping -> mapping.getCustomer().getId())
                .orElseThrow(() -> new AccessDeniedException("Akun tidak terhubung ke customer"));
    }
}
