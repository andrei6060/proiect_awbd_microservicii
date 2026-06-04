package com.clinic.clinic.Entity.Review;

import com.clinic.clinic.Entity.User.User;
import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Builder
@Entity
@Table(name = "reviews")
@NoArgsConstructor
@AllArgsConstructor
public class ReviewEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne()
    @JoinColumn(name = "patient")
    @NonNull
    private User patientId;

    private Boolean anonymousReview;

    private Integer rating;

    private String aspect;



    @ManyToOne
    @JoinColumn(name = "doctor")
    private User doctorId;

    private String comment;


}
