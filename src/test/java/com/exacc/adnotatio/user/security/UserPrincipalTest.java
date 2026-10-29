package com.exacc.adnotatio.user.security;

import com.exacc.adnotatio.user.entity.Role;
import com.exacc.adnotatio.user.entity.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserPrincipalTest {

    @Test
    void mapsEachPersistentRoleToSpringRoleAuthority() {
        User user = new User();
        user.setUsername("person");
        user.setPassword("{bcrypt-hash}");

        for (Role role : Role.values()) {
            UserPrincipal principal = new UserPrincipal(user, role);

            assertThat(principal.getAuthorities())
                    .extracting("authority")
                    .containsExactly("ROLE_" + role.name());
            assertThat(principal.getUsername()).isEqualTo("person");
            assertThat(principal.getPassword()).isEqualTo("{bcrypt-hash}");
        }
    }
}
