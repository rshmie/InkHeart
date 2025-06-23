package io.inkHeart.contollers;

import com.nimbusds.srp6.SRP6Exception;
import com.nimbusds.srp6.SRP6ServerSession;
import io.inkHeart.dto.*;
import io.inkHeart.entity.User;
import io.inkHeart.security.JwtUtil;
import io.inkHeart.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Base64;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtUtil jwtUtil;
    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> registerUser(@RequestBody RegisterSrpRequest srpRequest) {
        User user = authService.register(srpRequest.getEmail(), srpRequest.getSalt(), srpRequest.getVerifier());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/${id}")
                .buildAndExpand(user.getId())
                .toUri();
        return ResponseEntity.created(location).body(new RegisterResponse(user.getId(), user.getEmail()));
    }

    @PostMapping("/login/challenge")
    public ResponseEntity<LoginChallengeResponse> loginChallenge(@RequestBody LoginChallengeRequest challengeRequest) {
        var srpServerSession = authService.startLoginChallenge(challengeRequest.getEmail());

        // Get the salt and server Public key B and send to client
        String serverPublicKeyB64 = Base64.getEncoder().encodeToString(srpServerSession.getPublicServerValue().toByteArray());
        String saltB64 = Base64.getEncoder().encodeToString(srpServerSession.getSalt().toByteArray());
        LoginChallengeResponse challengeResponse = new LoginChallengeResponse();
        challengeResponse.setServerPublicKeyB(serverPublicKeyB64);
        challengeResponse.setSalt(saltB64);

        return ResponseEntity.ok(challengeResponse);
    }

    @PostMapping("/login/verify")
    public ResponseEntity<FinalLoginResponse> loginVerify(@RequestBody LoginVerifyRequest loginVerifyRequest) {
        SRP6ServerSession srpServerSession;
        try {
            srpServerSession = authService.verifyLogin(loginVerifyRequest);
        } catch (SRP6Exception e) {
            return ResponseEntity.badRequest().body(new FinalLoginResponse(null, null, "Login Failed! Please check your password"));
        }
        if (srpServerSession == null) {
            return ResponseEntity.badRequest().body(new FinalLoginResponse(null, null, "Login Failed! Please check your password"));
        }

        /* Get the proof M2 once client is authenticated successfully and clean the session */
        String serverProofM2 = Base64.getEncoder().encodeToString(srpServerSession.getServerEvidenceMessage().toByteArray());
        authService.getSrpSessionsMap().remove(loginVerifyRequest.getEmail());

        String jwtToken = jwtUtil.generateToken(loginVerifyRequest.getEmail());
        return ResponseEntity.ok(new FinalLoginResponse(serverProofM2, jwtToken, "Client is authenticated successfully"));
    }
}
