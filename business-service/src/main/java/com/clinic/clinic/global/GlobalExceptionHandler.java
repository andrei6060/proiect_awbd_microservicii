package com.clinic.clinic.global;

import static com.clinic.clinic.global.ErrorCodes.*;
import static org.springframework.http.HttpStatus.*;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.mail.MessagingException;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ExceptionResponse> handleException(
        LockedException lockedException
    ) {
        log.warn("Account locked: {}", lockedException.getMessage());
        return ResponseEntity.status(UNAUTHORIZED).body(
            ExceptionResponse.builder()
                .errorCode(ACCOUNT_LOCKED.getCode())
                .errorMessage(ACCOUNT_LOCKED.getMessage())
                .error(lockedException.getMessage())
                .build()
        );
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ExceptionResponse> handleException(
        DisabledException disabledException
    ) {
        log.warn("Account disabled: {}", disabledException.getMessage());
        return ResponseEntity.status(UNAUTHORIZED).body(
            ExceptionResponse.builder()
                .errorCode(ACCOUNT_DISABLED.getCode())
                .errorMessage(ACCOUNT_DISABLED.getMessage())
                .error(disabledException.getMessage())
                .build()
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ExceptionResponse> handleException(
        BadCredentialsException badCredentialsException
    ) {
        log.warn("Authentication failed: bad credentials");
        return ResponseEntity.status(UNAUTHORIZED).body(
            ExceptionResponse.builder()
                .errorCode(BAD_CREDENTIALS.getCode())
                .errorMessage(BAD_CREDENTIALS.getMessage())
                .error(BAD_CREDENTIALS.getMessage())
                .build()
        );
    }

    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<ExceptionResponse> handleException(
        MessagingException lockedException
    ) {
        log.error("Messaging failure while processing request", lockedException);
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(
            ExceptionResponse.builder()
                .error(lockedException.getMessage())
                .build()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleException(
        MethodArgumentNotValidException lockedException
    ) {
        Set<String> errors = new HashSet<>();
        lockedException
            .getBindingResult()
            .getAllErrors()
            .forEach(error -> {
                String errorMessage = error.getDefaultMessage();
                errors.add(errorMessage);
            });
        log.warn("Validation failed: {}", errors);
        return ResponseEntity.status(BAD_REQUEST).body(
            ExceptionResponse.builder()
                .validationErrors(errors)
                .errorCode(ACCOUNT_LOCKED.getCode())
                .build()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleException(
        Exception lockedException
    ) {
        log.error("Unhandled server error", lockedException);
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(
            ExceptionResponse.builder()
                .errorMessage("Internal error, please get in contact with us")
                .error(lockedException.getMessage())
                .build()
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrityViolation(
        DataIntegrityViolationException ex
    ) {
        log.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            "Datele trimise încalcă o constrângere."
        );
    }

    @ExceptionHandler(MedicationNotFoundException.class)
    public ResponseEntity<?> handleMedicationNotFound(
        MedicationNotFoundException ex
    ) {
        log.warn("Medication not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ex.getMessage()
        );
    }

    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<?> handlePatientNotFoundException(
        PatientNotFoundException ex
    ) {
        log.warn("Patient not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ex.getMessage()
        );
    }

    @ExceptionHandler(NotEnoughtMedicationException.class)
    public ResponseEntity<?> handlePatientNotFoundException(
        NotEnoughtMedicationException ex
    ) {
        log.warn("Not enough medication: {}", ex.getMessage());
        return ResponseEntity.status(FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(PatientFileNotFoundException.class)
    public ResponseEntity<?> handlePatientFileNotFound(
        PatientFileNotFoundException ex
    ) {
        log.warn("Patient file not found: {}", ex.getMessage());
        return ResponseEntity.status(NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(AppointmentNotFound.class)
    public ResponseEntity<?> handleAppointmentNotFound(AppointmentNotFound ex) {
        log.warn("Appointment not found: {}", ex.getMessage());
        return ResponseEntity.status(NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(AppointmentAlreadyAcceptedException.class)
    public ResponseEntity<?> handleAppointmentAlreadyAccepted(
        AppointmentAlreadyAcceptedException ex
    ) {
        log.warn("Appointment already accepted: {}", ex.getMessage());
        return ResponseEntity.status(NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(AppointmentNotMatchingException.class)
    public ResponseEntity<?> handleAppointmentNotMatching(
        AppointmentNotMatchingException ex
    ) {
        log.warn("Appointment not matching: {}", ex.getMessage());
        return ResponseEntity.status(NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(MedicationAlreadyDiscontinued.class)
    public ResponseEntity<?> handleMedicationAlreadyDiscontinued(
        MedicationAlreadyDiscontinued ex
    ) {
        log.warn("Medication already discontinued: {}", ex.getMessage());
        return ResponseEntity.status(NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(MedicationIsNotDiscontinued.class)
    public ResponseEntity<?> handleMedicationIsNotDiscontinued(
        MedicationIsNotDiscontinued ex
    ) {
        log.warn("Medication is not discontinued: {}", ex.getMessage());
        return ResponseEntity
                .status(NOT_FOUND)
                .body(ex.getMessage());
    }

    @ExceptionHandler(UserIsNotDoctor.class)
    public ResponseEntity<?> handleUserIsNotDoctor(
        UserIsNotDoctor ex
    ) {
        log.warn("User is not a doctor: {}", ex.getMessage());
        return ResponseEntity.status(NOT_FOUND).body(ex.getMessage());
    }

    //    @ExceptionHandler(InvalidFormatException.class)
    //    public ResponseEntity<Map<String, String>> handleInvalidFormat(InvalidFormatException ex) {
    //        String fieldName = ex.getPath().get(0).getFieldName();
    //        String invalidValue = ex.getValue().toString();
    //
    //        Map<String, String> response = new HashMap<>();
    //        response.put("error", "Invalid value for field '" + fieldName + "'");
    //        response.put("invalidValue", invalidValue);
    //        response.put("acceptedValues", "A_POSITIVE, A_NEGATIVE, B_POSITIVE, B_NEGATIVE, O_POSITIVE, O_NEGATIVE, AB_POSITIVE, AB_NEGATIVE, unknown");
    //
    //        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    //    }

    //    @ExceptionHandler(HttpMessageNotReadableException.class)
    //    public ResponseEntity<Map<String, String>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
    //        Throwable cause = ex.getCause();
    //
    //        if (cause instanceof InvalidFormatException invalidFormatException) {
    //            String fieldName = invalidFormatException.getPath().get(0).getFieldName();
    //            String invalidValue = invalidFormatException.getValue().toString();
    //
    //            Map<String, String> response = new HashMap<>();
    //            response.put("error", "Invalid value for field '" + fieldName + "'");
    //            response.put("invalidValue", invalidValue);
    //            response.put("acceptedValues", "A_POSITIVE, A_NEGATIVE, B_POSITIVE, B_NEGATIVE, O_POSITIVE, O_NEGATIVE, AB_POSITIVE, AB_NEGATIVE, unknown");
    //
    //            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    //        }
    //
    //        // fallback pentru alte cazuri de parse failure
    //        return new ResponseEntity<>(
    //                Map.of("error", "Invalid request format"),
    //                HttpStatus.BAD_REQUEST
    //        );
    //    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleInvalidFormat(
        HttpMessageNotReadableException ex
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

            Map<String, String> response = new HashMap<>();
            response.put(
                "error",
                "Invalid value for field '" + fieldName + "'"
            );
            response.put("invalidValue", invalidValue);
            response.put("acceptedValues", acceptedValues);

            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        // fallback pentru alte cazuri de parse failure
        log.warn("Unreadable request body: {}", ex.getMessage());
        return new ResponseEntity<>(
            Map.of("error", "Invalid request format"),
            HttpStatus.BAD_REQUEST
        );
    }
}
