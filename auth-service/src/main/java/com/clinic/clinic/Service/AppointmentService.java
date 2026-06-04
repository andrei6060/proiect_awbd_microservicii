//package com.clinic.clinic.Service;
//
//import com.clinic.clinic.Entity.Appointment.*;
//import com.clinic.clinic.JpaRepo.AppointmentJpaRepo;
//import com.clinic.clinic.global.AppointmentAlreadyAcceptedException;
//import com.clinic.clinic.global.AppointmentNotFound;
//import com.clinic.clinic.global.AppointmentNotMatchingException;
//import com.clinic.clinic.Entity.User.User;
//import com.clinic.clinic.JpaRepo.UserJpaRepo;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Optional;
//
//@Service
//@RequiredArgsConstructor
//public class AppointmentService {
//
//    private final AppointmentJpaRepo appointmentJpaRepo;
//    private final UserJpaRepo userJpaRepo;
//
//    public void addAppointment(AddAppointmentDto appointmentEntity) {
//        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        var appointment = AppointmentEntity.builder()
//                .neededSpecialization(String.valueOf(appointmentEntity.getNeededSpecialization()))
//                .date(appointmentEntity.getDate()!= null?appointmentEntity.getDate():null)
//                .patientId(user)
//                .build();
//        appointmentJpaRepo.save(appointment);
//    }
//
//    public void acceptAppointment(AcceptAppointmentDto appointmentId) {
//        AppointmentEntity appointment = appointmentJpaRepo.findById(appointmentId.getAppointmentId()).orElseThrow(
//                () -> new AppointmentNotFound(appointmentId.getAppointmentId())
//        );
//        String email = SecurityContextHolder.getContext().getAuthentication().getName();
//        User doctor = userJpaRepo.findByEmail(email).get();
//        String specialization = String.valueOf(doctor.getSpecialization());
//            if(appointment.getDoctorId()==null) {
//                if(appointment.getNeededSpecialization().equals(specialization)) {
//                    appointment.setDoctorId(doctor);
//                    appointmentJpaRepo.save(appointment);
//                } else {
//                    throw new AppointmentNotMatchingException(appointmentId.getAppointmentId(), specialization);
//                }
//            } else{
//                throw new AppointmentAlreadyAcceptedException(appointmentId.getAppointmentId(), doctor.getId());
//            }
//
//    }
//
//    public List<AppointmentDto> getAllAppointments() {
//        List<AppointmentEntity> appointmentDtos = appointmentJpaRepo.findAll();
//        List<AppointmentDto> appointments = new java.util.ArrayList<>(List.of());
//            for (AppointmentEntity appointmentEntity : appointmentDtos) {
//                appointments.add(new AppointmentDto(appointmentEntity));
//        }
//        return appointments;
//    }
//
//    public List<AppointmentDto> getAvailableApoointments() {
//        String email = SecurityContextHolder.getContext().getAuthentication().getName();
//        String specialization = String.valueOf(userJpaRepo.findByEmail(email).get().getSpecialization());
//        Optional<List<AppointmentEntity>> appointmentDtos = appointmentJpaRepo.findAppointmentEntitiesByNeededSpecialization(specialization);
//        List<AppointmentDto> appointments = new java.util.ArrayList<>(List.of());
//        if (appointmentDtos.isPresent()) {
//            for (AppointmentEntity appointmentEntity : appointmentDtos.get()) {
//                appointments.add(new AppointmentDto(appointmentEntity));
//            }
//        }
//        System.out.println(appointments.size());
//        return appointments;
//    }
//
//    public List<AppointmentDto> getMyAppointments() {
//        String email = SecurityContextHolder.getContext().getAuthentication().getName();
//        User patientId = userJpaRepo.findByEmail(email).get();
//        Optional<List<AppointmentEntity>> appointmentDtos = appointmentJpaRepo.findByPatientId(patientId);
//        List<AppointmentDto> appointments = new java.util.ArrayList<>(List.of());
//        if (appointmentDtos.isPresent()) {
//            for (AppointmentEntity appointmentEntity : appointmentDtos.get()) {
//                appointments.add(new AppointmentDto(appointmentEntity));
//            }
//        }
//        return appointments;
//    }
//    public List<AppointmentDto> getOwnAppointments() {
//        String email = SecurityContextHolder.getContext().getAuthentication().getName();
//        User doctorId = userJpaRepo.findByEmail(email).get();
//        Optional<List<AppointmentEntity>> appointmentDtos = appointmentJpaRepo.findByDoctorId(doctorId);
//        List<AppointmentDto> appointments = new java.util.ArrayList<>(List.of());
//        if (appointmentDtos.isPresent()) {
//            for (AppointmentEntity appointmentEntity : appointmentDtos.get()) {
//                appointments.add(new AppointmentDto(appointmentEntity));
//            }
//        }
//        return appointments;
//    }
//    public void deleteDoctorFromAppointment(DeleteAppointmentDto appointmentId) {
//        AppointmentEntity appointment = appointmentJpaRepo.findById(appointmentId.getAppointmentId()).orElseThrow(
//                () -> new AppointmentNotFound(appointmentId.getAppointmentId())
//        );
//        appointment.setDoctorId(null);
//        appointmentJpaRepo.save(appointment);
//    }
//
//    public void deleteAppointment(DeleteAppointmentDto dto) {
//        appointmentJpaRepo.findById(dto.getAppointmentId()).orElseThrow(
//                () -> new AppointmentNotFound(dto.getAppointmentId())
//        );
//        appointmentJpaRepo.deleteById(dto.getAppointmentId());
//    }
//}
