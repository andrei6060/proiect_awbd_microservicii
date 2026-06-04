//package com.clinic.clinic.Service;
//
//import com.clinic.clinic.Entity.File.FileEntity;
//import com.clinic.clinic.Entity.Medication.*;
//import com.clinic.clinic.JpaRepo.FileJpaRepo;
//import com.clinic.clinic.Entity.File.FileMedication;
//import com.clinic.clinic.JpaRepo.MedicationJpaRepo;
//import com.clinic.clinic.global.*;
//import com.clinic.clinic.Entity.User.User;
//import com.clinic.clinic.JpaRepo.UserJpaRepo;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Optional;
//
//@Service
//@RequiredArgsConstructor
//public class MedicationService {
//    private final MedicationJpaRepo medicationJpaRepo;
//    private final UserJpaRepo userJpaRepo;
//    private final FileJpaRepo fileJpaRepo;
//
//
//    public void addNewMedication(@Valid AddMedicationDto addMedicationDto) {
//        var medicationEntity = MedicationEntity.builder()
//                .name(addMedicationDto.getName())
//                .description(addMedicationDto.getDescription())
//                .quantity(addMedicationDto.getQuantity())
//                .active(true)
//                .build();
//        medicationJpaRepo.save(medicationEntity);
//    }
//
//    public void supplyMedication(SupplyMedicationDto dto) {
//        MedicationEntity medication = medicationJpaRepo.findByName(dto.getMedicationName())
//                .orElseThrow(() -> new MedicationNotFoundException(dto.getMedicationName()));
//
//        medication.setQuantity(medication.getQuantity() + dto.getQuantity());
//        medicationJpaRepo.save(medication);
//    }
//
////    public void supplyMedication(SupplyMedicationDto supplyMedicationDto) {
////        Optional<MedicationEntity> medicationEntity = medicationJpaRepo.findByName(supplyMedicationDto.getMedicationName());
////        if(medicationEntity.isPresent()) {
////            medicationEntity.get().setQuantity(supplyMedicationDto.getQuantity() + medicationEntity.get().getQuantity());
////            medicationJpaRepo.save(medicationEntity.get());
////        }else{
////            throw new RuntimeException("Medication not found");
////        }
////    }
//
//
//    public void discontinueMedication(DiscontinueMedicationDto discontinueMedicationDto) {
//        MedicationEntity medication = medicationJpaRepo.findByName(discontinueMedicationDto.getName())
//                .orElseThrow(() -> new MedicationNotFoundException(discontinueMedicationDto.getName()));
//        if(medication.isActive()){
//        medication.setActive(false);
//        medication.setQuantity(0);
//        medicationJpaRepo.save(medication);}
//        else{
//            throw new MedicationAlreadyDiscontinued();
//        }
//    }
//
////    public void discontinueMedication(DiscontinueMedicationDto discontinueMedicationDto) {
////        Optional<MedicationEntity> medicationEntity = medicationJpaRepo.findByName(discontinueMedicationDto.getName());
////                if(medicationEntity.isPresent()) {
////                    medicationEntity.get().setActive(false);
////                    medicationJpaRepo.save(medicationEntity.get());
////                }else {
////                    throw new RuntimeException("Medication not found");
////                }
////    }
//
//    public void reactivateMedication(DiscontinueMedicationDto discontinueMedicationDto) {
//        MedicationEntity medication = medicationJpaRepo.findByName(discontinueMedicationDto.getName())
//                .orElseThrow(() -> new MedicationNotFoundException(discontinueMedicationDto.getName()));
//        if (!medication.isActive()) {
//            medication.setActive(true);
//            medicationJpaRepo.save(medication);
//        } else {
//            throw new MedicationIsNotDiscontinued();
//        }
//    }
//
////    public void reactivateMedication(DiscontinueMedicationDto discontinueMedicationDto) {
////        Optional<MedicationEntity> medicationEntity = medicationJpaRepo.findByName(discontinueMedicationDto.getName());
////        if(medicationEntity.isPresent()) {
////            medicationEntity.get().setActive(true);
////            medicationJpaRepo.save(medicationEntity.get());
////        }else {
////            throw new RuntimeException("Medication not found");
////        }
////    }
//
//    public void giveMedication(GiveMedicationDto giveMedicationDto) {
//        User user = userJpaRepo.findById(giveMedicationDto.getIdPatient()).orElseThrow(
//                () -> new PatientNotFoundException(giveMedicationDto.getIdPatient())
//        );
//        MedicationEntity medicationEntity = medicationJpaRepo.findByName(giveMedicationDto.getMedicationName()).orElseThrow(
//                () -> new MedicationNotFoundException(giveMedicationDto.getMedicationName())
//        );
//        if(!medicationEntity.isActive()){
//            throw new MedicationAlreadyDiscontinued();
//        }
//            if(giveMedicationDto.getQuantity() <= medicationEntity.getQuantity()) {
//                medicationEntity.setQuantity(medicationEntity.getQuantity() - giveMedicationDto.getQuantity());
//                medicationJpaRepo.save(medicationEntity);
//                Optional<FileEntity> file = fileJpaRepo.findByUser(user);
//                if(file.isPresent()) {
//                        Integer contor = 0;
//                        for (FileMedication fileMedication : file.get().getMedications()) {
//                            if (fileMedication.getMedication().getName().equals(giveMedicationDto.getMedicationName())) {
//                                fileMedication.setQuantity(fileMedication.getQuantity() + giveMedicationDto.getQuantity());
//                                fileJpaRepo.save(file.get());
//                                contor++;
//                                break;
//                            }
//                        }
//                        if (contor == 0) {
//                            file.get().addMedication(medicationJpaRepo.findByName(giveMedicationDto.getMedicationName()).get(),
//                                    giveMedicationDto.getQuantity());
//                            fileJpaRepo.save(file.get());
//                        }
//                }else{
//                   throw new PatientFileNotFoundException(user.getId());
//                }
//            }else{
//                throw new NotEnoughtMedicationException(giveMedicationDto.getMedicationName());
//            }
//    }
//
//    public List<MedicineResponseDto> getAllActiveMedicine() {
//        List<MedicationEntity> medicationEntities = medicationJpaRepo.findAllByActiveIsTrue();
//        List<MedicineResponseDto> medicineResponseDtos = new java.util.ArrayList<>(List.of());
//        for (MedicationEntity medicationEntity : medicationEntities) {
//            medicineResponseDtos.add(new MedicineResponseDto(medicationEntity));
//        }
//
//        return medicineResponseDtos;
//    }
//
//    public List<MedicineResponseDto> getAllAvailableMedicine() {
//        List<MedicationEntity> medicationEntities = medicationJpaRepo.findAllByQuantityGreaterThan();
//        List<MedicineResponseDto> medicineResponseDtos = new java.util.ArrayList<>(List.of());
//        for (MedicationEntity medicationEntity : medicationEntities) {
//            medicineResponseDtos.add(new MedicineResponseDto(medicationEntity));
//        }
//
//        return medicineResponseDtos;
//    }
//
//    public MedicineResponseDto getMedicine(@Valid GetMedicineDto getMedicineDto) {
//        MedicationEntity medication = medicationJpaRepo.findByName(getMedicineDto.getName()).orElseThrow(
//                () -> new MedicationNotFoundException(getMedicineDto.getName())
//        );
//        MedicineResponseDto medicineResponseDto = new MedicineResponseDto(medication);
//        return medicineResponseDto;
//    }
//}
