package io.inkHeart.service;

import io.inkHeart.crypto.Argon2Utils;
import io.inkHeart.entity.User;
import io.inkHeart.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(String email, String password) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already in use");
        }
        String passwordHash = Argon2Utils.hashPassword(password);
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        return userRepository.save(user);
    }

    public User login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid user credentials"));
        if (!Argon2Utils.verifyPassword(user.getPasswordHash(), password)) {
            throw new RuntimeException("Invalid password");
        }
        return user;
    }

    public void updatePassword(String email, String oldPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (Argon2Utils.verifyPassword(user.getPasswordHash(), oldPassword)) {
            throw new RuntimeException("Old password is incorrect");
        }
        user.setPasswordHash(Argon2Utils.hashPassword(newPassword));
        userRepository.save(user);
    }

    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
}
