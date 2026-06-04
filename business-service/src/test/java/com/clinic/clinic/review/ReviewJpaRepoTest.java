package com.clinic.clinic.review;

import com.clinic.clinic.Entity.Review.ReviewEntity;
import com.clinic.clinic.Entity.User.User;
import com.clinic.clinic.JpaRepo.ReviewJpaRepo;
import com.clinic.clinic.JpaRepo.UserJpaRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
class ReviewJpaRepoTest {

    @Autowired
    private ReviewJpaRepo reviewJpaRepo;

    @Autowired
    private UserJpaRepo userJpaRepo;

    @Test
    void findReviewEntityByPatientId_shouldReturnPatientReviews() {
        User patient = User.builder()
                .firstName("Andrei")
                .lastName("Patient")
                .email("patient@test.com")
                .password("password")
                .enabled(true)
                .accountLocked(false)
                .build();

        User doctor = User.builder()
                .firstName("Doctor")
                .lastName("Test")
                .email("doctor@test.com")
                .password("password")
                .enabled(true)
                .accountLocked(false)
                .build();

        userJpaRepo.save(patient);
        userJpaRepo.save(doctor);

        ReviewEntity review = ReviewEntity.builder()
                .patientId(patient)
                .doctorId(doctor)
                .comment("Good doctor")
                .build();

        reviewJpaRepo.save(review);

        var result = reviewJpaRepo.findReviewEntityByPatientId(patient);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Good doctor", result.get(0).getComment());
    }
}
