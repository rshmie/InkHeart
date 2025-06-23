package io.inkHeart.cli.service.auth;

import io.inkHeart.cli.dto.FinalLoginResponse;
import io.inkHeart.cli.dto.LoginChallengeResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigInteger;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class LoginServiceTest {
    private LoginService loginService;

    @BeforeEach
    public void setup() {
        loginService = Mockito.spy(new LoginService(mock(java.net.http.HttpClient.class)));
    }

    @Test
    public void testHandleLoginSuccess() throws Exception {
        String email = "test@example.com";
        String password = "testPassword";

        // Prepare fake salt and server public key
        byte[] fakeSalt = new byte[16];
        new java.security.SecureRandom().nextBytes(fakeSalt);
        BigInteger serverB = new BigInteger("1234567890");

        // Prepare mock LoginChallengeResponse
        LoginChallengeResponse challengeResponse = new LoginChallengeResponse();
        challengeResponse.setSalt(Base64.getEncoder().encodeToString(fakeSalt));
        challengeResponse.setServerPublicKeyB(Base64.getEncoder().encodeToString(serverB.toByteArray()));

        FinalLoginResponse expectedFinalResponse = new FinalLoginResponse("M2Proof", "jwtToken", "Login success");

        // Mock internal method calls
        doReturn(challengeResponse).when(loginService).getLoginChallengeResponse(email);
        doReturn(expectedFinalResponse).when(loginService)
                .getLoginVerifyResponse(eq(email), eq(password), any(), any());

        FinalLoginResponse actualResponse = loginService.handleLogin(email, password);
        assertEquals("jwtToken", actualResponse.jwtToken());
        assertEquals("M2Proof", actualResponse.serverProofM2());
        assertEquals("Login success", actualResponse.message());
    }

    @Test
    public void testHandleLoginWithInvalidEmail() throws Exception {
        doReturn(null).when(loginService).getLoginChallengeResponse(any());
        FinalLoginResponse response = loginService.handleLogin("bad@example.com", "irrelevant");
        assertEquals("Login failed: Invalid email or server error.", response.message());
    }

    @Test
    public void testHandleLoginVerifyFailure() throws Exception {
        LoginChallengeResponse response = new LoginChallengeResponse();
        byte[] salt = new byte[16];
        new java.security.SecureRandom().nextBytes(salt);
        response.setSalt(Base64.getEncoder().encodeToString(salt));
        response.setServerPublicKeyB(Base64.getEncoder().encodeToString(new BigInteger("12345").toByteArray()));

        doReturn(response).when(loginService).getLoginChallengeResponse(any());
        doReturn(null).when(loginService).getLoginVerifyResponse(any(), any(), any(), any());

        FinalLoginResponse loginFail = loginService.handleLogin("email", "pw");
        assertEquals("Login failed!", loginFail.message());
    }

}