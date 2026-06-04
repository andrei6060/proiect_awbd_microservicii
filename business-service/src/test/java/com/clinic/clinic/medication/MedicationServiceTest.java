package com.clinic.clinic.medication;


import com.clinic.clinic.Entity.File.BloodType;
import com.clinic.clinic.Entity.File.FileEntity;
import com.clinic.clinic.Entity.Medication.*;
import com.clinic.clinic.Entity.User.User;
import com.clinic.clinic.JpaRepo.FileJpaRepo;
import com.clinic.clinic.JpaRepo.MedicationJpaRepo;
import com.clinic.clinic.JpaRepo.UserJpaRepo;
import com.clinic.clinic.Service.MedicationService;
import com.clinic.clinic.global.PatientNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicationServiceTest {

    @Mock
    private MedicationJpaRepo medicationJpaRepo;

    @Mock
    private FileJpaRepo fileJpaRepo;

    @Mock
    private UserJpaRepo userJpaRepo;

    @InjectMocks
    private MedicationService medicationService;

    @Test
    void addNewMedication_shouldSaveMedication() {
        AddMedicationDto dto = new AddMedicationDto();
        dto.setName("Paracetamol");
        dto.setDescription("Pain relief");
        dto.setQuantity(100);

        medicationService.addNewMedication(dto);

        verify(medicationJpaRepo).save(any(MedicationEntity.class));
    }

    @Test
    void supplyMedication_shouldIncreaseQuantityAndSave() {
        SupplyMedicationDto dto = new SupplyMedicationDto();
        dto.setMedicationName("Paracetamol");
        dto.setQuantity(50);

        MedicationEntity medication = MedicationEntity.builder()
                .id(1)
                .name("Paracetamol")
                .description("Pain relief")
                .quantity(100)
                .active(true)
                .build();

        when(medicationJpaRepo.findByName("Paracetamol")).thenReturn(Optional.of(medication));

        medicationService.supplyMedication(dto);

        assertEquals(150, medication.getQuantity());
        verify(medicationJpaRepo).save(medication);
    }

    @Test
    void discontinueMedication_shouldSetActiveFalseAndSave() {
        DiscontinueMedicationDto dto = new DiscontinueMedicationDto();
        dto.setName("Paracetamol");

        MedicationEntity medication = MedicationEntity.builder()
                .id(1)
                .name("Paracetamol")
                .description("Pain relief")
                .quantity(100)
                .active(true)
                .build();

        when(medicationJpaRepo.findByName("Paracetamol")).thenReturn(Optional.of(medication));

        medicationService.discontinueMedication(dto);

        assertFalse(medication.isActive());
        verify(medicationJpaRepo).save(medication);
    }

    @Test
    void reactivateMedication_shouldSetActiveTrueAndSave() {
        DiscontinueMedicationDto dto = new DiscontinueMedicationDto();
        dto.setName("Paracetamol");

        MedicationEntity medication = MedicationEntity.builder()
                .id(1)
                .name("Paracetamol")
                .description("Pain relief")
                .quantity(100)
                .active(false)
                .build();

        when(medicationJpaRepo.findByName("Paracetamol")).thenReturn(Optional.of(medication));

        medicationService.reactivateMedication(dto);

        assertTrue(medication.isActive());
        verify(medicationJpaRepo).save(medication);
    }

    @Test
    void giveMedication_shouldAddMedicationToPatientFileAndDecreaseStock() {
        GiveMedicationDto dto = new GiveMedicationDto();
        dto.setIdPatient(1);
        dto.setMedicationName("Paracetamol");
        dto.setQuantity(2);

        User patient = User.builder()
                .id(1)
                .email("patient@test.com")
                .build();

        MedicationEntity medication = MedicationEntity.builder()
                .id(1)
                .name("Paracetamol")
                .description("Pain relief")
                .quantity(10)
                .active(true)
                .build();

        FileEntity file = FileEntity.builder()
                .id(1)
                .user(patient)
                .bloodType(BloodType.A_POSITIVE)
                .medications(new java.util.ArrayList<>())
                .build();

        when(userJpaRepo.findById(1)).thenReturn(Optional.of(patient));
        when(medicationJpaRepo.findByName("Paracetamol")).thenReturn(Optional.of(medication));
        when(fileJpaRepo.findByUser(patient)).thenReturn(Optional.of(file));

        medicationService.giveMedication(dto);

        assertEquals(8, medication.getQuantity());
        assertEquals(1, file.getMedications().size());
        verify(medicationJpaRepo).save(medication);
        verify(fileJpaRepo).save(file);
    }

    @Test
    void giveMedication_whenPatientDoesNotExist_shouldThrowException() {
        GiveMedicationDto dto = new GiveMedicationDto();
        dto.setIdPatient(99);
        dto.setMedicationName("Paracetamol");
        dto.setQuantity(2);

        when(userJpaRepo.findById(99)).thenReturn(Optional.empty());

        assertThrows(PatientNotFoundException.class, () -> medicationService.giveMedication(dto));
        verify(fileJpaRepo, never()).save(any());
        verify(medicationJpaRepo, never()).save(any());
    }

    @Test
    void getMedicine_shouldReturnMedication() {
        GetMedicineDto dto = new GetMedicineDto();
        dto.setName("Paracetamol");

        MedicationEntity medication = MedicationEntity.builder()
                .id(1)
                .name("Paracetamol")
                .description("Pain relief")
                .quantity(100)
                .active(true)
                .build();

        when(medicationJpaRepo.findByName("Paracetamol")).thenReturn(Optional.of(medication));

        Object result = medicationService.getMedicine(dto);

        assertNotNull(result);
        verify(medicationJpaRepo).findByName("Paracetamol");
    }

    @Test
    void getAllActiveMedicine_shouldReturnOnlyActiveMedication() {
        MedicationEntity active = MedicationEntity.builder()
                .id(1)
                .name("Paracetamol")
                .description("Pain relief")
                .quantity(10)
                .active(true)
                .build();

        when(medicationJpaRepo.findAllByActiveIsTrue()).thenReturn(List.of(active));

        List<MedicineResponseDto> result = medicationService.getAllActiveMedicine();

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(medicationJpaRepo).findAllByActiveIsTrue();
    }
}
