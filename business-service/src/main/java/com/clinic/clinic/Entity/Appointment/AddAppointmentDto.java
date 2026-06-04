package com.clinic.clinic.Entity.Appointment;

import com.clinic.clinic.Entity.User.Specializations;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddAppointmentDto {

    @NotNull(message = "Date is mandatory")
    private LocalDateTime date;

    @NotNull(message = "Specialization is mandatory")
    private Specializations neededSpecialization;
}
