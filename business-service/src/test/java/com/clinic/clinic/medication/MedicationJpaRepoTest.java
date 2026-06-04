package com.clinic.clinic.medication;

import com.clinic.clinic.Entity.Medication.MedicationEntity;
import com.clinic.clinic.JpaRepo.MedicationJpaRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class MedicationJpaRepoTest {

    @Autowired
    private MedicationJpaRepo medicationJpaRepo;

    @Test
    void save_shouldPersistMedication() {
        MedicationEntity medication = MedicationEntity.builder()
                .name("Paracetamol")
                .description("Pain relief")
                .quantity(10)
                .active(true)
                .build();

        MedicationEntity savedMedication = medicationJpaRepo.save(medication);

        assertNotNull(savedMedication.getId());
        assertEquals("Paracetamol", savedMedication.getName());
        assertEquals("Pain relief", savedMedication.getDescription());
        assertEquals(10, savedMedication.getQuantity());
        assertTrue(savedMedication.isActive());
    }

    @Test
    void findById_shouldReturnMedicationWhenExists() {
        MedicationEntity medication = MedicationEntity.builder()
                .name("Ibuprofen")
                .description("Anti inflammatory")
                .quantity(20)
                .active(true)
                .build();

        MedicationEntity savedMedication = medicationJpaRepo.save(medication);

        Optional<MedicationEntity> result = medicationJpaRepo.findById(savedMedication.getId());

        assertTrue(result.isPresent());
        assertEquals("Ibuprofen", result.get().getName());
    }

    @Test
    void findAll_shouldReturnAllMedications() {
        MedicationEntity medication1 = MedicationEntity.builder()
                .name("Paracetamol")
                .description("Pain relief")
                .quantity(10)
                .active(true)
                .build();

        MedicationEntity medication2 = MedicationEntity.builder()
                .name("Aspirin")
                .description("Blood thinner")
                .quantity(15)
                .active(false)
                .build();

        medicationJpaRepo.save(medication1);
        medicationJpaRepo.save(medication2);

        assertEquals(2, medicationJpaRepo.findAll().size());
    }

    @Test
    void delete_shouldRemoveMedication() {
        MedicationEntity medication = MedicationEntity.builder()
                .name("Nurofen")
                .description("Pain relief")
                .quantity(5)
                .active(true)
                .build();

        MedicationEntity savedMedication = medicationJpaRepo.save(medication);

        medicationJpaRepo.delete(savedMedication);

        Optional<MedicationEntity> result = medicationJpaRepo.findById(savedMedication.getId());
        assertTrue(result.isEmpty());
    }
}
