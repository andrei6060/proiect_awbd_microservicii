package com.clinic.clinic.service;

import com.clinic.clinic.Entity.Appointment.AppointmentBDTO;
import com.clinic.clinic.Entity.Appointment.AppointmentDto;
import com.clinic.clinic.Entity.Appointment.AppointmentEntity;
import com.clinic.clinic.Entity.Review.ReviewDto;
import com.clinic.clinic.Entity.Review.ReviewEntity;
import com.clinic.clinic.Entity.User.Specializations;
import com.clinic.clinic.Entity.User.User;
import com.clinic.clinic.Entity.User.UserDTO;
import com.clinic.clinic.JpaRepo.AppointmentJpaRepo;
import com.clinic.clinic.JpaRepo.ReviewJpaRepo;
import com.clinic.clinic.JpaRepo.UserJpaRepo;
import com.clinic.clinic.Service.GetService;
import com.clinic.clinic.role.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetServiceTest {

    @Mock
    private AppointmentJpaRepo appointmentJpaRepo;
    @Mock
    private UserJpaRepo userJpaRepo;
    @Mock
    private ReviewJpaRepo reviewJpaRepo;

    @InjectMocks
    private GetService getService;

    private User user(int id, Specializations spec) {
        return User.builder().id(id).email(id + "@test.com").firstName("F").lastName("L")
                .specialization(spec).roles(List.of(Role.builder().name("USER").build())).build();
    }

    private AppointmentEntity appointment(Long id, User patient) {
        return AppointmentEntity.builder()
                .id(id).patientId(patient).neededSpecialization("CARDIOLOGIE").date(LocalDateTime.now()).build();
    }

    @Test
    void getAvailableAppointments_shouldMap() {
        User patient = user(1, null);
        when(appointmentJpaRepo.findAppointmentEntitiesByNeededSpecialization("CARDIOLOGIE"))
                .thenReturn(Optional.of(List.of(appointment(5L, patient))));

        List<AppointmentDto> result = getService.getAvailableAppointments("CARDIOLOGIE");

        assertEquals(1, result.size());
    }

    @Test
    void getMyAppointments_shouldMap() {
        User patient = user(1, null);
        when(userJpaRepo.findByEmail("1@test.com")).thenReturn(Optional.of(patient));
        when(appointmentJpaRepo.findByPatientId(patient))
                .thenReturn(Optional.of(List.of(appointment(5L, patient))));

        List<AppointmentDto> result = getService.getMyAppointments("1@test.com");

        assertEquals(1, result.size());
    }

    @Test
    void getOwnAppointments_shouldMap() {
        User doctor = user(2, Specializations.CARDIOLOGIE);
        User patient = user(1, null);
        when(userJpaRepo.findByEmail("2@test.com")).thenReturn(Optional.of(doctor));
        when(appointmentJpaRepo.findByDoctorId(doctor))
                .thenReturn(Optional.of(List.of(appointment(5L, patient))));

        List<AppointmentDto> result = getService.getOwnAppointments("2@test.com");

        assertEquals(1, result.size());
    }

    @Test
    void getReviews_shouldMap() {
        User doctor = user(2, Specializations.CARDIOLOGIE);
        User patient = user(1, null);
        ReviewEntity review = ReviewEntity.builder()
                .id(1L).patientId(patient).doctorId(doctor).anonymousReview(false)
                .rating(5).aspect("kindness").comment("great").build();
        when(userJpaRepo.findById(2)).thenReturn(Optional.of(doctor));
        when(reviewJpaRepo.findReviewEntityByDoctorId(doctor)).thenReturn(List.of(review));

        List<ReviewDto> result = getService.getReviews(2);

        assertEquals(1, result.size());
    }

    @Test
    void getUser_shouldReturnDto() {
        User user = user(1, null);
        when(userJpaRepo.findByEmail("1@test.com")).thenReturn(Optional.of(user));

        UserDTO result = getService.getUser("1@test.com");

        assertEquals("1@test.com", result.getEmail());
        assertEquals(List.of("USER"), result.getRoles());
    }

    @Test
    void getAppointment_shouldReturnBdto() {
        User patient = user(1, null);
        when(appointmentJpaRepo.findById(5)).thenReturn(Optional.of(appointment(5L, patient)));

        AppointmentBDTO result = getService.getAppointment(5);

        assertEquals("CARDIOLOGIE", result.getSpecialization());
        assertEquals(1, result.getPacientId());
    }
}
