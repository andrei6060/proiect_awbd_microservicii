package com.clinic.clinic.Controller;

import com.clinic.clinic.Service.DeleteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("delete")
@RequiredArgsConstructor
public class DeleteController {
    private final DeleteService deleteService;

    @DeleteMapping("appointment")
    public void deleteAppointment(@RequestParam Integer appointmentId ) {
        deleteService.deleteAppointment(appointmentId);
    }

    @PutMapping("doctorFromAppointment")
    public void deleteDoctorFromAppointment(@RequestParam Integer appointmentId) {
        deleteService.deleteDoctorFromAppointment(appointmentId);
    }
}
