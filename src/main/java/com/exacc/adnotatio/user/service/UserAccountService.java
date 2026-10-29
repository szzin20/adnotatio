package com.exacc.adnotatio.user.service;

import com.exacc.adnotatio.user.entity.Role;
import com.exacc.adnotatio.user.entity.User;
import com.exacc.adnotatio.user.entity.UserRole;
import com.exacc.adnotatio.user.repository.UserRepository;
import com.exacc.adnotatio.user.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserAccountService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void bootstrapAdmin(String username, String rawPassword) {
        if (userRoleRepository.existsByRole(Role.ADMIN)) {
            return;
        }
        if (username == null || username.isBlank() || rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalStateException(
                    "Set APP_BOOTSTRAP_ADMIN_USERNAME and APP_BOOTSTRAP_ADMIN_PASSWORD to create the first admin");
        }

        String normalizedUsername = username.trim();
        if (normalizedUsername.length() > 100) {
            throw new IllegalStateException("APP_BOOTSTRAP_ADMIN_USERNAME must be at most 100 characters");
        }
        if (userRepository.existsByUsername(normalizedUsername)) {
            throw new IllegalStateException("Bootstrap username already exists but has no ADMIN role");
        }

        User user = new User();
        user.setUsername(normalizedUsername);
        user.setPassword(passwordEncoder.encode(rawPassword));
        User savedUser = userRepository.save(user);
        userRoleRepository.save(new UserRole(savedUser, Role.ADMIN));
    }
}
