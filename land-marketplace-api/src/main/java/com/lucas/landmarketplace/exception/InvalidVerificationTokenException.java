package com.lucas.landmarketplace.exception;

public class InvalidVerificationTokenException extends RuntimeException {

    public InvalidVerificationTokenException() {
        super("This verification link is invalid or has expired");
    }
}
