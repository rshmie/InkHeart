package io.inkHeart.service;

import io.inkHeart.crypto.Argon2Utils;
import io.inkHeart.entity.User;
import io.inkHeart.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserService userService;

    @Test
    void testRegisterSuccess() {
        String email = "test@example.com";
        String password = "password@123";

        Mockito.when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());
        Mockito.when(userRepository.save(Mockito.any(User.class)))
                .thenAnswer(i -> i.getArguments()[0]);

       // User user = userService.register(email, password);
        //Assertions.assertEquals(email, user.getEmail());
        //Assertions.assertNotNull(user.getPasswordHash());
    }

    @Test
    void testRegisterExistingEmailThrowsException() {
        String email = "test@example.com";
        String password = "password@123";
        Mockito.when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(new User()));

//        Assertions.assertThrows(RuntimeException.class, () -> {
//            userService.register(email, password);
//        });
    }

    @Test
    void testLoginSuccess() {
        String email = "test@example.com";
        String password = "password@123";
        String hashedPassword = Argon2Utils.hashPassword(password);

        User mockUser = new User();
        mockUser.setEmail(email);
      //  mockUser.setPasswordHash(hashedPassword);

        Mockito.when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(mockUser));

        User result = userService.login(email, password);
        Assertions.assertEquals(email, result.getEmail());
    }

    @Test
    void testLoginInvalidPasswordThrowsException() {
        String email = "test@example.com";
        String correctPassword = "password@123";
        String wrongPassword = "wrong@123";

        User mockUser = new User();
        mockUser.setEmail(email);
        //mockUser.setPasswordHash(Argon2Utils.hashPassword(correctPassword));

        Mockito.when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(mockUser));

        assertThrows(RuntimeException.class, () -> {
            userService.login(email, wrongPassword);
        });

    }
}