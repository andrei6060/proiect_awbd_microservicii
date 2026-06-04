package com.clinic.clinic.global;

public class NotEnoughtMedicationException extends RuntimeException {
    public NotEnoughtMedicationException(String name) {
        super("Not enough medication of type "+ name+ " is disponible");
    }
}
