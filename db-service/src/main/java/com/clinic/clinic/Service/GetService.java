package com.clinic.clinic.Service;

import com.clinic.clinic.Entity.Appointment.AppointmentBDTO;
import com.clinic.clinic.Entity.Appointment.AppointmentDto;
import com.clinic.clinic.Entity.Appointment.AppointmentEntity;
import com.clinic.clinic.Entity.Review.ReviewDto;
import com.clinic.clinic.Entity.Review.ReviewEntity;
import com.clinic.clinic.Entity.User.User;
import com.clinic.clinic.Entity.User.UserDTO;
import com.clinic.clinic.JpaRepo.AppointmentJpaRepo;
import com.clinic.clinic.JpaRepo.ReviewJpaRepo;
import com.clinic.clinic.JpaRepo.UserJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetService {
    private final AppointmentJpaRepo appointmentJpaRepo;
    private final UserJpaRepo userJpaRepo;
    private final ReviewJpaRepo reviewJpaRepo;

    public List<AppointmentDto> getAvailableAppointments(String specialization){
        Optional<List<AppointmentEntity>> appointmentDtos = appointmentJpaRepo.findAppointmentEntitiesByNeededSpecialization(specialization);
        List<AppointmentDto> appointments = new java.util.ArrayList<>(List.of());
        if (appointmentDtos.isPresent()) {
            for (AppointmentEntity appointmentEntity : appointmentDtos.get()) {
                appointments.add(new AppointmentDto(appointmentEntity));
            }
        }
        return appointments;
    }

    public List<AppointmentDto> getMyAppointments(String email) {
        User patientId = userJpaRepo.findByEmail(email).get();
        Optional<List<AppointmentEntity>> appointmentDtos = appointmentJpaRepo.findByPatientId(patientId);
        List<AppointmentDto> appointments = new java.util.ArrayList<>(List.of());
        if (appointmentDtos.isPresent()) {
            for (AppointmentEntity appointmentEntity : appointmentDtos.get()) {
                appointments.add(new AppointmentDto(appointmentEntity));
            }
        }
        return appointments;
    }

    public List<AppointmentDto> getOwnAppointments(String email) {
        User doctorId = userJpaRepo.findByEmail(email).get();
        Optional<List<AppointmentEntity>> appointmentDtos = appointmentJpaRepo.findByDoctorId(doctorId);
        List<AppointmentDto> appointments = new java.util.ArrayList<>(List.of());
        if (appointmentDtos.isPresent()) {
            for (AppointmentEntity appointmentEntity : appointmentDtos.get()) {
                appointments.add(new AppointmentDto(appointmentEntity));
            }
        }
        return appointments;
    }

    public List<ReviewDto> getReviews(Integer id) {

        System.out.println(id);

        User user = userJpaRepo.findById(id).get();

        Optional<List<ReviewEntity>> reviewEntities = Optional.ofNullable(
                reviewJpaRepo.findReviewEntityByDoctorId(user)
        );
        List<ReviewDto> reviews = new java.util.ArrayList<>(List.of());
        if (reviewEntities.isPresent()) {
            for (ReviewEntity reviewEntity : reviewEntities.get()) {
                reviews.add(new ReviewDto(reviewEntity));
            }
        }
        System.out.println(reviews.size());
        return reviews;
    }

    public UserDTO getUser(String email) {
        User userEntity = userJpaRepo.findByEmail(email).get();
        var user =  UserDTO.builder()
                .id(userEntity.getId())
                .email(userEntity.getEmail())
                .firstName(userEntity.getFirstName())
                .lastName(userEntity.getLastName())
                .password(userEntity.getPassword())
                .roles(userEntity.getRoles().stream().map(r -> r.getName()).collect(Collectors.toList()))
                .specialization(userEntity.getSpecialization())
                .build();
        return user;
    }

    public AppointmentBDTO getAppointment(Integer id) {
        AppointmentEntity appointmentEntity = appointmentJpaRepo.findById(id).get();
        System.out.println(appointmentEntity.getNeededSpecialization());
        var appointment =  AppointmentBDTO.builder()
                .appointmentId(appointmentEntity.getId())
                .appointmentDate(appointmentEntity.getDate())
                .doctorId(appointmentEntity.getDoctorId()!= null ? appointmentEntity.getDoctorId().getId() : null)
                .pacientId(appointmentEntity.getPatientId().getId())
                .specialization(appointmentEntity.getNeededSpecialization())
                .build();
        System.out.println(appointment.getSpecialization());
        return appointment;
    }
}
