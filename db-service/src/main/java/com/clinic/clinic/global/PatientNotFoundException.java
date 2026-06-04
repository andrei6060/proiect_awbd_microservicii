package com.clinic.clinic.global;

public class PatientNotFoundException extends RuntimeException {
    public PatientNotFoundException(Integer id) {
        super("Pacientul cu id-ul '" + id + "' nu a fost găsit.");
    }
}