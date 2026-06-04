package com.clinic.clinic.global;

public class AppointmentAlreadyAcceptedException extends RuntimeException {
    public AppointmentAlreadyAcceptedException(Integer appointmentId, Integer doctorId) {
        super("Appointment with id " + appointmentId + " already accepted by doctor with id " + doctorId);
    }
}
