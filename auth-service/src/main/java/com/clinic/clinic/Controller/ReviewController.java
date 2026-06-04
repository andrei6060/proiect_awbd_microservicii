//package com.clinic.clinic.Controller;
//
//import com.clinic.clinic.Entity.Review.AddReviewDto;
//import com.clinic.clinic.Entity.Review.DeleteReviewDto;
//import com.clinic.clinic.Entity.Review.ReviewDto;
//import com.clinic.clinic.Service.ReviewService;
//import jakarta.mail.MessagingException;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("review")
//@RequiredArgsConstructor
//public class ReviewController {
//    private final ReviewService reviewService;
//
//    @PostMapping("/addReview")
//    @PreAuthorize("hasAnyAuthority('USER')")
//    public ResponseEntity<?> addReview(@Valid @RequestBody AddReviewDto review) throws MessagingException {
//        reviewService.addReview(review);
//        return ResponseEntity.ok().build();
//    }
//
//    @GetMapping("/getOwnReviews")
//    @PreAuthorize("hasAnyAuthority('USER')")
//    public ResponseEntity<List<ReviewDto>> getOwnReviews() throws MessagingException {
//        return ResponseEntity.ok(reviewService.getOwnReviews());
//    }
//
//    @GetMapping("/getOwnReviewsDoctor")
//    @PreAuthorize("hasAnyAuthority('DOCTOR')")
//    public ResponseEntity<List<ReviewDto>> getOwnReviewsDoctor() throws MessagingException {
//        return ResponseEntity.ok(reviewService.getOwnReviewsDoctor());
//    }
//
//
//    @GetMapping("/getAllReviews")
//    @PreAuthorize("hasAnyAuthority('DOCTOR')")
//    public ResponseEntity<List<ReviewDto>> getAllReviews() throws MessagingException {
//        return ResponseEntity.ok(reviewService.getAllReviews());
//    }
//
//    @DeleteMapping("/deleteReview")
//    @PreAuthorize("hasAnyAuthority('USER')")
//    public ResponseEntity<?> deleteReview(@Valid @RequestBody DeleteReviewDto review){
//        reviewService.deleteReview(review);
//        return ResponseEntity.ok().build();
//    }
//}
