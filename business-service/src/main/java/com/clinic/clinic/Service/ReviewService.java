package com.clinic.clinic.Service;

import com.clinic.clinic.Entity.Appointment.AppointmentBDTO;
import com.clinic.clinic.Entity.Appointment.AppointmentDto;
import com.clinic.clinic.Entity.Review.*;
import com.clinic.clinic.Entity.User.User;
import com.clinic.clinic.Entity.common.PageResponse;
import com.clinic.clinic.Entity.common.PaginationUtil;
import com.clinic.clinic.JpaRepo.ReviewJpaRepo;
import com.clinic.clinic.global.UserIsNotDoctor;
import com.clinic.clinic.JpaRepo.UserJpaRepo;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("rating", "aspect");
    private static final String DEFAULT_SORT_FIELD = "rating";

    private final ReviewJpaRepo reviewJpaRepo;
    private final RestTemplate restTemplate;

    @Value("${application.pagination.default-page-size:10}")
    private int defaultPageSize;

    @Value("${application.pagination.max-page-size:50}")
    private int maxPageSize;

    public void addReview(AddReviewDto review) {
        User pacient = (User) SecurityContextHolder.getContext()
            .getAuthentication()
            .getPrincipal();
        var dto = TransferReviewDTO.builder()
                .review(review.getReview())
                .anonymousReview(review.getAnonymousReview())
                .rating(review.getRating())
                .aspect(review.getAspect())
                .patientId(pacient.getId())
                .doctorId(review.getDoctorId())
                .build();
        HttpHeaders headersToken = new HttpHeaders();
        headersToken.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<TransferReviewDTO> request_api_token = new HttpEntity<>(dto, headersToken);

        log.info("Submitting review from patient {} for doctor {}", pacient.getId(), review.getDoctorId());

        String url_token = "http://localhost:8086/api/v1/save/review";

        ResponseEntity<Void> response_token = restTemplate.exchange(
                url_token,
                HttpMethod.POST,
                request_api_token,
                Void.class
        );
    }

    public List<ReviewDto> getOwnReviews() {
        User pacient = (User) SecurityContextHolder.getContext()
            .getAuthentication()
            .getPrincipal();
        log.debug("Fetching own reviews for patient {}", pacient.getId());
        HttpHeaders headersToken = new HttpHeaders();
        headersToken.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Integer> request_api_token = new HttpEntity<>(pacient.getId(), headersToken);

        String url = "http://localhost:8086/api/v1/get/reviews?id=" + pacient.getId();
        ResponseEntity<ReviewDto[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                ReviewDto[].class
        );
        List<ReviewDto> reviews = Arrays.asList(response.getBody());
        return reviews;




    }
    public List<ReviewDto> getOwnReviewsDoctor() {
        User doctor = (User) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        log.debug("Fetching reviews addressed to doctor {}", doctor.getId());
        Optional<List<ReviewEntity>> reviewEntities = Optional.ofNullable(
                reviewJpaRepo.findReviewEntityByDoctorId(doctor)
        );
        List<ReviewDto> reviews = new java.util.ArrayList<>(List.of());
        if (reviewEntities.isPresent()) {
            for (ReviewEntity reviewEntity : reviewEntities.get()) {
                reviews.add(new ReviewDto(reviewEntity));
            }
        }
        return reviews;
    }

    public List<ReviewDto> getAllReviews() {
        Optional<List<ReviewEntity>> reviewEntities = Optional.of(
            reviewJpaRepo.findAll()
        );
        List<ReviewDto> reviews = new java.util.ArrayList<>(List.of());
        if (reviewEntities.isPresent()) {
            for (ReviewEntity reviewEntity : reviewEntities.get()) {
                reviews.add(new ReviewDto(reviewEntity));
            }
        }
        return reviews;
    }

    public PageResponse<ReviewDto> getAllReviews(int page, int size, String sortBy, String direction) {
        PaginationUtil.PageQuery query = PaginationUtil.resolve(
                page, size, sortBy, direction,
                ALLOWED_SORT_FIELDS, DEFAULT_SORT_FIELD,
                defaultPageSize, maxPageSize);
        Page<ReviewEntity> entityPage = reviewJpaRepo.findAll(query.pageable());
        List<ReviewDto> content = entityPage.getContent().stream()
                .map(ReviewDto::new)
                .toList();
        return PageResponse.from(entityPage, content, query.sortBy(), query.direction());
    }

    public void deleteReview(DeleteReviewDto dto) {
        Optional<ReviewEntity> reviewEntity = reviewJpaRepo.findById(
            dto.getId()
        );
        if (reviewEntity.isPresent()) {
            reviewJpaRepo.delete(reviewEntity.get());
            log.info("Deleted review {}", dto.getId());
        } else {
            log.warn("Cannot delete review {}: not found", dto.getId());
            throw new RuntimeException("Review not found");
        }
    }
}
