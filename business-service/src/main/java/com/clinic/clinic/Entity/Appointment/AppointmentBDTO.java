package com.clinic.clinic.Entity.Appointment;


import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentBDTO {
    Integer pacientId;
    Integer doctorId;
    Long appointmentId;
    String specialization;

    //    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm")
    LocalDateTime appointmentDate;

    public AppointmentBDTO(AppointmentEntity appointmentEntity) {
        this.appointmentDate=appointmentEntity.getDate();
        this.appointmentId=appointmentEntity.getId();
        this.pacientId=appointmentEntity.getPatientId().getId();
        this.specialization = appointmentEntity.getNeededSpecialization();
        if (appointmentEntity.getDoctorId() != null) {
            this.doctorId=appointmentEntity.getDoctorId().getId();
        }
    }
}
