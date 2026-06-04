package com.clinic.clinic.Entity.Medication;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicineResponseDto {
    private String name;
    private Integer quantity;
    public MedicineResponseDto(MedicationEntity medicationEntity) {
        this.name = medicationEntity.getName();
        this.quantity = medicationEntity.getQuantity();
    }
}
