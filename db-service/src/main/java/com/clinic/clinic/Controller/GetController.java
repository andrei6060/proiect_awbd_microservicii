package com.clinic.clinic.Controller;

import com.clinic.clinic.Entity.Appointment.AppointmentBDTO;
import com.clinic.clinic.Entity.Appointment.AppointmentDto;
import com.clinic.clinic.Entity.Review.ReviewDto;
import com.clinic.clinic.Entity.User.UserDTO;
import com.clinic.clinic.JpaRepo.AppointmentJpaRepo;
import com.clinic.clinic.Service.GetService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("get")
@RequiredArgsConstructor
public class GetController {
    private final GetService getService;

    @GetMapping("user")
    public UserDTO getUser(@RequestParam String email) {
        return getService.getUser(email);
    }

    @GetMapping("appointment")
    public AppointmentBDTO getAppointment(@RequestParam Integer id) {
        return getService.getAppointment(id);
    }

    @GetMapping("availableAppointments")
    public List<AppointmentDto> getAvailableAppointments(@RequestParam String specialization) {
        return getService.getAvailableAppointments(specialization);
    }

    @GetMapping("myAppointments")
    public List<AppointmentDto> getMyAppointments(@RequestParam String email) {
        return getService.getMyAppointments(email);
    }

    @GetMapping("ownAppointments")
    public List<AppointmentDto> getOwnAppointments(@RequestParam String email) {
        return getService.getOwnAppointments(email);
    }

    @GetMapping("reviews")
    public List<ReviewDto> getReviews(@RequestParam Integer id) {
        return getService.getReviews(id);
    }

}
