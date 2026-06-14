package com.clinic.clinic.service;

import com.clinic.clinic.Entity.Appointment.AppointmentEntity;
import com.clinic.clinic.Entity.User.User;
import com.clinic.clinic.JpaRepo.AppointmentJpaRepo;
import com.clinic.clinic.Service.DeleteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteServiceTest {

    @Mock
    private AppointmentJpaRepo appointmentJpaRepo;

    @InjectMocks
    private DeleteService deleteService;

    @Test
    void deleteAppointment_shouldDelegateToRepo() {
        deleteService.deleteAppointment(7);

        verify(appointmentJpaRepo).deleteById(7);
    }

    @Test
    void deleteDoctorFromAppointment_shouldNullDoctorAndSave() {
        User patient = User.builder().id(1).email("p@test.com").build();
        User doctor = User.builder().id(2).email("doc@test.com").build();
        AppointmentEntity appt = AppointmentEntity.builder()
                .id(7L).patientId(patient).doctorId(doctor).neededSpecialization("ORL").build();
        when(appointmentJpaRepo.findById(7)).thenReturn(Optional.of(appt));

        deleteService.deleteDoctorFromAppointment(7);

        assertNull(appt.getDoctorId());
        verify(appointmentJpaRepo).save(appt);
    }
}
