package io.inkHeart.exception;

public class EmailAlreadyExistException extends RuntimeException{
    public EmailAlreadyExistException(String email) {
        super("Email already in use : " + email);
    }
}
