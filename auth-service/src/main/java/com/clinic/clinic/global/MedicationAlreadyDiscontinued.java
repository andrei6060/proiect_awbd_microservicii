package com.clinic.clinic.global;

public class MedicationAlreadyDiscontinued extends RuntimeException {
    public MedicationAlreadyDiscontinued() {
        super("Medication is discontinued");
    }
}
