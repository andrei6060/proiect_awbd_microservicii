package com.clinic.clinic.global;

public class AppointmentNotFound extends RuntimeException {
    public AppointmentNotFound(Integer appointmentId) {
        super("Appointment with id " + appointmentId + " not found");
    }
}
