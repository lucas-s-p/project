package com.lucas.landmarketplace.exception;

import java.util.stream.Collectors;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.lucas.landmarketplace.dto.ApiErrorResponse;
import com.lucas.landmarketplace.validation.NoOverlap;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LandPlotNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(LandPlotNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
    }

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public ResponseEntity<ApiErrorResponse> handleEmailAlreadyRegistered(EmailAlreadyRegisteredException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiErrorResponse(HttpStatus.CONFLICT.value(), ex.getMessage()));
    }

    @ExceptionHandler(InvalidVerificationTokenException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidVerificationToken(InvalidVerificationTokenException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
    }

    @ExceptionHandler(InvalidPasswordResetTokenException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidPasswordResetToken(InvalidPasswordResetTokenException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
    }

    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<ApiErrorResponse> handleEmailNotVerified(EmailNotVerifiedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), ex.getMessage()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Invalid email or password"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        boolean isOverlap = ex.getBindingResult().getGlobalErrors().stream()
                .anyMatch(error -> NoOverlap.class.getSimpleName().equals(error.getCode()));

        HttpStatus status = isOverlap ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
        String message = ex.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("; "));

        return ResponseEntity.status(status).body(new ApiErrorResponse(status.value(), message));
    }
}
