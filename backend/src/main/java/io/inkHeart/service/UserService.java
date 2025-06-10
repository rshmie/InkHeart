package io.inkHeart.service;

import com.nimbusds.srp6.SRP6CryptoParams;
import com.nimbusds.srp6.SRP6ServerSession;
import io.inkHeart.entity.User;
import io.inkHeart.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final Map<String, SRP6ServerSession> srpSessions;
    public Map<String, SRP6ServerSession> getSrpSessions() {
        return srpSessions;
    }

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.srpSessions = new ConcurrentHashMap<>();
    }


    public SRP6ServerSession startLoginChallenge(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email not found"));

        SRP6CryptoParams cryptoParams = SRP6CryptoParams.getInstance(2048, "SHA-1"); // Is SHA-1 right?
        return new SRP6ServerSession(cryptoParams);
    }
    public User login(String email, String password) {
        // what about error code?
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid user credentials"));
//        if (!Argon2Utils.verifyPassword(user.getPasswordHash(), password)) {
//            throw new RuntimeException("Invalid password");
//        }
        return user;
    }

    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
}
