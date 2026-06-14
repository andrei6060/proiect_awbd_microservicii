package com.clinic.clinic.service;

import com.clinic.clinic.Entity.File.*;
import com.clinic.clinic.Entity.User.Specializations;
import com.clinic.clinic.Entity.User.User;
import com.clinic.clinic.JpaRepo.FileJpaRepo;
import com.clinic.clinic.JpaRepo.UserJpaRepo;
import com.clinic.clinic.Service.FileService;
import com.clinic.clinic.global.PatientNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private FileJpaRepo fileJpaRepo;
    @Mock
    private UserJpaRepo userJpaRepo;

    @InjectMocks
    private FileService fileService;

    private User patient(int id) {
        return User.builder().id(id).email("p@test.com").firstName("Pat").lastName("Ient").build();
    }

    @Test
    void addNewFile_shouldSaveFileForPatient() {
        AddFileDto dto = AddFileDto.builder().patientId(1).bloodType(BloodType.A_POSITIVE).build();
        when(userJpaRepo.findById(1)).thenReturn(Optional.of(patient(1)));

        fileService.addNewFile(dto);

        verify(fileJpaRepo).save(any(FileEntity.class));
    }

    @Test
    void addNewFile_whenPatientNotFound_shouldThrow() {
        AddFileDto dto = AddFileDto.builder().patientId(99).build();
        when(userJpaRepo.findById(99)).thenReturn(Optional.empty());

        assertThrows(PatientNotFoundException.class, () -> fileService.addNewFile(dto));
        verify(fileJpaRepo, never()).save(any());
    }

    @Test
    void addNewFile_whenUserIsDoctor_shouldThrow() {
        AddFileDto dto = AddFileDto.builder().patientId(1).build();
        User doctor = patient(1);
        doctor.setSpecialization(Specializations.CARDIOLOGIE);
        when(userJpaRepo.findById(1)).thenReturn(Optional.of(doctor));

        assertThrows(PatientNotFoundException.class, () -> fileService.addNewFile(dto));
        verify(fileJpaRepo, never()).save(any());
    }

    @Test
    void getPatientFile_shouldReturnFindFileDto() {
        GetFileDto dto = GetFileDto.builder().patientId(1).build();
        User user = patient(1);
        FileEntity file = FileEntity.builder().id(1).user(user)
                .bloodType(BloodType.O_POSITIVE).medications(new ArrayList<>()).build();
        when(userJpaRepo.findById(1)).thenReturn(Optional.of(user));
        when(fileJpaRepo.findByUser(user)).thenReturn(Optional.of(file));

        FindFileDto result = fileService.getPatientFile(dto);

        assertEquals(1, result.getPatientId());
        assertEquals(BloodType.O_POSITIVE, result.getBloodType());
    }

    @Test
    void getPatientFile_whenNoFile_shouldThrow() {
        GetFileDto dto = GetFileDto.builder().patientId(1).build();
        User user = patient(1);
        when(userJpaRepo.findById(1)).thenReturn(Optional.of(user));
        when(fileJpaRepo.findByUser(user)).thenReturn(Optional.empty());

        assertThrows(PatientNotFoundException.class, () -> fileService.getPatientFile(dto));
    }

    @Test
    void getPatientFile_whenPatientNotFound_shouldThrow() {
        GetFileDto dto = GetFileDto.builder().patientId(99).build();
        when(userJpaRepo.findById(99)).thenReturn(Optional.empty());

        assertThrows(PatientNotFoundException.class, () -> fileService.getPatientFile(dto));
    }

    @Test
    void addBloodType_shouldUpdateAndSave() {
        AddBloodDto dto = AddBloodDto.builder().patientId(1).bloodType(BloodType.B_POSITIVE).build();
        User user = patient(1);
        FileEntity file = FileEntity.builder().id(1).user(user)
                .bloodType(BloodType.unknown).medications(new ArrayList<>()).build();
        when(userJpaRepo.findById(1)).thenReturn(Optional.of(user));
        when(fileJpaRepo.findByUser(user)).thenReturn(Optional.of(file));

        fileService.addBloodType(dto);

        assertEquals(BloodType.B_POSITIVE, file.getBloodType());
        verify(fileJpaRepo).save(file);
    }

    @Test
    void addBloodType_whenPatientNotFound_shouldThrow() {
        AddBloodDto dto = AddBloodDto.builder().patientId(99).bloodType(BloodType.B_POSITIVE).build();
        when(userJpaRepo.findById(99)).thenReturn(Optional.empty());

        assertThrows(PatientNotFoundException.class, () -> fileService.addBloodType(dto));
    }

    @Test
    void getAllFiles_shouldMapFiles() {
        User user = patient(1);
        FileEntity file = FileEntity.builder().id(1).user(user)
                .bloodType(BloodType.A_NEGATIVE).medications(new ArrayList<>()).build();
        when(fileJpaRepo.findAll()).thenReturn(List.of(file));

        Object result = fileService.getAllFiles();

        assertNotNull(result);
        assertEquals(1, ((List<?>) result).size());
    }
}
