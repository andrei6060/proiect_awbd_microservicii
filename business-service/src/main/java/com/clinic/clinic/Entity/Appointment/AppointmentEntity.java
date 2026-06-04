package com.clinic.clinic.Entity.Appointment;

import com.clinic.clinic.Entity.User.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "patient")
    @NonNull
    private User patientId;

    @ManyToOne
    @JoinColumn(name = "doctor")
    private User doctorId;
    private LocalDateTime date;
    @NonNull
    private String neededSpecialization;

}
