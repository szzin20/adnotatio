
package com.exacc.adnotatio.product.mapper;

import com.exacc.adnotatio.product.dto.ProductRequest;
import com.exacc.adnotatio.product.dto.ProductResponse;
import com.exacc.adnotatio.product.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductResponse toResponse(Product product);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(
            target = "name",
            source = "name",
            qualifiedByName = "trimName"
    )
    Product toEntity(ProductRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(
            target = "name",
            source = "name",
            qualifiedByName = "trimName"
    )
    void updateEntity(
            ProductRequest request,
            @MappingTarget Product product
    );

    @Named("trimName")
    default String trimName(String name) {
        return name == null ? null : name.trim();
    }
}
