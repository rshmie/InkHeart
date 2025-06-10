package io.inkHeart.dto;

public record FinalLoginResponse (String serverProofM2, String jwtToken,
                                  String message) {
}
