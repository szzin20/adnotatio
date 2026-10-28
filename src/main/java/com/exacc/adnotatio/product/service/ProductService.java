
package com.exacc.adnotatio.product.service;

import com.exacc.adnotatio.common.ResourceNotFoundException;
import com.exacc.adnotatio.product.dto.ProductRequest;
import com.exacc.adnotatio.product.dto.ProductResponse;
import com.exacc.adnotatio.product.entity.Product;
import com.exacc.adnotatio.product.mapper.ProductMapper;
import com.exacc.adnotatio.product.repository.ProductRepository;
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
import java.util.Set;

@Service
@Transactional
@AllArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductResponse create(ProductRequest request) {
        Product product = productMapper.toEntity(request);

        return productMapper.toResponse(
                productRepository.save(product)
        );
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getAll(
            int page,
            int size,
            String sortBy,
            String direction,
            String name,
            Integer minPrice,
            Integer maxPrice,
            Integer minStock,
            Integer maxStock,
            Boolean deactivated
    ) {
        if (page < 0) {
            throw new IllegalArgumentException("page tidak boleh kurang dari 0");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("size harus di antara 1 sampai 100");
        }

        Set<String> allowedSortFields = Set.of(
                "id", "name", "unitPrice", "unitsInStock", "deactivated"
        );
        if (!allowedSortFields.contains(sortBy)) {
            throw new IllegalArgumentException("sortBy harus berupa id, name, unitPrice, unitsInStock, atau deactivated");
        }

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Specification<Product> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (name != null && !name.isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + name.trim().toLowerCase(Locale.ROOT) + "%"
                ));
            }
            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.<Double>get("unitPrice"), minPrice.doubleValue()
                ));
            }
            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.<Double>get("unitPrice"), maxPrice.doubleValue()
                ));
            }
            if (minStock != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.<Integer>get("unitsInStock"), minStock
                ));
            }
            if (maxStock != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.<Integer>get("unitsInStock"), maxStock
                ));
            }
            if (deactivated != null) {
                predicates.add(criteriaBuilder.equal(root.get("deactivated"), deactivated));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return productRepository.findAll(specification, pageable)
                .map(productMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        return productMapper.toResponse(findById(id));
    }

    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findById(id);

        productMapper.updateEntity(request, product);

        return productMapper.toResponse(
                productRepository.save(product)
        );
    }

    public String delete(Long id) {
        Product product = findById(id);
        String productName = product.getName();

        productRepository.delete(product);

        return productName;
    }

    private Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product dengan ID " + id + " tidak ditemukan",
                        HttpStatus.NOT_FOUND
                ));
    }
}
