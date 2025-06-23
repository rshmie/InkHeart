package io.inkHeart.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class UpdatePasswordRequest {
    @Email
    @NotBlank(message = "Email is required!")
    private final String email;
    @NotBlank(message = "New password is required!")
    private final String newPassword;
    @NotBlank(message = "Old password is required!")
    private final String oldPassword;

    public UpdatePasswordRequest(String email, String password, String oldPassword) {
        this.email = email;
        this.newPassword = password;
        this.oldPassword = oldPassword;
    }
    public String getEmail() {
        return email;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public String getOldPassword() {
        return oldPassword;
    }

}
