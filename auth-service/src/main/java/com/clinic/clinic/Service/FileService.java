//package com.clinic.clinic.Service;
//
//import com.clinic.clinic.Entity.File.*;
//import com.clinic.clinic.JpaRepo.FileJpaRepo;
//import com.clinic.clinic.global.PatientNotFoundException;
//import com.clinic.clinic.Entity.User.User;
//import com.clinic.clinic.JpaRepo.UserJpaRepo;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class FileService {
//    private final FileJpaRepo fileJpaRepo;
//    private final UserJpaRepo userJpaRepo;
//
//    public void addNewFile(AddFileDto addFileDto) {
//        User user = userJpaRepo.findById(addFileDto.getPatientId()).orElseThrow(
//                () -> new PatientNotFoundException(addFileDto.getPatientId())
//        );
//        if(user.getSpecialization() != null){
//            throw new PatientNotFoundException(addFileDto.getPatientId());
//        }
//        var file = FileEntity.builder()
//                .user(user)
//                .bloodType(addFileDto.getBloodType()!=null?addFileDto.getBloodType(): BloodType.unknown)
//                .build();
//        fileJpaRepo.save(file);
//    }
//
//    public FileEntity getOwnFile(){
//        String email = SecurityContextHolder.getContext().getAuthentication().getName();
//        User user = userJpaRepo.findByEmail(email).get();
//        return fileJpaRepo.findByUser(user).get();
//    }
//
//    public FindFileDto getPatientFile(@Valid GetFileDto getFileDto) {
//        User user = userJpaRepo.findById(getFileDto.getPatientId()).orElseThrow(
//                () -> new PatientNotFoundException(getFileDto.getPatientId())
//        );
//        if(user.getSpecialization() != null){
//            throw new PatientNotFoundException(getFileDto.getPatientId());
//        }
//        FileEntity file = fileJpaRepo.findByUser(user).orElseThrow(
//                () -> new PatientNotFoundException(getFileDto.getPatientId())
//        );
//        return FindFileDto.builder()
//                .patientId(getFileDto.getPatientId())
//                .bloodType(file.getBloodType())
//                .medications(file.getMedications().stream().collect(
//                        java.util.stream.Collectors.toMap(
//                                fileMedication -> fileMedication.getMedication().getName(),
//                                fileMedication -> fileMedication.getQuantity()
//                        )
//                ))
//                .build();
//
//    }
//
//    public Object getAllFiles() {
//        return fileJpaRepo.findAll().stream().map(fileEntity -> FindFileDto.builder()
//                .patientId(fileEntity.getUser().getId())
//                .bloodType(fileEntity.getBloodType())
//                .medications(fileEntity.getMedications().stream().collect(
//                        java.util.stream.Collectors.toMap(
//                                fileMedication -> fileMedication.getMedication().getName(),
//                                fileMedication -> fileMedication.getQuantity()
//                        )
//                ))
//                .build()).toList();
//    }
//
//    public void addBloodType(@Valid AddBloodDto addBloodDto) {
//        User user = userJpaRepo.findById(addBloodDto.getPatientId()).orElseThrow(
//                () -> new PatientNotFoundException(addBloodDto.getPatientId())
//        );
//        if(user.getSpecialization() != null){
//            throw new PatientNotFoundException(addBloodDto.getPatientId());
//        }
//        FileEntity file = fileJpaRepo.findByUser(user).orElseThrow(
//                () -> new PatientNotFoundException(addBloodDto.getPatientId())
//        );
//        file.setBloodType(addBloodDto.getBloodType());
//        fileJpaRepo.save(file);
//    }
//}
