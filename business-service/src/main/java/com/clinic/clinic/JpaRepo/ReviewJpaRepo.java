package com.clinic.clinic.JpaRepo;

import com.clinic.clinic.Entity.Review.ReviewEntity;
import com.clinic.clinic.Entity.User.User;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewJpaRepo extends JpaRepository<ReviewEntity, Long> {
    List<ReviewEntity> findReviewEntityByPatientId(@NonNull User patientId);

    List<ReviewEntity> findReviewEntityByDoctorId(@NonNull User doctor);
}
