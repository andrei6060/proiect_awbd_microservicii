package com.clinic.clinic.Entity.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationRequestDto {
    @Email(message = "Email is not well-formatted")
    @NotEmpty(message = "Email is mandatory")
    @NotBlank(message = "Email is mandatory")
    private String email;
    @Size(min = 10, message = "Password should be at least 10 characters minimum")
    @NotEmpty (message = "Password is mandatory")
    @NotBlank(message = "Password is mandatory")
    private String password;
}
