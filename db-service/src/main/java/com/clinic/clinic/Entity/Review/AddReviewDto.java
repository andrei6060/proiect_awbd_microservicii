package com.clinic.clinic.Entity.Review;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddReviewDto {

    @NotNull(message = "Doctor ID can't be null")
    private Integer doctorId;

    @NotNull(message = "Review text can't be null")
    private String review;

    @NotNull(message = "Anonimity option must not be null")
    private Boolean anonymousReview;

    private Integer rating;
    private String aspect;
}
