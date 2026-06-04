package com.clinic.clinic.Entity.Medication;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetMedicineDto {
    @NotNull(message = "Medicine Id is mandatory")
    private String name;
}
