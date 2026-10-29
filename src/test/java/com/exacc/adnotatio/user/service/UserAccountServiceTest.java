package com.exacc.adnotatio.user.service;

import com.exacc.adnotatio.user.entity.Role;
import com.exacc.adnotatio.user.entity.User;
import com.exacc.adnotatio.user.entity.UserRole;
import com.exacc.adnotatio.user.repository.UserRepository;
import com.exacc.adnotatio.user.repository.UserRoleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserAccountServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final UserRoleRepository userRoleRepository = mock(UserRoleRepository.class);
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final UserAccountService service = new UserAccountService(
            userRepository, userRoleRepository, passwordEncoder);

    @Test
    void createsFirstAdminWithBcryptPasswordAndSeparateRoleMapping() {
        when(userRoleRepository.existsByRole(Role.ADMIN)).thenReturn(false);
        when(userRepository.existsByUsername("first-admin")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        service.bootstrapAdmin(" first-admin ", "secret-password");

        var userCaptor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getUsername()).isEqualTo("first-admin");
        assertThat(savedUser.getPassword()).isNotEqualTo("secret-password");
        assertThat(passwordEncoder.matches("secret-password", savedUser.getPassword())).isTrue();

        var roleCaptor = org.mockito.ArgumentCaptor.forClass(UserRole.class);
        verify(userRoleRepository).save(roleCaptor.capture());
        assertThat(roleCaptor.getValue().getRole()).isEqualTo(Role.ADMIN);
        assertThat(roleCaptor.getValue().getUser()).isSameAs(savedUser);
    }

    @Test
    void doesNotCreateDuplicateAdminOnRestart() {
        when(userRoleRepository.existsByRole(Role.ADMIN)).thenReturn(true);

        service.bootstrapAdmin("first-admin", "secret-password");

        verify(userRepository, never()).save(any(User.class));
        verify(userRoleRepository, never()).save(any(UserRole.class));
    }

    @Test
    void requiresBootstrapCredentialsWhenNoAdminExists() {
        when(userRoleRepository.existsByRole(Role.ADMIN)).thenReturn(false);

        assertThatThrownBy(() -> service.bootstrapAdmin("", ""))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("APP_BOOTSTRAP_ADMIN_USERNAME");
    }
}
