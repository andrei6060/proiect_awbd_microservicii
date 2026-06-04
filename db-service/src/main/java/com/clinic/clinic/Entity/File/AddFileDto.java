package com.clinic.clinic.Entity.File;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddFileDto {
    @NotNull(message = "Patient ID is mandatory")
    private Integer patientId;
    private BloodType bloodType;
}
