package com.clinic.clinic.global;

import static com.clinic.clinic.global.ErrorCodes.*;
import static org.springframework.http.HttpStatus.*;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Pre-populates an {@link ExceptionResponse} builder with the fields common to
     * every error response (HTTP status code, request path, ISO-8601 timestamp) so
     * every handler returns the same JSON shape.
     */
    private ExceptionResponse.ExceptionResponseBuilder base(
        HttpStatus status,
        HttpServletRequest request
    ) {
        return ExceptionResponse.builder()
            .status(status.value())
            .path(request.getRequestURI())
            .timestamp(OffsetDateTime.now().toString());
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ExceptionResponse> handleException(
        LockedException lockedException,
        HttpServletRequest request
    ) {
        log.warn("Account locked: {}", lockedException.getMessage());
        return ResponseEntity.status(UNAUTHORIZED).body(
            base(UNAUTHORIZED, request)
                .errorCode(ACCOUNT_LOCKED.getCode())
                .errorMessage(ACCOUNT_LOCKED.getMessage())
                .error(lockedException.getMessage())
                .build()
        );
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ExceptionResponse> handleException(
        DisabledException disabledException,
        HttpServletRequest request
    ) {
        log.warn("Account disabled: {}", disabledException.getMessage());
        return ResponseEntity.status(UNAUTHORIZED).body(
            base(UNAUTHORIZED, request)
                .errorCode(ACCOUNT_DISABLED.getCode())
                .errorMessage(ACCOUNT_DISABLED.getMessage())
                .error(disabledException.getMessage())
                .build()
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ExceptionResponse> handleException(
        BadCredentialsException badCredentialsException,
        HttpServletRequest request
    ) {
        log.warn("Authentication failed: bad credentials");
        return ResponseEntity.status(UNAUTHORIZED).body(
            base(UNAUTHORIZED, request)
                .errorCode(BAD_CREDENTIALS.getCode())
                .errorMessage(BAD_CREDENTIALS.getMessage())
                .error(BAD_CREDENTIALS.getMessage())
                .build()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ExceptionResponse> handleAccessDenied(
        AccessDeniedException ex,
        HttpServletRequest request
    ) {
        // Thrown by method security (@PreAuthorize) when an authenticated user lacks the
        // required authority. Without this handler it would fall through to the catch-all
        // and (wrongly) return 500 instead of 403.
        log.warn("Access denied on {}: {}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(FORBIDDEN).body(
            base(FORBIDDEN, request)
                .errorMessage("Access denied")
                .build()
        );
    }

    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<ExceptionResponse> handleException(
        MessagingException messagingException,
        HttpServletRequest request
    ) {
        log.error("Messaging failure while processing request", messagingException);
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(
            base(INTERNAL_SERVER_ERROR, request)
                .errorMessage("Internal error, please get in contact with us")
                .build()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleException(
        MethodArgumentNotValidException validationException,
        HttpServletRequest request
    ) {
        Set<String> errors = new HashSet<>();
        validationException
            .getBindingResult()
            .getAllErrors()
            .forEach(error -> {
                String errorMessage = error.getDefaultMessage();
                errors.add(errorMessage);
            });
        log.warn("Validation failed: {}", errors);
        return ResponseEntity.status(BAD_REQUEST).body(
            base(BAD_REQUEST, request)
                .errorMessage("Validation failed")
                .validationErrors(errors)
                .build()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleException(
        Exception exception,
        HttpServletRequest request
    ) {
        log.error("Unhandled exception", exception);
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(
            base(INTERNAL_SERVER_ERROR, request)
                .errorMessage("Internal error, please get in contact with us")
                .build()
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ExceptionResponse> handleDataIntegrityViolation(
        DataIntegrityViolationException ex,
        HttpServletRequest request
    ) {
        log.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());
        return ResponseEntity.status(CONFLICT).body(
            base(CONFLICT, request)
                .errorMessage("Datele trimise încalcă o constrângere.")
                .build()
        );
    }

    @ExceptionHandler(MedicationNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleMedicationNotFound(
        MedicationNotFoundException ex,
        HttpServletRequest request
    ) {
        log.warn("Medication not found: {}", ex.getMessage());
        return ResponseEntity.status(NOT_FOUND).body(
            base(NOT_FOUND, request).error(ex.getMessage()).build()
        );
    }

    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handlePatientNotFoundException(
        PatientNotFoundException ex,
        HttpServletRequest request
    ) {
        log.warn("Patient not found: {}", ex.getMessage());
        return ResponseEntity.status(NOT_FOUND).body(
            base(NOT_FOUND, request).error(ex.getMessage()).build()
        );
    }

    @ExceptionHandler(NotEnoughtMedicationException.class)
    public ResponseEntity<ExceptionResponse> handleNotEnoughMedication(
        NotEnoughtMedicationException ex,
        HttpServletRequest request
    ) {
        log.warn("Not enough medication: {}", ex.getMessage());
        return ResponseEntity.status(CONFLICT).body(
            base(CONFLICT, request).error(ex.getMessage()).build()
        );
    }

    @ExceptionHandler(PatientFileNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handlePatientFileNotFound(
        PatientFileNotFoundException ex,
        HttpServletRequest request
    ) {
        log.warn("Patient file not found: {}", ex.getMessage());
        return ResponseEntity.status(NOT_FOUND).body(
            base(NOT_FOUND, request).error(ex.getMessage()).build()
        );
    }

    @ExceptionHandler(AppointmentNotFound.class)
    public ResponseEntity<ExceptionResponse> handleAppointmentNotFound(
        AppointmentNotFound ex,
        HttpServletRequest request
    ) {
        log.warn("Appointment not found: {}", ex.getMessage());
        return ResponseEntity.status(NOT_FOUND).body(
            base(NOT_FOUND, request).error(ex.getMessage()).build()
        );
    }

    @ExceptionHandler(AppointmentAlreadyAcceptedException.class)
    public ResponseEntity<ExceptionResponse> handleAppointmentAlreadyAccepted(
        AppointmentAlreadyAcceptedException ex,
        HttpServletRequest request
    ) {
        log.warn("Appointment already accepted: {}", ex.getMessage());
        return ResponseEntity.status(CONFLICT).body(
            base(CONFLICT, request).error(ex.getMessage()).build()
        );
    }

    @ExceptionHandler(AppointmentNotMatchingException.class)
    public ResponseEntity<ExceptionResponse> handleAppointmentNotMatching(
        AppointmentNotMatchingException ex,
        HttpServletRequest request
    ) {
        log.warn("Appointment not matching: {}", ex.getMessage());
        return ResponseEntity.status(CONFLICT).body(
            base(CONFLICT, request).error(ex.getMessage()).build()
        );
    }

    @ExceptionHandler(MedicationAlreadyDiscontinued.class)
    public ResponseEntity<ExceptionResponse> handleMedicationAlreadyDiscontinued(
        MedicationAlreadyDiscontinued ex,
        HttpServletRequest request
    ) {
        log.warn("Medication already discontinued: {}", ex.getMessage());
        return ResponseEntity.status(CONFLICT).body(
            base(CONFLICT, request).error(ex.getMessage()).build()
        );
    }

    @ExceptionHandler(MedicationIsNotDiscontinued.class)
    public ResponseEntity<ExceptionResponse> handleMedicationIsNotDiscontinued(
        MedicationIsNotDiscontinued ex,
        HttpServletRequest request
    ) {
        log.warn("Medication is not discontinued: {}", ex.getMessage());
        return ResponseEntity.status(CONFLICT).body(
            base(CONFLICT, request).error(ex.getMessage()).build()
        );
    }

    @ExceptionHandler(UserIsNotDoctor.class)
    public ResponseEntity<ExceptionResponse> handleUserIsNotDoctor(
        UserIsNotDoctor ex,
        HttpServletRequest request
    ) {
        log.warn("User is not a doctor: {}", ex.getMessage());
        return ResponseEntity.status(FORBIDDEN).body(
            base(FORBIDDEN, request).error(ex.getMessage()).build()
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ExceptionResponse> handleInvalidFormat(
        HttpMessageNotReadableException ex,
        HttpServletRequest request
    ) {
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException invalidFormatException) {
            String fieldName = invalidFormatException
                .getPath()
                .get(0)
                .getFieldName();
            String invalidValue = invalidFormatException.getValue().toString();

            // Obținem clasa enumului încercat (AICI E MAGIA)
            Class<?> targetType = invalidFormatException.getTargetType();

            // Dacă e enum, extragem valorile acceptate
            String acceptedValues = "unknown";
            if (targetType.isEnum()) {
                Object[] constants = targetType.getEnumConstants();
                acceptedValues = Arrays.stream(constants)
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));
            }

            log.warn("Invalid value '{}' for field '{}'", invalidValue, fieldName);

            Map<String, String> details = new HashMap<>();
            details.put("field", fieldName);
            details.put("invalidValue", invalidValue);
            details.put("acceptedValues", acceptedValues);

            return ResponseEntity.status(BAD_REQUEST).body(
                base(BAD_REQUEST, request)
                    .errorMessage("Invalid value for field '" + fieldName + "'")
                    .validationErrorDetails(details)
                    .build()
            );
        }

        // fallback pentru alte cazuri de parse failure
        log.warn("Unreadable request body: {}", ex.getMessage());
        return ResponseEntity.status(BAD_REQUEST).body(
            base(BAD_REQUEST, request)
                .errorMessage("Invalid request format")
                .build()
        );
    }
}
