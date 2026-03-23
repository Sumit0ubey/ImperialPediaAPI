package com.imperialpedia.api.exception;

import com.imperialpedia.api.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception ex, HttpServletRequest request) {

        return new ResponseEntity<>(
                createErrorResponse(
                        500,
                        ex.getMessage(),
                        request.getRequestURI()
                ), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {

        return new ResponseEntity<>(
                createErrorResponse(
                        404,
                        ex.getMessage(),
                        request.getRequestURI()
                ), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handleAlreadyExists(ResourceAlreadyExistsException ex, HttpServletRequest request) {

        return new ResponseEntity<>(
                createErrorResponse(
                        409,
                        ex.getMessage(),
                        request.getRequestURI()
                ), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(IntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleIntegrityViolation(IntegrityViolationException ex, HttpServletRequest request) {

        return new ResponseEntity<>(
                createErrorResponse(
                        400,
                        ex.getMessage(),
                        request.getRequestURI()
                ), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgument(ArgumentException ex, HttpServletRequest request) {

        return new ResponseEntity<>(
                createErrorResponse(
                        400,
                        ex.getMessage(),
                        request.getRequestURI()
                ), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ApiResponse<Object>> handleTooManyRequest(TooManyRequestsException ex, HttpServletRequest request) {

        return new ResponseEntity<>(
                createErrorResponse(
                        429,
                        ex.getMessage(),
                        request.getRequestURI()
                ), HttpStatus.TOO_MANY_REQUESTS);
    }

    private ApiResponse<Object> createErrorResponse(int status, String message, String path) {
        ApiResponse<Object> response = ApiResponse.error(
                status,
                message
        );
        response.setPath(path);

        return response;
    }
}
