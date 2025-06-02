package io.inkHeart.contollers;

import io.inkHeart.crypto.Argon2Utils;
import io.inkHeart.dto.AuthRequest;
import io.inkHeart.dto.UpdatePasswordRequest;
import io.inkHeart.entity.User;
import io.inkHeart.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;

    @Test
    void testRegisterSuccess() throws Exception {

        AuthRequest request = new AuthRequest("test_user@example.com", "securePass123");

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test_user@example.com"));
    }

    @Test
    void testLoginUserSuccess() throws Exception {
        // Ensure the user exists before login
        User user = new User();
        user.setEmail("login_user@example.com");
        user.setPasswordHash(Argon2Utils.hashPassword("mypassword"));
        userRepository.save(user);

        AuthRequest request = new AuthRequest("login_user@example.com", "mypassword");

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Login successful"));
    }

    @Test
    void testUpdatePasswordSuccess() throws Exception {
        // Ensure the user exists
        User user = new User();
        user.setEmail("update_user@example.com");
        user.setPasswordHash(Argon2Utils.hashPassword("oldpass"));
        userRepository.save(user);

        UpdatePasswordRequest request = new UpdatePasswordRequest(
                "update_user@example.com", "newpass123", "oldpass"
        );

        mockMvc.perform(put("/user/update-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Password updated successfully"));
    }

    @Test
    void testDeleteUserByIdSuccess() throws Exception {
        // Create and save user first
        User user = new User();
        user.setEmail("delete_me@example.com");
        user.setPasswordHash(Argon2Utils.hashPassword("pass123"));
        User saved = userRepository.save(user);

        mockMvc.perform(delete("/user/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted successfully"));
    }



}