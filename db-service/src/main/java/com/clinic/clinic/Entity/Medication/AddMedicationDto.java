package com.clinic.clinic.Entity.Medication;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddMedicationDto {
    @NotEmpty(message = "Name of the medication is mandatory")
    @NotBlank(message = "Name of the medication is mandatory")
    private String name;
    @NotEmpty (message = "Description of the medication is mandatory")
    @NotBlank(message = "Description of the medication is mandatory")
    private String description;
    @NotNull(message = "The added quantity is mandatory")
    private Integer quantity;
}
