package org.example.socialmedia_services.exception;
import jakarta.servlet.http.HttpServletRequest;
import org.example.socialmedia_services.dto.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataAccessException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import jakarta.validation.ConstraintViolationException;
import jakarta.persistence.EntityNotFoundException;


@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ApiError> handleBadRequestException(BadRequestException ex, HttpServletRequest request) {
    ApiError apiError = new ApiError(
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            ex.getMessage(),
            request.getRequestURI()
    );
    return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(TokenException.class)
  public ResponseEntity<ApiError> handleTokenException(TokenException ex, HttpServletRequest request) {
    ApiError apiError = new ApiError(
            HttpStatus.UNAUTHORIZED.value(),
            "Token Invalid",
            ex.getMessage(),
            request.getRequestURI()
    );
    return new ResponseEntity<>(apiError, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(UsernameNotFoundException.class)
  public ResponseEntity<ApiError> handleUsernameNotFoundException(UsernameNotFoundException ex, HttpServletRequest request) {
    ApiError apiError = new ApiError(
            HttpStatus.UNAUTHORIZED.value(),
            "Authentication Failed",
            "Email not found",
            request.getRequestURI()
    );
    return new ResponseEntity<>(apiError, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ApiError> handleBadCredentialsException(BadCredentialsException ex, HttpServletRequest request) {
    ApiError apiError = new ApiError(
            HttpStatus.UNAUTHORIZED.value(),
            "Authentication Failed",
            ex.getMessage(),  // ✅ Use the actual exception message!
            request.getRequestURI()
    );
    return new ResponseEntity<>(apiError, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
    // Collect all validation errors
    StringBuilder errorMessage = new StringBuilder();
    ex.getBindingResult().getAllErrors().forEach(error -> {
      String fieldName = ((FieldError) error).getField();
      String message = error.getDefaultMessage();
      errorMessage.append(fieldName).append(": ").append(message).append("; ");
    });

    // Remove trailing "; " if present
    String finalMessage = errorMessage.length() > 0 ?
            errorMessage.substring(0, errorMessage.length() - 2) :
            "Validation failed";

    ApiError apiError = new ApiError(
            HttpStatus.BAD_REQUEST.value(),
            "Validation Failed",
            finalMessage,
            request.getRequestURI()
    );

    return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
  }


  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleGlobalException(Exception ex, HttpServletRequest request) {
    ApiError apiError = new ApiError(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "An unexpected error occurred",
            request.getRequestURI()
    );
    return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ApiError> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
    String message = "Data integrity constraint violated";
    if (ex.getMessage().contains("Duplicate entry")) {
      message = "Duplicate entry - this record already exists";
    } else if (ex.getMessage().contains("foreign key constraint")) {
      message = "Cannot perform operation - referenced data exists";
    } else if (ex.getMessage().contains("not-null")) {
      message = "Required field cannot be empty";
    }

    ApiError apiError = new ApiError(
            HttpStatus.CONFLICT.value(),
            "Data Integrity Error",
            message,
            request.getRequestURI()
    );
    return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<ApiError> handleEntityNotFoundException(EntityNotFoundException ex, HttpServletRequest request) {
    ApiError apiError = new ApiError(
            HttpStatus.NOT_FOUND.value(),
            "Resource Not Found",
            ex.getMessage() != null ? ex.getMessage() : "The requested resource was not found",
            request.getRequestURI()
    );
    return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiError> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
    ApiError apiError = new ApiError(
            HttpStatus.FORBIDDEN.value(),
            "Access Denied",
            "You don't have permission to access this resource",
            request.getRequestURI()
    );
    return new ResponseEntity<>(apiError, HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ApiError> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
    ApiError apiError = new ApiError(
            HttpStatus.METHOD_NOT_ALLOWED.value(),
            "Method Not Allowed",
            "HTTP method '" + ex.getMethod() + "' is not supported for this endpoint",
            request.getRequestURI()
    );
    return new ResponseEntity<>(apiError, HttpStatus.METHOD_NOT_ALLOWED);
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ApiError> handleMissingParameter(MissingServletRequestParameterException ex, HttpServletRequest request) {
    ApiError apiError = new ApiError(
            HttpStatus.BAD_REQUEST.value(),
            "Missing Parameter",
            "Required parameter '" + ex.getParameterName() + "' is missing",
            request.getRequestURI()
    );
    return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
    StringBuilder errorMessage = new StringBuilder();
    ex.getConstraintViolations().forEach(violation -> {
      errorMessage.append(violation.getPropertyPath()).append(": ").append(violation.getMessage()).append("; ");
    });

    String finalMessage = errorMessage.length() > 0 ?
            errorMessage.substring(0, errorMessage.length() - 2) :
            "Validation constraint violated";

    ApiError apiError = new ApiError(
            HttpStatus.BAD_REQUEST.value(),
            "Constraint Violation",
            finalMessage,
            request.getRequestURI()
    );
    return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
    ApiError apiError = new ApiError(
            HttpStatus.BAD_REQUEST.value(),
            "Invalid Argument",
            ex.getMessage() != null ? ex.getMessage() : "Invalid argument provided",
            request.getRequestURI()
    );
    return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(DataAccessException.class)
  public ResponseEntity<ApiError> handleDataAccessException(DataAccessException ex, HttpServletRequest request) {
    ApiError apiError = new ApiError(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Database Error",
            "A database error occurred while processing your request",
            request.getRequestURI()
    );
    return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(NoHandlerFoundException.class)
  public ResponseEntity<ApiError> handleNoHandlerFound(NoHandlerFoundException ex, HttpServletRequest request) {
    ApiError apiError = new ApiError(
            HttpStatus.NOT_FOUND.value(),
            "Endpoint Not Found",
            "The requested endpoint '" + ex.getRequestURL() + "' was not found",
            request.getRequestURI()
    );
    return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
  }
}
