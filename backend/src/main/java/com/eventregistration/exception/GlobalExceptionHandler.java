package com.eventregistration.exception;

import java.time.Instant;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.eventregistration.dto.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException exception, HttpServletRequest request
    ) {
        return clientError(HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(
            BadRequestException exception, HttpServletRequest request
    ) {
        return clientError(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, exception.getMessage(), request);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(
            ConflictException exception, HttpServletRequest request
    ) {
        return clientError(HttpStatus.CONFLICT, ErrorCode.CONFLICT, exception.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException exception, HttpServletRequest request
    ) {
        List<ErrorResponse.FieldError> fieldErrors = exception.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldError)
                .toList();
        return clientError(
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_ERROR,
                "Please correct the highlighted fields.",
                request,
                fieldErrors
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception, HttpServletRequest request
    ) {
        List<ErrorResponse.FieldError> fieldErrors = exception.getConstraintViolations().stream()
                .map(violation -> new ErrorResponse.FieldError(
                        violation.getPropertyPath().toString(), violation.getMessage()))
                .toList();
        return clientError(
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_ERROR,
                "Please correct the highlighted fields.",
                request,
                fieldErrors
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(
            HttpMessageNotReadableException exception, HttpServletRequest request
    ) {
        return clientError(
                HttpStatus.BAD_REQUEST,
                ErrorCode.INVALID_REQUEST_BODY,
                "Invalid or malformed request body. Check date formats and allowed enum values.",
                request
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception, HttpServletRequest request
    ) {
        String parameter = exception.getName();
        return clientError(
                HttpStatus.BAD_REQUEST,
                ErrorCode.INVALID_PARAMETER,
                "Invalid value for parameter '" + parameter + "'.",
                request
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(
            MissingServletRequestParameterException exception, HttpServletRequest request
    ) {
        String parameter = exception.getParameterName();
        return clientError(
                HttpStatus.BAD_REQUEST,
                ErrorCode.MISSING_PARAMETER,
                "Required parameter '" + parameter + "' is missing.",
                request
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException exception, HttpServletRequest request
    ) {
        return clientError(
                HttpStatus.METHOD_NOT_ALLOWED,
                ErrorCode.METHOD_NOT_ALLOWED,
                "This request method is not supported for the requested resource.",
                request
        );
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(
            HttpMediaTypeNotSupportedException exception, HttpServletRequest request
    ) {
        return clientError(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                ErrorCode.UNSUPPORTED_MEDIA_TYPE,
                "The request content type is not supported.",
                request
        );
    }

    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(Exception exception, HttpServletRequest request) {
        return clientError(
                HttpStatus.NOT_FOUND,
                ErrorCode.RESOURCE_NOT_FOUND,
                "The requested resource was not found.",
                request
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(
            DataIntegrityViolationException exception, HttpServletRequest request
    ) {
        return clientError(
                HttpStatus.CONFLICT,
                ErrorCode.DATA_INTEGRITY_VIOLATION,
                "This request conflicts with existing data.",
                request
        );
    }

    @ExceptionHandler(PessimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleLockFailure(
            PessimisticLockingFailureException exception, HttpServletRequest request
    ) {
        return serverError(
                HttpStatus.SERVICE_UNAVAILABLE,
                ErrorCode.SERVICE_BUSY,
                "The system is busy. Please try again in a moment.",
                exception,
                request
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException exception, HttpServletRequest request
    ) {
        return clientError(
                HttpStatus.FORBIDDEN,
                ErrorCode.ACCESS_DENIED,
                "You do not have permission to perform this action.",
                request
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(
            AuthenticationException exception, HttpServletRequest request
    ) {
        String message = exception instanceof BadCredentialsException
                ? "Invalid email or password."
                : "Please log in to continue.";
        ErrorCode errorCode = exception instanceof BadCredentialsException
                ? ErrorCode.AUTHENTICATION_FAILED
                : ErrorCode.AUTHENTICATION_REQUIRED;
        return clientError(HttpStatus.UNAUTHORIZED, errorCode, message, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(
            Exception exception, HttpServletRequest request
    ) {
        return serverError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_SERVER_ERROR,
                "Something went wrong on our side. Please try again later.",
                exception,
                request,
                List.of()
        );
    }

    private ErrorResponse.FieldError toFieldError(FieldError error) {
        return new ErrorResponse.FieldError(error.getField(), error.getDefaultMessage());
    }

    private ResponseEntity<ErrorResponse> clientError(
            HttpStatus status, ErrorCode errorCode, String message, HttpServletRequest request
    ) {
        return clientError(status, errorCode, message, request, List.of());
    }

    private ResponseEntity<ErrorResponse> clientError(
            HttpStatus status,
            ErrorCode errorCode,
            String message,
            HttpServletRequest request,
            List<ErrorResponse.FieldError> fieldErrors
    ) {
        log.warn("Request failed with status {} for {} ({})", status.value(), request.getRequestURI(), errorCode);
        return response(status, errorCode, message, request, fieldErrors);
    }

    private ResponseEntity<ErrorResponse> serverError(
            HttpStatus status,
            ErrorCode errorCode,
            String message,
            Exception exception,
            HttpServletRequest request,
            List<ErrorResponse.FieldError> fieldErrors
    ) {
        log.error("Server error {} while processing request to {}", status.value(), request.getRequestURI(), exception);
        return response(status, errorCode, message, request, fieldErrors);
    }

    private ResponseEntity<ErrorResponse> serverError(
            HttpStatus status,
            ErrorCode errorCode,
            String message,
            Exception exception,
            HttpServletRequest request
    ) {
        return serverError(status, errorCode, message, exception, request, List.of());
    }

    private ResponseEntity<ErrorResponse> response(
            HttpStatus status,
            ErrorCode errorCode,
            String message,
            HttpServletRequest request,
            List<ErrorResponse.FieldError> fieldErrors
    ) {
        ErrorResponse body = new ErrorResponse(
                false,
                message,
                errorCode.name(),
                status.value(),
                request.getRequestURI(),
                Instant.now(),
                fieldErrors
        );
        return ResponseEntity.status(status).body(body);
    }
}
