package com.clinic.clinic.Entity.File;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetFileDto {
    @NotNull(message = "Patient cannot be null")
    Integer patientId;


}
