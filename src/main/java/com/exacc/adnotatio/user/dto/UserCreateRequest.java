package com.exacc.adnotatio.user.dto;

import com.exacc.adnotatio.user.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
        @NotBlank @Size(max = 100) String username,
        @NotBlank @Size(max = 72) String password,
        @NotNull Role role,
        Long customerId
) {
}
