package com.clinic.clinic.service;

import com.clinic.clinic.Entity.Appointment.*;
import com.clinic.clinic.Entity.User.Specializations;
import com.clinic.clinic.Entity.User.User;
import com.clinic.clinic.JpaRepo.AppointmentJpaRepo;
import com.clinic.clinic.JpaRepo.UserJpaRepo;
import com.clinic.clinic.Service.AppointmentService;
import com.clinic.clinic.global.AppointmentAlreadyAcceptedException;
import com.clinic.clinic.global.AppointmentNotFound;
import com.clinic.clinic.global.AppointmentNotMatchingException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentJpaRepo appointmentJpaRepo;
    @Mock
    private UserJpaRepo userJpaRepo;

    @InjectMocks
    private AppointmentService appointmentService;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(User user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList()));
    }

    private User user(int id, String email, Specializations spec) {
        return User.builder().id(id).email(email).firstName("F").lastName("L").specialization(spec).build();
    }

    private AppointmentEntity appointment(Long id, User patient, User doctor, String spec) {
        return AppointmentEntity.builder()
                .id(id).patientId(patient).doctorId(doctor).neededSpecialization(spec)
                .date(LocalDateTime.now()).build();
    }

    @Test
    void addAppointment_shouldSave() {
        User patient = user(1, "p@test.com", null);
        authenticateAs(patient);
        AddAppointmentDto dto = AddAppointmentDto.builder()
                .date(LocalDateTime.now()).neededSpecialization(Specializations.CARDIOLOGIE).build();

        appointmentService.addAppointment(dto);

        verify(appointmentJpaRepo).save(any(AppointmentEntity.class));
    }

    @Test
    void acceptAppointment_whenMatchingAndFree_shouldAssignDoctor() {
        User patient = user(1, "p@test.com", null);
        User doctor = user(2, "doc@test.com", Specializations.CARDIOLOGIE);
        authenticateAs(doctor);
        AppointmentEntity appt = appointment(5L, patient, null, "CARDIOLOGIE");
        when(appointmentJpaRepo.findById(5)).thenReturn(Optional.of(appt));
        when(userJpaRepo.findByEmail("doc@test.com")).thenReturn(Optional.of(doctor));

        appointmentService.acceptAppointment(AcceptAppointmentDto.builder().appointmentId(5).build());

        assertEquals(doctor, appt.getDoctorId());
        verify(appointmentJpaRepo).save(appt);
    }

    @Test
    void acceptAppointment_whenNotFound_shouldThrow() {
        when(appointmentJpaRepo.findById(5)).thenReturn(Optional.empty());

        assertThrows(AppointmentNotFound.class,
                () -> appointmentService.acceptAppointment(AcceptAppointmentDto.builder().appointmentId(5).build()));
    }

    @Test
    void acceptAppointment_whenAlreadyAccepted_shouldThrow() {
        User patient = user(1, "p@test.com", null);
        User doctor = user(2, "doc@test.com", Specializations.CARDIOLOGIE);
        User otherDoctor = user(3, "doc2@test.com", Specializations.CARDIOLOGIE);
        authenticateAs(doctor);
        AppointmentEntity appt = appointment(5L, patient, otherDoctor, "CARDIOLOGIE");
        when(appointmentJpaRepo.findById(5)).thenReturn(Optional.of(appt));
        when(userJpaRepo.findByEmail("doc@test.com")).thenReturn(Optional.of(doctor));

        assertThrows(AppointmentAlreadyAcceptedException.class,
                () -> appointmentService.acceptAppointment(AcceptAppointmentDto.builder().appointmentId(5).build()));
    }

    @Test
    void acceptAppointment_whenSpecializationMismatch_shouldThrow() {
        User patient = user(1, "p@test.com", null);
        User doctor = user(2, "doc@test.com", Specializations.CARDIOLOGIE);
        authenticateAs(doctor);
        AppointmentEntity appt = appointment(5L, patient, null, "ORL");
        when(appointmentJpaRepo.findById(5)).thenReturn(Optional.of(appt));
        when(userJpaRepo.findByEmail("doc@test.com")).thenReturn(Optional.of(doctor));

        assertThrows(AppointmentNotMatchingException.class,
                () -> appointmentService.acceptAppointment(AcceptAppointmentDto.builder().appointmentId(5).build()));
    }

    @Test
    void getAllAppointments_shouldMap() {
        User patient = user(1, "p@test.com", null);
        when(appointmentJpaRepo.findAll()).thenReturn(List.of(appointment(5L, patient, null, "ORL")));

        List<AppointmentDto> result = appointmentService.getAllAppointments();

        assertEquals(1, result.size());
    }

    @Test
    void getAvailableAppointments_shouldMap() {
        User doctor = user(2, "doc@test.com", Specializations.CARDIOLOGIE);
        User patient = user(1, "p@test.com", null);
        authenticateAs(doctor);
        when(userJpaRepo.findByEmail("doc@test.com")).thenReturn(Optional.of(doctor));
        when(appointmentJpaRepo.findAppointmentEntitiesByNeededSpecialization("CARDIOLOGIE"))
                .thenReturn(Optional.of(List.of(appointment(5L, patient, null, "CARDIOLOGIE"))));

        List<AppointmentDto> result = appointmentService.getAvailableApoointments();

        assertEquals(1, result.size());
    }

    @Test
    void getMyAppointments_shouldMap() {
        User patient = user(1, "p@test.com", null);
        authenticateAs(patient);
        when(userJpaRepo.findByEmail("p@test.com")).thenReturn(Optional.of(patient));
        when(appointmentJpaRepo.findByPatientId(patient))
                .thenReturn(Optional.of(List.of(appointment(5L, patient, null, "ORL"))));

        List<AppointmentDto> result = appointmentService.getMyAppointments();

        assertEquals(1, result.size());
    }

    @Test
    void getOwnAppointments_shouldMap() {
        User patient = user(1, "p@test.com", null);
        User doctor = user(2, "doc@test.com", Specializations.CARDIOLOGIE);
        authenticateAs(doctor);
        when(userJpaRepo.findByEmail("doc@test.com")).thenReturn(Optional.of(doctor));
        when(appointmentJpaRepo.findByDoctorId(doctor))
                .thenReturn(Optional.of(List.of(appointment(5L, patient, doctor, "CARDIOLOGIE"))));

        List<AppointmentDto> result = appointmentService.getOwnAppointments();

        assertEquals(1, result.size());
    }

    @Test
    void deleteDoctorFromAppointment_shouldNullDoctorAndSave() {
        User patient = user(1, "p@test.com", null);
        User doctor = user(2, "doc@test.com", Specializations.CARDIOLOGIE);
        AppointmentEntity appt = appointment(5L, patient, doctor, "CARDIOLOGIE");
        when(appointmentJpaRepo.findById(5)).thenReturn(Optional.of(appt));

        appointmentService.deleteDoctorFromAppointment(DeleteAppointmentDto.builder().appointmentId(5).build());

        assertNull(appt.getDoctorId());
        verify(appointmentJpaRepo).save(appt);
    }

    @Test
    void deleteDoctorFromAppointment_whenNotFound_shouldThrow() {
        when(appointmentJpaRepo.findById(5)).thenReturn(Optional.empty());

        assertThrows(AppointmentNotFound.class,
                () -> appointmentService.deleteDoctorFromAppointment(DeleteAppointmentDto.builder().appointmentId(5).build()));
    }

    @Test
    void deleteAppointment_shouldDelete() {
        User patient = user(1, "p@test.com", null);
        when(appointmentJpaRepo.findById(5)).thenReturn(Optional.of(appointment(5L, patient, null, "ORL")));

        appointmentService.deleteAppointment(DeleteAppointmentDto.builder().appointmentId(5).build());

        verify(appointmentJpaRepo).deleteById(5);
    }

    @Test
    void deleteAppointment_whenNotFound_shouldThrow() {
        when(appointmentJpaRepo.findById(5)).thenReturn(Optional.empty());

        assertThrows(AppointmentNotFound.class,
                () -> appointmentService.deleteAppointment(DeleteAppointmentDto.builder().appointmentId(5).build()));
        verify(appointmentJpaRepo, never()).deleteById(any());
    }
}
