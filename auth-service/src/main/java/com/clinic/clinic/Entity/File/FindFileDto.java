//package com.clinic.clinic.Entity.File;
//
//import jakarta.validation.constraints.NotNull;
//import lombok.*;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@Builder
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//public class FindFileDto {
//    @NotNull(message = "Patient cannot be null")
//    Integer patientId;
//    private Boolean isSick;
//    private BloodType bloodType;
//    private Map<String, Integer> medications = new HashMap<>();
//
//    public FindFileDto(FileEntity fileEntity) {
//        this.bloodType = fileEntity.getBloodType();
//        this.patientId = fileEntity.getUser().getId();
//        this.medications = new HashMap<>();
//        for (FileMedication fileMedication : fileEntity.getMedications()) {
//            this.medications.put(fileMedication.getMedication().getName(), fileMedication.getQuantity());
//        }
//    }
//}
