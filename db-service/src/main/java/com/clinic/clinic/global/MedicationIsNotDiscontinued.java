package com.clinic.clinic.global;

public class MedicationIsNotDiscontinued extends RuntimeException {
    public MedicationIsNotDiscontinued() {
        super("Medication is not discontinued");
    }
}
