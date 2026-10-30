package com.exacc.adnotatio.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminBootstrapRunner implements ApplicationRunner {

    private final UserAccountService userAccountService;

    @Value("${app.bootstrap.admin.username:}")
    private String username;

    @Value("${app.bootstrap.admin.password:}")
    private String password;

    @Override
    public void run(ApplicationArguments args) {
        if ((username == null || username.isBlank()) && (password == null || password.isBlank())) {
            log.warn("Admin bootstrap skipped. Set APP_BOOTSTRAP_ADMIN_USERNAME and "
                    + "APP_BOOTSTRAP_ADMIN_PASSWORD to create the first admin.");
            return;
        }
        userAccountService.bootstrapAdmin(username, password);
    }
}
