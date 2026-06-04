package com.clinic.clinic.Entity.Review;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {
    String review;
    Integer rating;
    Integer patientId;
    String aspect;

    public ReviewDto(ReviewEntity reviewEntity) {
        this.review = reviewEntity.getComment();
        this.rating = reviewEntity.getRating();
        this.aspect = reviewEntity.getAspect();
        if(!reviewEntity.getAnonymousReview()){
            this.patientId = reviewEntity.getPatientId().getId();
        }else{
            this.patientId = null;
        }
    }


}
