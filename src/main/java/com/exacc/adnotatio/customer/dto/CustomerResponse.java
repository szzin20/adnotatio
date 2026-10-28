package com.exacc.adnotatio.customer.dto;

public record CustomerResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber
) {}
