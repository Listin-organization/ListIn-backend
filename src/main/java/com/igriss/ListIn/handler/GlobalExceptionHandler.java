package com.igriss.ListIn.handler;


import com.igriss.ListIn.exceptions.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.igriss.ListIn.handler.BusinessErrorCodes.*;
import static org.springframework.http.HttpStatus.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ExceptionResponse> handleException(RateLimitExceededException exception) {
        return ResponseEntity
                .status(TOO_MANY_REQUESTS)
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(TOO_MANY_ATTEMPTS.getCode())
                                .businessErrorDescription(TOO_MANY_ATTEMPTS.getDescription())
                                .errorMessage(exception.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(EmailVerificationFailedException.class)
    public ResponseEntity<ExceptionResponse> handleException(EmailVerificationFailedException exception) {
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(EMAIL_CONFIRMATION_FAILED.getCode())
                                .businessErrorDescription(EMAIL_CONFIRMATION_FAILED.getDescription())
                                .errorMessage(exception.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(EmailNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleException(EmailNotFoundException exception) {
        return ResponseEntity.status(NOT_FOUND).body(
                ExceptionResponse.builder()
                        .businessErrorCode(EMAIL_NOT_FOUND.getCode())
                        .businessErrorDescription(EMAIL_NOT_FOUND.getDescription())
                        .errorMessage(exception.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(LoadHTMLTemplateFailedException.class)
    public ResponseEntity<ExceptionResponse> handleException(LoadHTMLTemplateFailedException exception) {
        return ResponseEntity.status(SERVICE_UNAVAILABLE).body(
                ExceptionResponse.builder()
                        .businessErrorCode(EMAIL_TEMPLATE_LOAD_FAILED.getCode())
                        .businessErrorDescription(EMAIL_TEMPLATE_LOAD_FAILED.getDescription())
                        .errorMessage(exception.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleException(ResourceNotFoundException exception) {
        return ResponseEntity.status(NOT_FOUND).body(
                ExceptionResponse.builder()
                        .businessErrorCode(ATTRIBUTES_NOT_FOUND.getCode())
                        .businessErrorDescription(ATTRIBUTES_NOT_FOUND.getDescription())
                        .errorMessage(exception.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(SearchQueryException.class)
    public ResponseEntity<ExceptionResponse> handleException(SearchQueryException exception) {
        return ResponseEntity.status(BAD_REQUEST).body(
                ExceptionResponse.builder()
                        .businessErrorCode(SEARCH_QUERY_FAILED.getCode())
                        .businessErrorDescription(SEARCH_QUERY_FAILED.getDescription())
                        .errorMessage(exception.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(UserHasAccountException.class)
    public ResponseEntity<ExceptionResponse> handleException(UserHasAccountException exception) {
        return ResponseEntity.status(CONFLICT).body(
                ExceptionResponse.builder()
                        .businessErrorCode(USER_HAS_ACCOUNT.getCode())
                        .businessErrorDescription(USER_HAS_ACCOUNT.getDescription())
                        .errorMessage(exception.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ExceptionResponse> handleException(ValidationException exception) {
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(
                ExceptionResponse.builder()
                        .businessErrorCode(ATTRIBUTE_VALUES_NOT_FOUND.getCode())
                        .businessErrorDescription(ATTRIBUTE_VALUES_NOT_FOUND.getDescription())
                        .errorMessage(exception.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(PublicationNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleException(PublicationNotFoundException exception){
        return ResponseEntity.status(NO_CONTENT).body(
          ExceptionResponse.builder()
                  .businessErrorCode(NO_PUBLICATION.getCode())
                  .businessErrorDescription(NO_PUBLICATION.getDescription())
                  .errorMessage(exception.getMessage())
                  .build()
        );
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleException(UserNotFoundException exception){
        return ResponseEntity.status(NOT_FOUND).body(
                ExceptionResponse.builder()
                        .businessErrorCode(USER_NOT_FOUND.getCode())
                        .businessErrorDescription(USER_NOT_FOUND.getDescription())
                        .errorMessage(exception.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleException(Exception exception) {
        // Log the exception with full stack trace
        log.error("An exception occurred: ", exception);

        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(
                        ExceptionResponse.builder()
                                .businessErrorDescription("Internal error, contact the admin")
                                .errorMessage(exception.getMessage())
                                .build()
                );
    }
}
