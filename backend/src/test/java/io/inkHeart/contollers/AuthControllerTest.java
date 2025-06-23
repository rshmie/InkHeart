package io.inkHeart.contollers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.srp6.SRP6ClientSession;
import com.nimbusds.srp6.SRP6CryptoParams;
import com.nimbusds.srp6.SRP6VerifierGenerator;
import io.inkHeart.dto.LoginChallengeRequest;
import io.inkHeart.dto.LoginChallengeResponse;
import io.inkHeart.dto.LoginVerifyRequest;
import io.inkHeart.dto.RegisterSrpRequest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    private static final String TEST_EMAIL = "testuser@example.com";
    private static final String TEST_PASSWORD = "my-strong-password-123";

    @Test
    @Order(1)
    void testRegisterUser() throws Exception {
        // Test-side Client Logic (for registration)
        SRP6CryptoParams cryptoParams = SRP6CryptoParams.getInstance(2048, "SHA-256");
        SRP6VerifierGenerator verifierGenerator = new SRP6VerifierGenerator(cryptoParams);
        byte[] salt = verifierGenerator.generateRandomSalt();
        BigInteger verifier = verifierGenerator.generateVerifier(salt, TEST_EMAIL.getBytes(StandardCharsets.UTF_8), TEST_PASSWORD.getBytes(StandardCharsets.UTF_8));

        RegisterSrpRequest request = new RegisterSrpRequest();
        request.setEmail(TEST_EMAIL);
        request.setSalt(Base64.getEncoder().encodeToString(salt));
        request.setVerifier(Base64.getEncoder().encodeToString(verifier.toByteArray()));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(TEST_EMAIL));
    }

    @Test
    @Order(2)
    void testFullLoginFlow() throws Exception {
        // LOGIN CHALLENGE
        LoginChallengeRequest challengeRequest = new LoginChallengeRequest();
        challengeRequest.setEmail(TEST_EMAIL);

        MvcResult challengeResult = mockMvc.perform(post("/api/auth/login/challenge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(challengeRequest)))
                .andExpect(status().isOk())
                .andReturn();

        // Extract salt and server public key 'B' from the response
        String responseBody = challengeResult.getResponse().getContentAsString();
        LoginChallengeResponse challengeResponse = objectMapper.readValue(responseBody, LoginChallengeResponse.class);
        BigInteger salt = new BigInteger(1, Base64.getDecoder().decode(challengeResponse.getSalt()));
        BigInteger serverPublicKeyB = new BigInteger(1, Base64.getDecoder().decode(challengeResponse.getServerPublicKeyB()));

        // Test-side Client Logic (SRP calculations)
        SRP6CryptoParams cryptoParams = SRP6CryptoParams.getInstance(2048, "SHA-256");
        SRP6ClientSession clientSession = new SRP6ClientSession();
        clientSession.step1(TEST_EMAIL, TEST_PASSWORD);
        // Use the salt and B from the server to compute the client proof M1
        clientSession.step2(cryptoParams, salt, serverPublicKeyB);

        // Get the client public key 'A' and proof 'M1'
        BigInteger clientPublicKeyA = clientSession.getPublicClientValue();
        BigInteger clientProofM1 = clientSession.getClientEvidenceMessage();


        // LOGIN VERIFY
        LoginVerifyRequest verifyRequest = new LoginVerifyRequest();
        verifyRequest.setEmail(TEST_EMAIL);
        verifyRequest.setClientPublicKey(Base64.getEncoder().encodeToString(clientPublicKeyA.toByteArray()));
        verifyRequest.setClientProof(Base64.getEncoder().encodeToString(clientProofM1.toByteArray()));

        mockMvc.perform(post("/api/auth/login/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Client is authenticated successfully"))
                .andExpect(jsonPath("$.jwtToken").isNotEmpty());
    }
}
