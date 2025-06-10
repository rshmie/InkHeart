package io.inkHeart.dto;

public class LoginVerifyRequest {
    private String email;
    private String clientPublicKey; // 'A'
    private String clientProof;     // 'M1'
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getClientPublicKey() {
        return clientPublicKey;
    }

    public void setClientPublicKey(String clientPublicKey) {
        this.clientPublicKey = clientPublicKey;
    }

    public String getClientProof() {
        return clientProof;
    }

    public void setClientProof(String clientProof) {
        this.clientProof = clientProof;
    }


}
