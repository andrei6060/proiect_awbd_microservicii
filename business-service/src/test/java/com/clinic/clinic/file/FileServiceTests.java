package com.clinic.clinic.file;



import com.clinic.clinic.Entity.File.*;
import com.clinic.clinic.Entity.Medication.MedicationEntity;
import com.clinic.clinic.Entity.User.Specializations;
import com.clinic.clinic.Entity.User.User;
import com.clinic.clinic.JpaRepo.FileJpaRepo;
import com.clinic.clinic.JpaRepo.UserJpaRepo;
import com.clinic.clinic.Service.FileService;
import com.clinic.clinic.global.PatientNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTests {

    @Mock
    private FileJpaRepo fileJpaRepo;

    @Mock
    private UserJpaRepo userJpaRepo;

    @InjectMocks
    private FileService fileService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void addNewFile_shouldSaveFile_whenPatientExists() {
        User patient = User.builder()
                .id(1)
                .email("patient@test.com")
                .specialization(null)
                .build();

        AddFileDto dto = AddFileDto.builder()
                .patientId(1)
                .bloodType(BloodType.A_POSITIVE)
                .build();

        when(userJpaRepo.findById(1)).thenReturn(Optional.of(patient));

        fileService.addNewFile(dto);

        ArgumentCaptor<FileEntity> captor = ArgumentCaptor.forClass(FileEntity.class);
        verify(fileJpaRepo).save(captor.capture());

        FileEntity savedFile = captor.getValue();

        assertEquals(patient, savedFile.getUser());
        assertEquals(BloodType.A_POSITIVE, savedFile.getBloodType());
    }

    @Test
    void addNewFile_shouldUseUnknownBloodType_whenBloodTypeIsNull() {
        User patient = User.builder()
                .id(1)
                .specialization(null)
                .build();

        AddFileDto dto = AddFileDto.builder()
                .patientId(1)
                .bloodType(null)
                .build();

        when(userJpaRepo.findById(1)).thenReturn(Optional.of(patient));

        fileService.addNewFile(dto);

        ArgumentCaptor<FileEntity> captor = ArgumentCaptor.forClass(FileEntity.class);
        verify(fileJpaRepo).save(captor.capture());

        assertEquals(BloodType.unknown, captor.getValue().getBloodType());
    }

    @Test
    void addNewFile_shouldThrowException_whenPatientDoesNotExist() {
        AddFileDto dto = AddFileDto.builder()
                .patientId(99)
                .bloodType(BloodType.A_POSITIVE)
                .build();

        when(userJpaRepo.findById(99)).thenReturn(Optional.empty());

        assertThrows(PatientNotFoundException.class, () -> fileService.addNewFile(dto));

        verify(fileJpaRepo, never()).save(any());
    }

    @Test
    void addNewFile_shouldThrowException_whenUserIsDoctor() {
        User doctor = User.builder()
                .id(1)
                .specialization(Enum.valueOf(
                        Specializations.class,
                        Specializations.values()[0].name()
                ))
                .build();

        AddFileDto dto = AddFileDto.builder()
                .patientId(1)
                .bloodType(BloodType.A_POSITIVE)
                .build();

        when(userJpaRepo.findById(1)).thenReturn(Optional.of(doctor));

        assertThrows(PatientNotFoundException.class, () -> fileService.addNewFile(dto));

        verify(fileJpaRepo, never()).save(any());
    }

    @Test
    void getOwnFile_shouldReturnAuthenticatedUserFile() {
        User user = User.builder()
                .id(1)
                .email("patient@test.com")
                .build();

        FileEntity file = FileEntity.builder()
                .id(10)
                .user(user)
                .bloodType(BloodType.A_POSITIVE)
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("patient@test.com", null)
        );

        when(userJpaRepo.findByEmail("patient@test.com")).thenReturn(Optional.of(user));
        when(fileJpaRepo.findByUser(user)).thenReturn(Optional.of(file));

        FileEntity result = fileService.getOwnFile();

        assertEquals(file, result);
    }

//    @Test
//    void getPatientFile_shouldReturnFindFileDto() {
//        User patient = User.builder()
//                .id(1)
//                .specialization(null)
//                .build();
//
//        MedicationEntity medication = MedicationEntity.builder()
//                .id(1)
//                .name("Paracetamol")
//                .build();
//
//        FileEntity file = FileEntity.builder()
//                .id(1)
//                .user(patient)
//                .bloodType(BloodType.A_POSITIVE)
//                .build();
//
//        file.addMedication(medication, 3);
//
//        GetFileDto dto = GetFileDto.builder()
//                .patientId(1)
//                .build();
//
//        when(userJpaRepo.findById(1)).thenReturn(Optional.of(patient));
//        when(fileJpaRepo.findByUser(patient)).thenReturn(Optional.of(file));
//
//        FindFileDto result = fileService.getPatientFile(dto);
//
//        assertEquals(1, result.getPatientId());
//        assertEquals(BloodType.A_POSITIVE, result.getBloodType());
//        assertEquals(3, result.getMedications().get("Paracetamol"));
//    }

    @Test
    void getPatientFile_shouldThrowException_whenFileDoesNotExist() {
        User patient = User.builder()
                .id(1)
                .specialization(null)
                .build();

        GetFileDto dto = GetFileDto.builder()
                .patientId(1)
                .build();

        when(userJpaRepo.findById(1)).thenReturn(Optional.of(patient));
        when(fileJpaRepo.findByUser(patient)).thenReturn(Optional.empty());

        assertThrows(PatientNotFoundException.class, () -> fileService.getPatientFile(dto));
    }

//    @Test
//    void getAllFiles_shouldReturnAllFilesAsDtos() {
//        User patient = User.builder()
//                .id(1)
//                .build();
//
//        FileEntity file = FileEntity.builder()
//                .id(1)
//                .user(patient)
//                .bloodType(BloodType.A_POSITIVE)
//                .build();
//
//        when(fileJpaRepo.findAll()).thenReturn(List.of(file));
//
//        Object result = fileService.getAllFiles();
//
//        assertNotNull(result);
//        assertTrue(result instanceof List<?>);
//        assertEquals(1, ((List<?>) result).size());
//    }

    @Test
    void addBloodType_shouldUpdateBloodType() {
        User patient = User.builder()
                .id(1)
                .specialization(null)
                .build();

        FileEntity file = FileEntity.builder()
                .id(1)
                .user(patient)
                .bloodType(BloodType.unknown)
                .build();

        AddBloodDto dto = AddBloodDto.builder()
                .patientId(1)
                .bloodType(BloodType.A_POSITIVE)
                .build();

        when(userJpaRepo.findById(1)).thenReturn(Optional.of(patient));
        when(fileJpaRepo.findByUser(patient)).thenReturn(Optional.of(file));

        fileService.addBloodType(dto);

        assertEquals(BloodType.A_POSITIVE, file.getBloodType());
        verify(fileJpaRepo).save(file);
    }
}