package com.clinic.clinic.review;


import com.clinic.clinic.Entity.Review.AddReviewDto;
import com.clinic.clinic.Entity.Review.DeleteReviewDto;
import com.clinic.clinic.Entity.Review.ReviewEntity;
import com.clinic.clinic.Entity.User.Specializations;
import com.clinic.clinic.Entity.User.User;
import com.clinic.clinic.JpaRepo.ReviewJpaRepo;
import com.clinic.clinic.JpaRepo.UserJpaRepo;
import com.clinic.clinic.Service.ReviewService;
import com.clinic.clinic.resilience.DbServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReviewServiceTest {

    @Mock
    private ReviewJpaRepo reviewJpaRepo;

    @Mock
    private RestTemplate userJpaRepo;

    @Mock
    private DbServiceClient dbServiceClient;

    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reviewService = new ReviewService(reviewJpaRepo, userJpaRepo, dbServiceClient);
        SecurityContextHolder.clearContext();
    }

//    @Test
//    void addReview_shouldSaveReview_whenDoctorExists() {
//        User patient = User.builder()
//                .id(1)
//                .email("patient@test.com")
//                .build();
//
//        User doctor = User.builder()
//                .id(2)
//                .email("doctor@test.com")
//                .specialization(Specializations.CARDIOLOGIE)
//                .build();
//
//        SecurityContextHolder.getContext().setAuthentication(
//                new UsernamePasswordAuthenticationToken(patient, null)
//        );
//
//        AddReviewDto dto = AddReviewDto.builder()
//                .doctorId(2)
//                .review("Very good doctor")
//                .anonymousReview(false)
//                .build();
//
//        when(userJpaRepo.findById(2)).thenReturn(Optional.of(doctor));
//
//        reviewService.addReview(dto);
//
//        verify(reviewJpaRepo).save(any(ReviewEntity.class));
//    }

//    @Test
//    void addReview_shouldThrowException_whenUserIsNotDoctor() {
//        User patient = User.builder()
//                .id(1)
//                .build();
//
//        User notDoctor = User.builder()
//                .id(2)
//                .specialization(null)
//                .build();
//
//        SecurityContextHolder.getContext().setAuthentication(
//                new UsernamePasswordAuthenticationToken(patient, null)
//        );
//
//        AddReviewDto dto = AddReviewDto.builder()
//                .doctorId(2)
//                .review("Review")
//                .anonymousReview(false)
//                .build();
//
//        when(userJpaRepo.findById(2)).thenReturn(Optional.of(notDoctor));
//
//        assertThrows(UserIsNotDoctor.class, () -> reviewService.addReview(dto));
//
//        verify(reviewJpaRepo, never()).save(any());
//    }

//    @Test
//    void getOwnReviews_shouldReturnOwnReviews() {
//        User patient = User.builder()
//                .id(1)
//                .build();
//
//        User doctor = User.builder()
//                .id(2)
//                .build();
//
//        ReviewEntity review = ReviewEntity.builder()
//                .id(1L)
//                .patientId(patient)
//                .doctorId(doctor)
//                .comment("Good")
//                .anonymousReview(false)
//                .build();
//
//        SecurityContextHolder.getContext().setAuthentication(
//                new UsernamePasswordAuthenticationToken(patient, null)
//        );
//
//        when(reviewJpaRepo.findReviewEntityByPatientId(patient))
//                .thenReturn(List.of(review));
//
//        var result = reviewService.getOwnReviews();
//
//        assertNotNull(result);
//        assertEquals(1, result.size());
//        verify(reviewJpaRepo).findReviewEntityByPatientId(patient);
//    }

    @Test
    void getAllReviews_shouldReturnAllReviews() {
        User patient = User.builder()
                .id(1)
                .build();

        User doctor = User.builder()
                .id(2)
                .build();

        ReviewEntity review = ReviewEntity.builder()
                .id(1L)
                .patientId(patient)
                .doctorId(doctor)
                .comment("Good")
                .anonymousReview(false)
                .build();

        when(reviewJpaRepo.findAll()).thenReturn(List.of(review));

        var result = reviewService.getAllReviews();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(reviewJpaRepo).findAll();
    }

    @Test
    void deleteReview_shouldDeleteReview_whenReviewExists() {
        User patient = User.builder()
                .id(1)
                .email("patient@test.com")
                .build();

        ReviewEntity review = ReviewEntity.builder()
                .id(1L)
                .patientId(patient)
                .comment("Good")
                .anonymousReview(false)
                .build();

        DeleteReviewDto dto = DeleteReviewDto.builder()
                .id(1L)
                .build();

        when(reviewJpaRepo.findById(1L)).thenReturn(Optional.of(review));

        reviewService.deleteReview(dto);

        verify(reviewJpaRepo).delete(review);
    }

    @Test
    void deleteReview_shouldThrowException_whenReviewDoesNotExist() {
        DeleteReviewDto dto = DeleteReviewDto.builder()
                .id(1L)
                .build();

        when(reviewJpaRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> reviewService.deleteReview(dto));

        verify(reviewJpaRepo, never()).delete(any());
    }
}
