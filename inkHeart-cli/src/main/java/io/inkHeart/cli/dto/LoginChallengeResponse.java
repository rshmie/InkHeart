package io.inkHeart.cli.dto;

public class LoginChallengeResponse {
    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getServerPublicKeyB() {
        return serverPublicKeyB;
    }

    public void setServerPublicKeyB(String serverPublicKeyB) {
        this.serverPublicKeyB = serverPublicKeyB;
    }

    private String salt; // Base64
    private String serverPublicKeyB; // 'B', Base64
}
