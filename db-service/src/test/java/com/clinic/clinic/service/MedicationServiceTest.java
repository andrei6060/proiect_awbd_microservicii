package com.clinic.clinic.service;

import com.clinic.clinic.Entity.File.BloodType;
import com.clinic.clinic.Entity.File.FileEntity;
import com.clinic.clinic.Entity.File.FileMedication;
import com.clinic.clinic.Entity.Medication.*;
import com.clinic.clinic.Entity.User.User;
import com.clinic.clinic.JpaRepo.FileJpaRepo;
import com.clinic.clinic.JpaRepo.MedicationJpaRepo;
import com.clinic.clinic.JpaRepo.UserJpaRepo;
import com.clinic.clinic.Service.MedicationService;
import com.clinic.clinic.global.*;
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
class MedicationServiceTest {

    @Mock
    private MedicationJpaRepo medicationJpaRepo;
    @Mock
    private UserJpaRepo userJpaRepo;
    @Mock
    private FileJpaRepo fileJpaRepo;

    @InjectMocks
    private MedicationService medicationService;

    private MedicationEntity activeMed(int qty) {
        return MedicationEntity.builder()
                .id(1).name("Paracetamol").description("Pain relief").quantity(qty).active(true).build();
    }

    @Test
    void addNewMedication_shouldSave() {
        AddMedicationDto dto = new AddMedicationDto();
        dto.setName("Paracetamol");
        dto.setDescription("Pain relief");
        dto.setQuantity(100);

        medicationService.addNewMedication(dto);

        verify(medicationJpaRepo).save(any(MedicationEntity.class));
    }

    @Test
    void supplyMedication_shouldIncreaseQuantity() {
        SupplyMedicationDto dto = new SupplyMedicationDto();
        dto.setMedicationName("Paracetamol");
        dto.setQuantity(50);
        MedicationEntity med = activeMed(100);
        when(medicationJpaRepo.findByName("Paracetamol")).thenReturn(Optional.of(med));

        medicationService.supplyMedication(dto);

        assertEquals(150, med.getQuantity());
        verify(medicationJpaRepo).save(med);
    }

    @Test
    void supplyMedication_whenNotFound_shouldThrow() {
        SupplyMedicationDto dto = new SupplyMedicationDto();
        dto.setMedicationName("Ghost");
        dto.setQuantity(50);
        when(medicationJpaRepo.findByName("Ghost")).thenReturn(Optional.empty());

        assertThrows(MedicationNotFoundException.class, () -> medicationService.supplyMedication(dto));
        verify(medicationJpaRepo, never()).save(any());
    }

    @Test
    void discontinueMedication_shouldDeactivateAndZeroQuantity() {
        DiscontinueMedicationDto dto = new DiscontinueMedicationDto();
        dto.setName("Paracetamol");
        MedicationEntity med = activeMed(100);
        when(medicationJpaRepo.findByName("Paracetamol")).thenReturn(Optional.of(med));

        medicationService.discontinueMedication(dto);

        assertFalse(med.isActive());
        assertEquals(0, med.getQuantity());
        verify(medicationJpaRepo).save(med);
    }

    @Test
    void discontinueMedication_whenAlreadyDiscontinued_shouldThrow() {
        DiscontinueMedicationDto dto = new DiscontinueMedicationDto();
        dto.setName("Paracetamol");
        MedicationEntity med = activeMed(0);
        med.setActive(false);
        when(medicationJpaRepo.findByName("Paracetamol")).thenReturn(Optional.of(med));

        assertThrows(MedicationAlreadyDiscontinued.class, () -> medicationService.discontinueMedication(dto));
    }

    @Test
    void discontinueMedication_whenNotFound_shouldThrow() {
        DiscontinueMedicationDto dto = new DiscontinueMedicationDto();
        dto.setName("Ghost");
        when(medicationJpaRepo.findByName("Ghost")).thenReturn(Optional.empty());

        assertThrows(MedicationNotFoundException.class, () -> medicationService.discontinueMedication(dto));
    }

    @Test
    void reactivateMedication_shouldActivate() {
        DiscontinueMedicationDto dto = new DiscontinueMedicationDto();
        dto.setName("Paracetamol");
        MedicationEntity med = activeMed(0);
        med.setActive(false);
        when(medicationJpaRepo.findByName("Paracetamol")).thenReturn(Optional.of(med));

        medicationService.reactivateMedication(dto);

        assertTrue(med.isActive());
        verify(medicationJpaRepo).save(med);
    }

    @Test
    void reactivateMedication_whenNotDiscontinued_shouldThrow() {
        DiscontinueMedicationDto dto = new DiscontinueMedicationDto();
        dto.setName("Paracetamol");
        MedicationEntity med = activeMed(10);
        when(medicationJpaRepo.findByName("Paracetamol")).thenReturn(Optional.of(med));

        assertThrows(MedicationIsNotDiscontinued.class, () -> medicationService.reactivateMedication(dto));
    }

    @Test
    void giveMedication_whenFileEmpty_shouldAddMedicationAndDecreaseStock() {
        GiveMedicationDto dto = new GiveMedicationDto();
        dto.setIdPatient(1);
        dto.setMedicationName("Paracetamol");
        dto.setQuantity(2);

        User patient = User.builder().id(1).email("p@test.com").build();
        MedicationEntity med = activeMed(10);
        FileEntity file = FileEntity.builder().id(1).user(patient)
                .bloodType(BloodType.A_POSITIVE).medications(new ArrayList<>()).build();

        when(userJpaRepo.findById(1)).thenReturn(Optional.of(patient));
        when(medicationJpaRepo.findByName("Paracetamol")).thenReturn(Optional.of(med));
        when(fileJpaRepo.findByUser(patient)).thenReturn(Optional.of(file));

        medicationService.giveMedication(dto);

        assertEquals(8, med.getQuantity());
        assertEquals(1, file.getMedications().size());
        verify(fileJpaRepo).save(file);
    }

    @Test
    void giveMedication_whenMedicationAlreadyInFile_shouldIncrementQuantity() {
        GiveMedicationDto dto = new GiveMedicationDto();
        dto.setIdPatient(1);
        dto.setMedicationName("Paracetamol");
        dto.setQuantity(3);

        User patient = User.builder().id(1).email("p@test.com").build();
        MedicationEntity med = activeMed(10);
        FileEntity file = FileEntity.builder().id(1).user(patient)
                .bloodType(BloodType.A_POSITIVE).medications(new ArrayList<>()).build();
        FileMedication existing = new FileMedication(file, med, 5);
        file.getMedications().add(existing);

        when(userJpaRepo.findById(1)).thenReturn(Optional.of(patient));
        when(medicationJpaRepo.findByName("Paracetamol")).thenReturn(Optional.of(med));
        when(fileJpaRepo.findByUser(patient)).thenReturn(Optional.of(file));

        medicationService.giveMedication(dto);

        assertEquals(7, med.getQuantity());
        assertEquals(8, existing.getQuantity());
        verify(fileJpaRepo).save(file);
    }

    @Test
    void giveMedication_whenPatientNotFound_shouldThrow() {
        GiveMedicationDto dto = new GiveMedicationDto();
        dto.setIdPatient(99);
        dto.setMedicationName("Paracetamol");
        dto.setQuantity(2);
        when(userJpaRepo.findById(99)).thenReturn(Optional.empty());

        assertThrows(PatientNotFoundException.class, () -> medicationService.giveMedication(dto));
    }

    @Test
    void giveMedication_whenMedicationDiscontinued_shouldThrow() {
        GiveMedicationDto dto = new GiveMedicationDto();
        dto.setIdPatient(1);
        dto.setMedicationName("Paracetamol");
        dto.setQuantity(2);
        User patient = User.builder().id(1).email("p@test.com").build();
        MedicationEntity med = activeMed(10);
        med.setActive(false);
        when(userJpaRepo.findById(1)).thenReturn(Optional.of(patient));
        when(medicationJpaRepo.findByName("Paracetamol")).thenReturn(Optional.of(med));

        assertThrows(MedicationAlreadyDiscontinued.class, () -> medicationService.giveMedication(dto));
    }

    @Test
    void giveMedication_whenNotEnoughStock_shouldThrow() {
        GiveMedicationDto dto = new GiveMedicationDto();
        dto.setIdPatient(1);
        dto.setMedicationName("Paracetamol");
        dto.setQuantity(50);
        User patient = User.builder().id(1).email("p@test.com").build();
        MedicationEntity med = activeMed(10);
        when(userJpaRepo.findById(1)).thenReturn(Optional.of(patient));
        when(medicationJpaRepo.findByName("Paracetamol")).thenReturn(Optional.of(med));

        assertThrows(NotEnoughtMedicationException.class, () -> medicationService.giveMedication(dto));
    }

    @Test
    void giveMedication_whenNoFile_shouldThrow() {
        GiveMedicationDto dto = new GiveMedicationDto();
        dto.setIdPatient(1);
        dto.setMedicationName("Paracetamol");
        dto.setQuantity(2);
        User patient = User.builder().id(1).email("p@test.com").build();
        MedicationEntity med = activeMed(10);
        when(userJpaRepo.findById(1)).thenReturn(Optional.of(patient));
        when(medicationJpaRepo.findByName("Paracetamol")).thenReturn(Optional.of(med));
        when(fileJpaRepo.findByUser(patient)).thenReturn(Optional.empty());

        assertThrows(PatientFileNotFoundException.class, () -> medicationService.giveMedication(dto));
    }

    @Test
    void getMedicine_shouldReturnDto() {
        GetMedicineDto dto = new GetMedicineDto();
        dto.setName("Paracetamol");
        when(medicationJpaRepo.findByName("Paracetamol")).thenReturn(Optional.of(activeMed(10)));

        MedicineResponseDto result = medicationService.getMedicine(dto);

        assertNotNull(result);
        assertEquals("Paracetamol", result.getName());
    }

    @Test
    void getMedicine_whenNotFound_shouldThrow() {
        GetMedicineDto dto = new GetMedicineDto();
        dto.setName("Ghost");
        when(medicationJpaRepo.findByName("Ghost")).thenReturn(Optional.empty());

        assertThrows(MedicationNotFoundException.class, () -> medicationService.getMedicine(dto));
    }

    @Test
    void getAllActiveMedicine_shouldMapEntities() {
        when(medicationJpaRepo.findAllByActiveIsTrue()).thenReturn(List.of(activeMed(10)));

        List<MedicineResponseDto> result = medicationService.getAllActiveMedicine();

        assertEquals(1, result.size());
        verify(medicationJpaRepo).findAllByActiveIsTrue();
    }

    @Test
    void getAllAvailableMedicine_shouldMapEntities() {
        when(medicationJpaRepo.findAllByQuantityGreaterThan()).thenReturn(List.of(activeMed(10)));

        List<MedicineResponseDto> result = medicationService.getAllAvailableMedicine();

        assertEquals(1, result.size());
        verify(medicationJpaRepo).findAllByQuantityGreaterThan();
    }
}
