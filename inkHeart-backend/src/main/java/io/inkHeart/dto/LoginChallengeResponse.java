package io.inkHeart.dto;

public class LoginChallengeResponse {
    private String salt; // Base64
    private String serverPublicKeyB; // 'B', Base64
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

}
