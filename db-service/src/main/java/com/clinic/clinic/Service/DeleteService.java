package com.clinic.clinic.Service;

import com.clinic.clinic.Entity.Appointment.AppointmentEntity;
import com.clinic.clinic.JpaRepo.AppointmentJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteService {
    private final AppointmentJpaRepo appointmentJpaRepo;

    public void deleteAppointment(Integer id) {
        appointmentJpaRepo.deleteById(id);
    }

    public void deleteDoctorFromAppointment(Integer appointmentId) {
        AppointmentEntity appointmentEntity = appointmentJpaRepo.findById(appointmentId).get();
        appointmentEntity.setDoctorId(null);
        appointmentJpaRepo.save(appointmentEntity);
    }
}
