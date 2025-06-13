package io.inkHeart.service;

import com.nimbusds.srp6.SRP6CryptoParams;
import com.nimbusds.srp6.SRP6Exception;
import com.nimbusds.srp6.SRP6ServerSession;
import io.inkHeart.dto.LoginVerifyRequest;
import io.inkHeart.entity.User;
import io.inkHeart.exception.EmailAlreadyExistException;
import io.inkHeart.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final Map<String, SRP6ServerSession> srpSessionsMap; // Session cache to be improved
    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.srpSessionsMap = new ConcurrentHashMap<>();
    }

    public Map<String, SRP6ServerSession> getSrpSessionsMap() {
        return srpSessionsMap;
    }

    public User register(String email, String srpSalt, String passwordVerifier) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new EmailAlreadyExistException(email);
        }
        byte[] salt = Base64.getDecoder().decode(srpSalt);
        byte[] verifier = Base64.getDecoder().decode(passwordVerifier);
        User user = new User();
        user.setEmail(email);
        user.setSrpSalt(salt);
        user.setSrpVerifier(verifier);
        return userRepository.save(user);
    }

    public SRP6ServerSession startLoginChallenge(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email not found"));

        byte[] saltBytes = user.getSrpSalt();
        byte[] verifierBytes = user.getSrpVerifier();
        BigInteger salt = new BigInteger(1, saltBytes);
        BigInteger verifier = new BigInteger(1, verifierBytes);

        SRP6CryptoParams cryptoParams = SRP6CryptoParams.getInstance(2048, "SHA-256");
        SRP6ServerSession serverSession = new SRP6ServerSession(cryptoParams);
        serverSession.step1(email, salt, verifier); // Generates a servers public key B

        srpSessionsMap.put(email, serverSession);
        return serverSession;
    }

    public SRP6ServerSession verifyLogin(LoginVerifyRequest verifyRequest) throws SRP6Exception {
        userRepository.findByEmail(verifyRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Email not found"));

        SRP6ServerSession serverSession = srpSessionsMap.get(verifyRequest.getEmail());
        if (serverSession == null) {
            return null;
        }
        BigInteger clientPublicKeyA = new BigInteger(1, Base64.getDecoder().decode(verifyRequest.getClientPublicKey()));
        BigInteger clientProofM1 = new BigInteger(1, Base64.getDecoder().decode(verifyRequest.getClientProof()));
        serverSession.step2(clientPublicKeyA, clientProofM1);

        return serverSession;
    }
}
