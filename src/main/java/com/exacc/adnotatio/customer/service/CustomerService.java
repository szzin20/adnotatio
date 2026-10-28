package com.exacc.adnotatio.customer.service;

import com.exacc.adnotatio.customer.dto.CustomerRequest;
import com.exacc.adnotatio.customer.dto.CustomerResponse;
import com.exacc.adnotatio.customer.entity.Customer;
import com.exacc.adnotatio.customer.mapper.CustomerMapper;
import com.exacc.adnotatio.customer.repository.CustomerRepository;
import com.exacc.adnotatio.common.ResourceNotFoundException;
import jakarta.persistence.criteria.Predicate;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
@AllArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Transactional(readOnly = true)
    public Page<CustomerResponse> getAll(int page, int size, String sort, String fullName) {
        if (page < 0) {
            throw new IllegalArgumentException("page tidak boleh kurang dari 0");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("size harus di antara 1 sampai 100");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        Specification<Customer> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.isFalse(root.get("deleted")));

            if (fullName != null && !fullName.isBlank()) {
                String pattern = "%" + fullName.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(criteriaBuilder.concat(
                                criteriaBuilder.concat(root.<String>get("firstName"), " "),
                                root.<String>get("lastName")
                        )),
                        pattern
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return customerRepository.findAll(specification, pageable)
                .map(customerMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getById(Long id) {
        return customerMapper.toResponse(getActiveCustomerEntityById(id));
    }

    public CustomerResponse create(CustomerRequest request) {
        Customer customer = customerMapper.toEntity(request);
        return customerMapper.toResponse(customerRepository.save(customer));
    }

    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = getActiveCustomerEntityById(id);
        customerMapper.updateEntity(request, customer);
        return customerMapper.toResponse(customerRepository.save(customer));
    }

    public void delete(Long id) {
        Customer customer = getActiveCustomerEntityById(id);
        customer.setDeleted(true);
        customerRepository.save(customer);
    }

    /** Use this lookup before cart or order operations to reject soft-deleted customers. */
    @Transactional(readOnly = true)
    public Customer getActiveCustomerEntityById(Long id) {
        return customerRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer dengan ID " + id + " tidak ditemukan",
                        HttpStatus.NOT_FOUND
                ));
    }
}
