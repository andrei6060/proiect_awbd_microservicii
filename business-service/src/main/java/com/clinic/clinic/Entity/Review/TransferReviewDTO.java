package com.clinic.clinic.Entity.Review;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransferReviewDTO {
    private Integer doctorId;

    private String review;

    private Boolean anonymousReview;

    private Integer rating;
    private String aspect;

    private Integer patientId;
}
