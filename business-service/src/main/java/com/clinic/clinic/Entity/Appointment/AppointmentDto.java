package com.clinic.clinic.Entity.Appointment;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentDto {
    String pacientId;
    String doctorId;
    Long appointmentId;
    String specialization;

//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm")
    LocalDateTime appointmentDate;

    public AppointmentDto(AppointmentEntity appointmentEntity) {
        this.appointmentDate=appointmentEntity.getDate();
        this.appointmentId=appointmentEntity.getId();
        this.pacientId=appointmentEntity.getPatientId().getFullName();
        this.specialization = appointmentEntity.getNeededSpecialization();
        if (appointmentEntity.getDoctorId() != null) {
            this.doctorId=appointmentEntity.getDoctorId().getFullName();
        }
    }
}
