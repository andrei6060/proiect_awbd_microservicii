package com.clinic.clinic.global;

public class PatientFileNotFoundException extends RuntimeException {
    public PatientFileNotFoundException(Integer id) {
        super("Patient file not found for id " + id);
    }
}
