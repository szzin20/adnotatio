package com.exacc.adnotatio.user.dto;

import com.exacc.adnotatio.user.entity.Role;

public record UserAdminResponse(Long id, String username, Role role, Long customerId) {
}
