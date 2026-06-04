package com.clinic.clinic.global;

public class AppointmentNotMatchingException extends RuntimeException {
    public AppointmentNotMatchingException(Integer appointmentId, String specialization) {
        super("Appointment " + appointmentId + " not matching " + specialization);
    }
}
