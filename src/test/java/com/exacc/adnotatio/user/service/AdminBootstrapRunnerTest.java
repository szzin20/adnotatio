package com.exacc.adnotatio.user.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class AdminBootstrapRunnerTest {

    private final UserAccountService userAccountService = mock(UserAccountService.class);
    private final AdminBootstrapRunner runner = new AdminBootstrapRunner(userAccountService);

    @Test
    void skipsBootstrapWhenNeitherEnvironmentValueIsSet() {
        ReflectionTestUtils.setField(runner, "username", "");
        ReflectionTestUtils.setField(runner, "password", "");

        runner.run(new DefaultApplicationArguments(new String[0]));

        verifyNoInteractions(userAccountService);
    }

    @Test
    void bootstrapsWhenCredentialsAreConfigured() {
        ReflectionTestUtils.setField(runner, "username", "first-admin");
        ReflectionTestUtils.setField(runner, "password", "provided-password");

        runner.run(new DefaultApplicationArguments(new String[0]));

        verify(userAccountService).bootstrapAdmin("first-admin", "provided-password");
    }
}
