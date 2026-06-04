//package com.clinic.clinic.JpaRepo;
//
//import com.clinic.clinic.Entity.Medication.MedicationEntity;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//
//import java.util.List;
//import java.util.Optional;
//
//public interface MedicationJpaRepo extends JpaRepository<MedicationEntity, Integer> {
//    Optional<MedicationEntity> findByName(String name);
//
//    @Query("SELECT m FROM MedicationEntity m WHERE m.active = true")
//    List<MedicationEntity> findAllByActiveIsTrue();
//
//    @Query("SELECT m FROM MedicationEntity m WHERE m.quantity > 0")
//    List<MedicationEntity> findAllByQuantityGreaterThan();
//}
