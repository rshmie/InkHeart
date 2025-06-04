package io.inkHeart.contollers;

import io.inkHeart.dto.AuthRequest;
import io.inkHeart.dto.LoginResponse;
import io.inkHeart.dto.RegisterResponse;
import io.inkHeart.dto.UpdatePasswordRequest;
import io.inkHeart.entity.CustomUserDetails;
import io.inkHeart.entity.User;
import io.inkHeart.security.JwtUtil;
import io.inkHeart.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;


/*
Client → /login → generates JWT
↓
Client sends: Authorization: Bearer <token>
↓
JwtAuthFilter checks token + identity
↓
Spring Security allows access if valid
↓
Your controller logic executes securely
 */
@RestController()
@RequestMapping("/api/user")
@Validated
public class UserController {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody AuthRequest authRequest) {
        User user = userService.register(authRequest.getEmail(), authRequest.getPassword());
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(user.getId())
                .toUri();
        return ResponseEntity.created(location).body(new RegisterResponse(user.getId(), user.getEmail()));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody AuthRequest authRequest) {
        userService.login(authRequest.getEmail(), authRequest.getPassword());
        String token = jwtUtil.generateToken(authRequest.getEmail());
        return ResponseEntity.ok(new LoginResponse(token, "Login successful"));
    }

    @PutMapping("/update-password")
    public ResponseEntity<String> updatePassword(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                 @RequestBody UpdatePasswordRequest passwordRequest) {
        var authenticatedEmail = userDetails.getUsername();
        userService.updatePassword(authenticatedEmail, passwordRequest.getNewPassword(), passwordRequest.getOldPassword());
        return ResponseEntity.ok("Password updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUserById(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id) {
         String authenticatedUserEmail = userDetails.getUsername();
         User authenticatedUser = userService.findByEmail(authenticatedUserEmail);
         if (authenticatedUser == null) {
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authenticated user not found.");
         }
         if (authenticatedUser.getId().equals(id)) {  /* or user has admin role */
             userService.deleteUserById(id);
             return ResponseEntity.ok("User deleted successfully");
         } else {
             return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authorized"); }
    }

    // Implement delete user account
}
