package io.inkHeart.contollers;

import io.inkHeart.dto.AuthRequest;
import io.inkHeart.dto.UpdatePasswordRequest;
import io.inkHeart.entity.User;
import io.inkHeart.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController()
@RequestMapping("/user")
@Validated
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody AuthRequest authRequest) {
        User user = userService.register(authRequest.getEmail(), authRequest.getPassword());
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(user.getId())
                .toUri();
        return ResponseEntity.created(location).body(user);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody AuthRequest authRequest) {
        userService.login(authRequest.getEmail(), authRequest.getPassword());
        return ResponseEntity.ok("Login successful");
    }

    @PutMapping("/update-password")
    public ResponseEntity<String> updatePassword(@RequestBody UpdatePasswordRequest passwordRequest) {
        userService.updatePassword(passwordRequest.getEmail(), passwordRequest.getNewPassword(), passwordRequest.getOldPassword());
        return ResponseEntity.ok("Password updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUserById(@PathVariable Long id) {
        userService.deleteUserById(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    // Implement delete user account
}
