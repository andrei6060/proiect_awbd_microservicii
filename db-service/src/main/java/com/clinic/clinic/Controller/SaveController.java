package com.clinic.clinic.Controller;

import com.clinic.clinic.Entity.Appointment.AppointmentBDTO;
import com.clinic.clinic.Entity.Review.AddReviewDto;
import com.clinic.clinic.Entity.Review.ReviewDto;
import com.clinic.clinic.Entity.Review.TransferReviewDTO;
import com.clinic.clinic.Entity.User.RegistrationRequestDto;
import com.clinic.clinic.Entity.User.TokenDTO;
import com.clinic.clinic.Entity.User.User;
import com.clinic.clinic.Entity.User.UserDTO;
import com.clinic.clinic.Service.SaveService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("save")
@RequiredArgsConstructor
public class SaveController {
    private final SaveService saveService;

    @PostMapping(path = "user",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void saveUser(@RequestBody UserDTO user) throws MessagingException {
        saveService.saveUserAuth(user);
    }

    @PostMapping(path = "userConfirm",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void saveUserConfirm(@RequestBody UserDTO user) throws MessagingException {
        saveService.saveUser(user);
    }

    @PostMapping(path = "token",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void saveToken(@RequestBody TokenDTO token) throws MessagingException {
        saveService.saveToken(token);
    }

    @PostMapping(path = "appointment",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void saveAppointment(@RequestBody AppointmentBDTO appointmentBDTO) throws MessagingException {
        if (appointmentBDTO.getDoctorId() != null) {
            saveService.SaveAcceptAppointment(appointmentBDTO);
        } else {
            saveService.saveAppointment(appointmentBDTO);
        }
    }

    @PostMapping(path = "review",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void saveReview(@RequestBody TransferReviewDTO review) throws MessagingException {
        saveService.saveReview(review);
    }




}
