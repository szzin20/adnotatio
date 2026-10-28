package com.exacc.adnotatio.customer.mapper;

import com.exacc.adnotatio.customer.dto.CustomerRequest;
import com.exacc.adnotatio.customer.dto.CustomerResponse;
import com.exacc.adnotatio.customer.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    CustomerResponse toResponse(Customer customer);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Customer toEntity(CustomerRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void updateEntity(CustomerRequest request, @MappingTarget Customer customer);
}
