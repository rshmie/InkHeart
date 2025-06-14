package io.inkHeart.cli.auth;

import com.nimbusds.srp6.SRP6CryptoParams;
import com.nimbusds.srp6.SRP6VerifierGenerator;
import io.inkHeart.cli.dto.RegisterSrpRequest;
import io.inkHeart.cli.util.JsonUtil;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import static io.inkHeart.cli.CliApplication.API_URL;

public class RegisterService {
    public HttpResponse<String> handleSignUp(String email, String password) throws IOException, InterruptedException {
        // 1. Get the standard crypto parameters (must be same on client and server)
        SRP6CryptoParams cryptoParams = SRP6CryptoParams.getInstance(2048, "SHA-256");

        // 2. Create a verifier generator - To Do: Use the custom hash function for the verifier later.
        SRP6VerifierGenerator verifierGenerator = new SRP6VerifierGenerator(cryptoParams);

        // 3. Generate Random Salt on the client.
        byte[] salt =  verifierGenerator.generateRandomSalt();
        // 4. Generate password verifier "v" and Encode salt and verifier into base64
        // v= g^hash(salt, identity, password) mod N
        var verifier = verifierGenerator.generateVerifier(salt, email.getBytes(StandardCharsets.UTF_8), password.getBytes(StandardCharsets.UTF_8));
        String saltBase64 = Base64.getEncoder().encodeToString(salt);
        String verifierBase64 = Base64.getEncoder().encodeToString(verifier.toByteArray());

        RegisterSrpRequest registerSrpRequest = new RegisterSrpRequest();
        registerSrpRequest.setEmail(email);
        registerSrpRequest.setSalt(saltBase64);
        registerSrpRequest.setVerifier(verifierBase64);

        var registerBody = JsonUtil.getObjectMapper().writeValueAsString(registerSrpRequest);

        HttpRequest registerRequest = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(registerBody))
                .build();

        return HttpClient.newBuilder().build().send(registerRequest, HttpResponse.BodyHandlers.ofString());

    }
}
