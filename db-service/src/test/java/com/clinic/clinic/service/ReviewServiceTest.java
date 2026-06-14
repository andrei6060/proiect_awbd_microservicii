package com.clinic.clinic.service;

import com.clinic.clinic.Entity.Review.AddReviewDto;
import com.clinic.clinic.Entity.Review.DeleteReviewDto;
import com.clinic.clinic.Entity.Review.ReviewDto;
import com.clinic.clinic.Entity.Review.ReviewEntity;
import com.clinic.clinic.Entity.User.Specializations;
import com.clinic.clinic.Entity.User.User;
import com.clinic.clinic.JpaRepo.ReviewJpaRepo;
import com.clinic.clinic.JpaRepo.UserJpaRepo;
import com.clinic.clinic.Service.ReviewService;
import com.clinic.clinic.global.UserIsNotDoctor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewJpaRepo reviewJpaRepo;
    @Mock
    private UserJpaRepo userJpaRepo;

    @InjectMocks
    private ReviewService reviewService;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(User user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList()));
    }

    private User user(int id, Specializations spec) {
        return User.builder().id(id).email(id + "@test.com").firstName("F").lastName("L").specialization(spec).build();
    }

    private ReviewEntity review(User patient, User doctor) {
        return ReviewEntity.builder()
                .id(1L).patientId(patient).doctorId(doctor).anonymousReview(false)
                .rating(5).aspect("kindness").comment("great").build();
    }

    @Test
    void addReview_shouldSave() {
        User patient = user(1, null);
        User doctor = user(2, Specializations.CARDIOLOGIE);
        authenticateAs(patient);
        AddReviewDto dto = AddReviewDto.builder()
                .doctorId(2).review("great").anonymousReview(false).rating(5).aspect("kindness").build();
        when(userJpaRepo.findById(2)).thenReturn(Optional.of(doctor));

        reviewService.addReview(dto);

        verify(reviewJpaRepo).save(any(ReviewEntity.class));
    }

    @Test
    void addReview_whenDoctorNotFound_shouldThrow() {
        User patient = user(1, null);
        authenticateAs(patient);
        AddReviewDto dto = AddReviewDto.builder()
                .doctorId(99).review("x").anonymousReview(false).build();
        when(userJpaRepo.findById(99)).thenReturn(Optional.empty());

        assertThrows(UserIsNotDoctor.class, () -> reviewService.addReview(dto));
    }

    @Test
    void addReview_whenTargetIsNotDoctor_shouldThrow() {
        User patient = user(1, null);
        User notDoctor = user(2, null); // no specialization
        authenticateAs(patient);
        AddReviewDto dto = AddReviewDto.builder()
                .doctorId(2).review("x").anonymousReview(false).build();
        when(userJpaRepo.findById(2)).thenReturn(Optional.of(notDoctor));

        assertThrows(UserIsNotDoctor.class, () -> reviewService.addReview(dto));
        verify(reviewJpaRepo, never()).save(any());
    }

    @Test
    void getOwnReviews_shouldMap() {
        User patient = user(1, null);
        User doctor = user(2, Specializations.CARDIOLOGIE);
        authenticateAs(patient);
        when(reviewJpaRepo.findReviewEntityByPatientId(patient)).thenReturn(List.of(review(patient, doctor)));

        List<ReviewDto> result = reviewService.getOwnReviews();

        assertEquals(1, result.size());
    }

    @Test
    void getOwnReviewsDoctor_shouldMap() {
        User patient = user(1, null);
        User doctor = user(2, Specializations.CARDIOLOGIE);
        authenticateAs(doctor);
        when(reviewJpaRepo.findReviewEntityByDoctorId(doctor)).thenReturn(List.of(review(patient, doctor)));

        List<ReviewDto> result = reviewService.getOwnReviewsDoctor();

        assertEquals(1, result.size());
    }

    @Test
    void getAllReviews_shouldMap() {
        User patient = user(1, null);
        User doctor = user(2, Specializations.CARDIOLOGIE);
        when(reviewJpaRepo.findAll()).thenReturn(List.of(review(patient, doctor)));

        List<ReviewDto> result = reviewService.getAllReviews();

        assertEquals(1, result.size());
    }

    @Test
    void deleteReview_shouldDelete() {
        User patient = user(1, null);
        User doctor = user(2, Specializations.CARDIOLOGIE);
        ReviewEntity entity = review(patient, doctor);
        when(reviewJpaRepo.findById(1L)).thenReturn(Optional.of(entity));

        reviewService.deleteReview(DeleteReviewDto.builder().id(1L).build());

        verify(reviewJpaRepo).delete(entity);
    }

    @Test
    void deleteReview_whenNotFound_shouldThrow() {
        when(reviewJpaRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> reviewService.deleteReview(DeleteReviewDto.builder().id(1L).build()));
    }
}
