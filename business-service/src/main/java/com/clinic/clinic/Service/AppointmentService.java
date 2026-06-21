package com.clinic.clinic.Service;

import com.clinic.clinic.Entity.Appointment.*;
import com.clinic.clinic.Entity.User.Token;
import com.clinic.clinic.Entity.User.UserDTO;
import com.clinic.clinic.Entity.common.PageResponse;
import com.clinic.clinic.Entity.common.PaginationUtil;
import com.clinic.clinic.JpaRepo.AppointmentJpaRepo;
import com.clinic.clinic.global.AppointmentAlreadyAcceptedException;
import com.clinic.clinic.global.AppointmentNotFound;
import com.clinic.clinic.global.AppointmentNotMatchingException;
import com.clinic.clinic.Entity.User.User;
import com.clinic.clinic.JpaRepo.UserJpaRepo;
import com.clinic.clinic.global.RemoteServiceUnavailableException;
import com.clinic.clinic.resilience.DbServiceClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("date", "neededSpecialization");
    private static final String DEFAULT_SORT_FIELD = "date";

    private final AppointmentJpaRepo appointmentJpaRepo;
    private final UserJpaRepo userJpaRepo;
    private final RestTemplate restTemplate;
    private final DbServiceClient dbServiceClient;
    private final HttpServletRequest request;

    @Value("${application.pagination.default-page-size:10}")
    private int defaultPageSize;

    @Value("${application.pagination.max-page-size:50}")
    private int maxPageSize;

    @Transactional
    public void addAppointment(AddAppointmentDto appointmentEntity) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Creating appointment for patient {} (specialization {})",
                user.getId(), appointmentEntity.getNeededSpecialization());

        var appointment = AppointmentEntity.builder()
                .neededSpecialization(String.valueOf(appointmentEntity.getNeededSpecialization()))
                .date(appointmentEntity.getDate()!= null?appointmentEntity.getDate():null)
                .patientId(user)
                .build();
        var appointmentBDTO = AppointmentBDTO.builder()
                .specialization(String.valueOf(appointmentEntity.getNeededSpecialization()))
                .appointmentDate(appointmentEntity.getDate()!= null?appointmentEntity.getDate():null)
                .pacientId(user.getId())
                .build();
        HttpHeaders headersToken = new HttpHeaders();
         headersToken.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<AppointmentBDTO> request_api_token = new HttpEntity<>(appointmentBDTO, headersToken);

        String url_token = "http://db-service/api/v1/save/appointment";

        // Write op: cannot fabricate success -> on failure raise a clean 503 domain error.
        dbServiceClient.call(
                () -> restTemplate.exchange(url_token, HttpMethod.POST, request_api_token, Void.class),
                throwable -> { throw new RemoteServiceUnavailableException("db-service", throwable); }
        );
    }

    public void acceptAppointment(AcceptAppointmentDto appointmentId) {
        String urlAppointment = "http://db-service/api/v1/get/appointment?id=" + appointmentId.getAppointmentId();

        // Read needed to proceed: if db-service is down we cannot accept -> 503.
        AppointmentBDTO appointment = dbServiceClient.call(
                () -> restTemplate.exchange(urlAppointment, HttpMethod.GET, null, AppointmentBDTO.class).getBody(),
                throwable -> { throw new RemoteServiceUnavailableException("db-service", throwable); }
        );

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        String url = "http://db-service/api/v1/get/user?email=" + email;

        UserDTO doctor = dbServiceClient.call(
                () -> restTemplate.exchange(url, HttpMethod.GET, null, UserDTO.class).getBody(),
                throwable -> { throw new RemoteServiceUnavailableException("db-service", throwable); }
        );

        String specialization = String.valueOf(doctor.getSpecialization());
        log.debug("Doctor {} (specialization {}) accepting appointment {}",
                doctor.getId(), specialization, appointmentId.getAppointmentId());
            if(appointment.getDoctorId()==null) {
                if(appointment.getSpecialization().equals(specialization)) {

                    var appointmentBDTO = AppointmentBDTO.builder()
                            .specialization(appointment.getSpecialization())
                            .appointmentDate(appointment.getAppointmentDate()!= null?appointment.getAppointmentDate():null)
                            .pacientId(appointment.getPacientId())
                            .doctorId(doctor.getId())
                            .appointmentId(appointment.getAppointmentId())
                            .build();
                    HttpHeaders headersToken = new HttpHeaders();
                    headersToken.setContentType(MediaType.APPLICATION_JSON);

                    HttpEntity<AppointmentBDTO> request_api_token = new HttpEntity<>(appointmentBDTO, headersToken);

                    String url_token = "http://db-service/api/v1/save/appointment";

                    dbServiceClient.call(
                            () -> restTemplate.exchange(url_token, HttpMethod.POST, request_api_token, Void.class),
                            throwable -> { throw new RemoteServiceUnavailableException("db-service", throwable); }
                    );
                    log.info("Appointment {} accepted by doctor {}",
                            appointmentId.getAppointmentId(), doctor.getId());
                } else {
                    throw new AppointmentNotMatchingException(appointmentId.getAppointmentId(), specialization);
                }
            } else{
                throw new AppointmentAlreadyAcceptedException(appointmentId.getAppointmentId(), doctor.getId());
            }

    }

    public List<AppointmentDto> getAllAppointments() {
        List<AppointmentEntity> appointmentDtos = appointmentJpaRepo.findAll();
        List<AppointmentDto> appointments = new java.util.ArrayList<>(List.of());
            for (AppointmentEntity appointmentEntity : appointmentDtos) {
                appointments.add(new AppointmentDto(appointmentEntity));
        }
        return appointments;
    }

    public PageResponse<AppointmentDto> getAllAppointments(int page, int size, String sortBy, String direction) {
        PaginationUtil.PageQuery query = PaginationUtil.resolve(
                page, size, sortBy, direction,
                ALLOWED_SORT_FIELDS, DEFAULT_SORT_FIELD,
                defaultPageSize, maxPageSize);
        Page<AppointmentEntity> entityPage = appointmentJpaRepo.findAll(query.pageable());
        List<AppointmentDto> content = entityPage.getContent().stream()
                .map(AppointmentDto::new)
                .toList();
        return PageResponse.from(entityPage, content, query.sortBy(), query.direction());
    }

    public List<AppointmentDto> getAvailableApoointments() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        String specialization = String.valueOf(userJpaRepo.findByEmail(email).get().getSpecialization());

        String url = "http://db-service/api/v1/get/availableAppointments?specialization=" + specialization;

        // Read list: degrade gracefully to an empty list (never fabricate data).
        return dbServiceClient.call(
                () -> Arrays.asList(restTemplate.exchange(url, HttpMethod.GET, null, AppointmentDto[].class).getBody()),
                throwable -> {
                    log.warn("db-service unavailable for getAvailableAppointments; returning empty list ({})", throwable.toString());
                    return List.of();
                }
        );
    }

    public List<AppointmentDto> getMyAppointments() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        String url = "http://db-service/api/v1/get/myAppointments?email=" + email;

        return dbServiceClient.call(
                () -> Arrays.asList(restTemplate.exchange(url, HttpMethod.GET, null, AppointmentDto[].class).getBody()),
                throwable -> {
                    log.warn("db-service unavailable for getMyAppointments; returning empty list ({})", throwable.toString());
                    return List.of();
                }
        );
    }
    public List<AppointmentDto> getOwnAppointments() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        String url = "http://db-service/api/v1/get/ownAppointments?email=" + email;
        return dbServiceClient.call(
                () -> Arrays.asList(restTemplate.exchange(url, HttpMethod.GET, null, AppointmentDto[].class).getBody()),
                throwable -> {
                    log.warn("db-service unavailable for getOwnAppointments; returning empty list ({})", throwable.toString());
                    return List.of();
                }
        );
    }
    public void deleteDoctorFromAppointment(DeleteAppointmentDto appointmentId) {
        HttpHeaders headersToken = new HttpHeaders();
        headersToken.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Integer> request_api_token = new HttpEntity<>(appointmentId.getAppointmentId(), headersToken);

        String url_token = "http://db-service/api/v1/delete/doctorFromAppointment?appointmentId=" + appointmentId.getAppointmentId();

        dbServiceClient.call(
                () -> restTemplate.exchange(url_token, HttpMethod.PUT, request_api_token, Void.class),
                throwable -> { throw new RemoteServiceUnavailableException("db-service", throwable); }
        );
    }

    public void deleteAppointment(DeleteAppointmentDto dto) {
        HttpHeaders headersToken = new HttpHeaders();
        headersToken.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Integer> request_api_token = new HttpEntity<>(dto.getAppointmentId(), headersToken);

        String url = "http://db-service/api/v1/delete/appointment?appointmentId=" + dto.getAppointmentId();

        dbServiceClient.call(
                () -> restTemplate.exchange(url, HttpMethod.DELETE, null, Void.class),
                throwable -> { throw new RemoteServiceUnavailableException("db-service", throwable); }
        );
    }
}
