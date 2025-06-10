package io.inkHeart.service;

import io.inkHeart.entity.User;
import io.inkHeart.repository.UserRepository;

import org.springframework.stereotype.Service;
@Service
public class UserService {
    private final UserRepository userRepository;
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
}
