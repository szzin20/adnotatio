package com.exacc.adnotatio.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CustomerRequest(
        @NotBlank(message = "firstName wajib diisi")
        @Size(max = 50, message = "firstName maksimal 50 karakter")
        String firstName,

        @NotBlank(message = "lastName wajib diisi")
        @Size(max = 50, message = "lastName maksimal 50 karakter")
        String lastName,

        @NotBlank(message = "email wajib diisi")
        @Email(message = "email harus memiliki format yang valid")
        String email,

        @NotBlank(message = "phoneNumber wajib diisi")
        @Pattern(regexp = "^[0-9]{10,12}$", message = "phoneNumber harus berisi 10 sampai 12 digit")
        String phoneNumber
) {}
