package io.inkHeart.dto;

public class RegisterSrpRequest {
    private String email;
    private String salt; // Hex representation of BigInteger
    private String verifier; // Hex representation of BigInteger

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getVerifier() {
        return verifier;
    }

    public void setVerifier(String verifier) {
        this.verifier = verifier;
    }

}
