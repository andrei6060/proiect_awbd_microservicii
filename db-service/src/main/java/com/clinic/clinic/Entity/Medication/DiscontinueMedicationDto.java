package com.clinic.clinic.Entity.Medication;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscontinueMedicationDto {
    @NotNull(message = "Id is mandatory")
    private String name;
}
