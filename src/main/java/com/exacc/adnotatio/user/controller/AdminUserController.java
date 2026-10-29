package com.exacc.adnotatio.user.controller;

import com.exacc.adnotatio.common.ApiResponse;
import com.exacc.adnotatio.user.dto.UserAdminResponse;
import com.exacc.adnotatio.user.dto.UserCreateRequest;
import com.exacc.adnotatio.user.dto.UserRoleUpdateRequest;
import com.exacc.adnotatio.user.security.UserPrincipal;
import com.exacc.adnotatio.user.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserAdminResponse>>> getUsers() {
        return ResponseEntity.ok(ApiResponse.success("Berhasil menampilkan users", adminUserService.getAll()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserAdminResponse>> createUser(
            @Valid @RequestBody UserCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User berhasil dibuat", adminUserService.create(request)));
    }

    @PatchMapping("/{userId}/role")
    public ResponseEntity<ApiResponse<UserAdminResponse>> updateRole(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UserRoleUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Role user berhasil diubah",
                adminUserService.updateRole(userId, principal.getUserId(), request)));
    }
}
