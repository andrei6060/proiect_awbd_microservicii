package com.clinic.clinic.Entity.Medication;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GiveMedicationDto {
    @NotNull(message = "Patient id is mandatory")
    private Integer idPatient;
    @NotNull(message = "Medication name is mandatory")
    private String medicationName;
    @NotNull(message = "Medicine quantity is mandatory")
    private Integer quantity;
}
