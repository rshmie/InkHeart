package io.inkHeart.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// may be i can use records here
public class AuthRequest {
    @Email
    @NotBlank(message = "Email is required!")
    private final String email;
    @NotBlank(message = "Password is required!")
    private final String password;

    @JsonCreator
    public AuthRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }


    public String getPassword() {
        return password;
    }

}
