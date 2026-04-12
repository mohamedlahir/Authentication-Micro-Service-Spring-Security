package com.authentication.service.auth.Exceptions;

import com.authentication.service.auth.DTO.ErrorResponseDTO;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Keep default handling for @Valid annotated request bodies
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> validationErrors = new HashMap<>();
        List<ObjectError> validationErrorList = ex.getBindingResult().getAllErrors();

        validationErrorList.forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String validationMsg = error.getDefaultMessage();
            validationErrors.put(fieldName, validationMsg);
        });
        return new ResponseEntity<>(validationErrors, HttpStatus.BAD_REQUEST);
    }

    // Handle malformed JSON / unreadable messages
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ErrorResponseDTO dto = buildErrorResponse("Malformed request: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
        return handleExceptionInternal(ex, dto, headers, HttpStatus.BAD_REQUEST, request);
    }

    // Handle unsupported media types
    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ErrorResponseDTO dto = buildErrorResponse("Unsupported media type: " + ex.getMessage(), HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        return handleExceptionInternal(ex, dto, headers, HttpStatus.UNSUPPORTED_MEDIA_TYPE, request);
    }

    // Handle missing servlet request parameters
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ErrorResponseDTO dto = buildErrorResponse("Missing request parameter: " + ex.getParameterName(), HttpStatus.BAD_REQUEST);
        return handleExceptionInternal(ex, dto, headers, HttpStatus.BAD_REQUEST, request);
    }

    // Validation exceptions from other places (e.g. @RequestParam, @PathVariable)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleConstraintViolation(ConstraintViolationException ex) {
        ErrorResponseDTO dto = buildErrorResponse("Validation failed: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(dto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserNameAndPasswordMismatchException(InvalidCredentialsException ex, WebRequest request) {
        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO();
        errorResponseDTO.setErrorMessage(ex.getMessage());
        errorResponseDTO.setErrorCode(403);
        return new ResponseEntity<>(errorResponseDTO, HttpStatus.FORBIDDEN);
    }

    // JWT specific exceptions
    @ExceptionHandler({ExpiredJwtException.class, MalformedJwtException.class, SignatureException.class})
    public ResponseEntity<ErrorResponseDTO> handleJwtExceptions(Exception ex) {
        ErrorResponseDTO dto = buildErrorResponse("Invalid or expired token: " + ex.getMessage(), HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(dto, HttpStatus.UNAUTHORIZED);
    }

    // Spring Security exceptions
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDTO> handleAuthenticationException(AuthenticationException ex) {
        ErrorResponseDTO dto = buildErrorResponse("Authentication failed: " + ex.getMessage(), HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(dto, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDenied(AccessDeniedException ex) {
        ErrorResponseDTO dto = buildErrorResponse("Access denied: " + ex.getMessage(), HttpStatus.FORBIDDEN);
        return new ResponseEntity<>(dto, HttpStatus.FORBIDDEN);
    }

    // Specific handler for method argument type mismatches (not handled by the base class)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ErrorResponseDTO dto = buildErrorResponse("Type mismatch: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(dto, HttpStatus.BAD_REQUEST);
    }

    // Data access exceptions
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponseDTO> handleDataAccessException(DataAccessException ex) {
        ErrorResponseDTO dto = buildErrorResponse("Database error: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(dto, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Illegal arguments
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgument(IllegalArgumentException ex) {
        ErrorResponseDTO dto = buildErrorResponse("Invalid argument: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(dto, HttpStatus.BAD_REQUEST);
    }

    // Fallback handling for exceptions routed through ResponseEntityExceptionHandler
    // Override handleExceptionInternal so the base class handlers (and the final
    // handleException) will produce consistent ErrorResponseDTO responses.
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        logger.error("Handled exception: {}", ex.getMessage(), ex);
        HttpStatus httpStatus = HttpStatus.resolve(status.value());
        if (httpStatus == null) {
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        ErrorResponseDTO dto;
        if (body instanceof ErrorResponseDTO) {
            dto = (ErrorResponseDTO) body;
        } else {
            dto = buildErrorResponse("Unexpected error: " + ex.getMessage(), httpStatus);
        }

        return new ResponseEntity<>(dto, headers, httpStatus);
    }

    // Helper to build consistent error responses
    private ErrorResponseDTO buildErrorResponse(String message, HttpStatus status) {
        ErrorResponseDTO dto = new ErrorResponseDTO();
        dto.setErrorMessage(message);
        dto.setErrorCode(status.value());
        return dto;
    }
}
