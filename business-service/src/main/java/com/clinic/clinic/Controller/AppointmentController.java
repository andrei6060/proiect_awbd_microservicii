package com.clinic.clinic.Controller;

import com.clinic.clinic.Entity.Appointment.AcceptAppointmentDto;
import com.clinic.clinic.Entity.Appointment.AddAppointmentDto;
import com.clinic.clinic.Entity.Appointment.AppointmentDto;
import com.clinic.clinic.Entity.Appointment.DeleteAppointmentDto;
import com.clinic.clinic.Service.AppointmentService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("appointment")
@RequiredArgsConstructor
public class AppointmentController {
    private final AppointmentService appointmentService;


    //verificatx2
    //done
    @PostMapping("/addAppointment")
    @PreAuthorize("hasAnyAuthority('USER')")
    public ResponseEntity<?> addAppointment(
            @Valid @RequestBody AddAppointmentDto appointmentEntity) throws MessagingException {
        appointmentService.addAppointment(appointmentEntity);
        return ResponseEntity.ok().build();
    }

    //verificatx2
    //done
    @PutMapping("/acceptAppointment")
    @PreAuthorize("hasAuthority('DOCTOR')")
    public ResponseEntity<?> acceptAppointment(@Valid @RequestBody AcceptAppointmentDto dto) throws MessagingException {
        try {
            appointmentService.acceptAppointment(dto);  // Apelăm serviciul pentru a accepta programarea
            return ResponseEntity.ok().build(); // Dacă totul merge bine, returnăm 200 OK
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    //verificatx2
    @GetMapping("/getAllAppointments")
    @PreAuthorize("hasAuthority('DOCTOR')")
    public ResponseEntity<List<AppointmentDto>> getAllAppointments() throws MessagingException {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    //verificatx2
    @GetMapping("/getAvailableAppointments")
    @PreAuthorize("hasAuthority('DOCTOR')")
    public ResponseEntity<List<AppointmentDto>> getAvailableAppointments() throws MessagingException {
        return ResponseEntity.ok(appointmentService.getAvailableApoointments());
    }

    //verificatx2
    @GetMapping("/getOwnAppointments")
    @PreAuthorize("hasAuthority('DOCTOR')")
    public ResponseEntity<List<AppointmentDto>> getOwnAppointments() throws MessagingException {
        return ResponseEntity.ok(appointmentService.getOwnAppointments());
    }
    @GetMapping("/getMyAppointments")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<List<AppointmentDto>> getMyAppointments() throws MessagingException {
        return ResponseEntity.ok(appointmentService.getMyAppointments());
    }

    @DeleteMapping("/notGoodDoctor")
    @PreAuthorize("hasAnyAuthority('DOCTOR', 'USER')")
    public ResponseEntity<?> deleteDoctorFromAppointment(@Valid @RequestBody DeleteAppointmentDto dto){
        appointmentService.deleteDoctorFromAppointment(dto);
        return ResponseEntity.ok().build();
    }


    //verificatx2
    @DeleteMapping("/deleteAppointment")
    @PreAuthorize("hasAnyAuthority('DOCTOR', 'USER')")
    public ResponseEntity<?> deleteAppointment(@Valid @RequestBody DeleteAppointmentDto dto){
        appointmentService.deleteAppointment(dto);
        return ResponseEntity.ok().build();
    }

}
