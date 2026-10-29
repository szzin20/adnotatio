package com.exacc.adnotatio.user.dto;

import com.exacc.adnotatio.user.entity.Role;
import jakarta.validation.constraints.NotNull;

public record UserRoleUpdateRequest(@NotNull Role role, Long customerId) {
}
