package com.clinic.clinic.global;

public class MedicationNotFoundException extends RuntimeException {
    public MedicationNotFoundException(String name) {
        super("Medicamentul cu numele '" + name + "' nu a fost găsit.");
    }
}
