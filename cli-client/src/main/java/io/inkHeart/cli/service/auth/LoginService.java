package io.inkHeart.cli.service.auth;

import com.nimbusds.srp6.SRP6ClientCredentials;
import com.nimbusds.srp6.SRP6ClientSession;
import com.nimbusds.srp6.SRP6CryptoParams;
import com.nimbusds.srp6.SRP6Exception;
import io.inkHeart.cli.dto.FinalLoginResponse;
import io.inkHeart.cli.dto.LoginChallengeResponse;
import io.inkHeart.cli.dto.LoginVerifyRequest;
import io.inkHeart.cli.util.JsonUtil;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

import static io.inkHeart.cli.CliApplication.API_URL;

public class LoginService {
    public FinalLoginResponse handleLogin(String email, String password) throws IOException, InterruptedException {
        var loginChallengeResponse = getLoginChallengeResponse(email);
        if (loginChallengeResponse == null) {
            return new FinalLoginResponse("", "", "Login failed: Invalid email or server error.");
        }
        byte[] salt = Base64.getDecoder().decode(loginChallengeResponse.getSalt());
        BigInteger serverPublicKeyB = new BigInteger(Base64.getDecoder().decode(loginChallengeResponse.getServerPublicKeyB()));

        // Compute the proof and Verify ( M2 for mutual authentication - TO DO.)
        FinalLoginResponse finalLoginResponse = getLoginVerifyResponse(email, password, salt, serverPublicKeyB);
        if (finalLoginResponse == null) {
            return new FinalLoginResponse("", "", "Login failed!");
        }
        return finalLoginResponse;
    }

    /**
     * Sends the request to /login/challenge and gets response back from the server
     *
     * @param email - User ID used to log in
     * @return LoginChallengeResponse consisting of salt and server's Public Key Value B
     */
    private static LoginChallengeResponse getLoginChallengeResponse(String email) throws IOException, InterruptedException {
        String challengeRequestJson = "{\"email\":\"" + email + "\"}";

        HttpRequest challengeHttpRequest = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/login/challenge"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(challengeRequestJson))
                .build();

        // Send the request and get the response
        var challengeResponse = HttpClient.newBuilder().build().send(challengeHttpRequest,
                HttpResponse.BodyHandlers.ofString());
        if (challengeResponse.statusCode() != 200) {
            return null;
        }
        return JsonUtil.getObjectMapper().readValue(challengeResponse.body(), LoginChallengeResponse.class);
    }

    /**
     *
     * @param email - User id used for login
     * @param password - Password used for login
     * @param salt - Salt
     * @param serverPublicKeyB - server's Public key value B
     * @return FinalLoginResponse consisting of Server's proof M2 and JWT token upon successful verification from the server.

     * Client sends the requests to /login/verify once the challenge succeeds. It sends it's public Key,
     * and it's computed proof M1 to the server and server and server verifies it and upon success server sends back
     * the response with its proof M2 and JWT token
     *
     */
    private static FinalLoginResponse getLoginVerifyResponse(String email, String password, byte[] salt, BigInteger serverPublicKeyB) throws IOException, InterruptedException {
        SRP6CryptoParams cryptoParams = SRP6CryptoParams.getInstance(2048, "SHA-256");
        SRP6ClientSession clientSession = new SRP6ClientSession();
        clientSession.step1(email, password);

        // Computes the shared secret 'S' and the client's proof 'M1'
        SRP6ClientCredentials clientProof;
        try {
            clientProof = clientSession.step2(cryptoParams, new BigInteger(1, salt), serverPublicKeyB);
        } catch (SRP6Exception e) {
            return new FinalLoginResponse("", "", "Login failed, Invalid user credentials: " + e.getMessage()); // is printing e.getMessage needed?
        }

        String clientPublicKeyABase64 = Base64.getEncoder().encodeToString(clientProof.A.toByteArray());
        String clientProofM1Base64 = Base64.getEncoder().encodeToString(clientProof.M1.toByteArray());

        LoginVerifyRequest loginVerifyRequest = new LoginVerifyRequest();
        loginVerifyRequest.setEmail(email);
        loginVerifyRequest.setClientPublicKey(clientPublicKeyABase64);
        loginVerifyRequest.setClientProof(clientProofM1Base64);

        var verifyRequest = JsonUtil.getObjectMapper().writeValueAsString(loginVerifyRequest);
        HttpRequest verifyHttpRequest = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/login/verify"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(verifyRequest))
                .build();

        var loginVerifyResponse = HttpClient.newBuilder().build().send(verifyHttpRequest, HttpResponse.BodyHandlers.ofString());
        if (loginVerifyResponse.statusCode() != 200) {
            return new FinalLoginResponse("", "", "Login failed, Please check your password! Server returned with code: " + loginVerifyResponse.statusCode());
        }
        return JsonUtil.getObjectMapper().readValue(loginVerifyResponse.body(), FinalLoginResponse.class);

    }
}
