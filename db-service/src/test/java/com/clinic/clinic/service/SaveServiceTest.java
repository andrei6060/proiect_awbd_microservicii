package com.clinic.clinic.service;

import com.clinic.clinic.Entity.Appointment.AppointmentBDTO;
import com.clinic.clinic.Entity.Appointment.AppointmentEntity;
import com.clinic.clinic.Entity.Review.ReviewEntity;
import com.clinic.clinic.Entity.Review.TransferReviewDTO;
import com.clinic.clinic.Entity.User.*;
import com.clinic.clinic.JpaRepo.*;
import com.clinic.clinic.email.EmailService;
import com.clinic.clinic.role.Role;
import com.clinic.clinic.role.RoleJpaRepo;
import com.clinic.clinic.security.JwtService;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaveServiceTest {

    @Mock private UserJpaRepo userJpaRepo;
    @Mock private ReviewJpaRepo reviewJpaRepo;
    @Mock private FileJpaRepo fileJpaRepo;
    @Mock private RoleJpaRepo roleJpaRepo;
    @Mock private EmailService emailService;
    @Mock private TokenJpaRepo tokenJpaRepo;
    @Mock private AppointmentJpaRepo appointmentJpaRepo;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;

    @InjectMocks
    private com.clinic.clinic.Service.SaveService saveService;

    private User user(int id) {
        return User.builder().id(id).email(id + "@test.com").firstName("F").lastName("L").build();
    }

    @Test
    void saveUser_shouldPersistUser() {
        UserDTO dto = UserDTO.builder()
                .firstName("F").lastName("L").email("1@test.com").password("pw")
                .roles(List.of("USER")).build();
        when(roleJpaRepo.findByName("USER")).thenReturn(Optional.of(Role.builder().name("USER").build()));
        when(userJpaRepo.findByEmail("1@test.com")).thenReturn(Optional.of(user(1)));

        saveService.saveUser(dto);

        verify(userJpaRepo).save(any(User.class));
    }

    @Test
    void saveUserAuth_shouldPersistUserAndSendEmail() throws MessagingException {
        UserDTO dto = UserDTO.builder()
                .firstName("F").lastName("L").email("doc@test.com").password("pw")
                .specialization(Specializations.CARDIOLOGIE).roles(List.of("DOCTOR")).build();
        when(roleJpaRepo.findByName("DOCTOR")).thenReturn(Optional.of(Role.builder().name("DOCTOR").build()));

        saveService.saveUserAuth(dto);

        verify(userJpaRepo).save(any(User.class));
        verify(emailService).sendEmail(any(), any(), any(), any(), any(), any());
    }

    @Test
    void saveAppointment_shouldPersist() {
        AppointmentBDTO dto = AppointmentBDTO.builder()
                .pacientId(1).specialization("CARDIOLOGIE").appointmentDate(LocalDateTime.now()).build();
        when(userJpaRepo.findById(1)).thenReturn(Optional.of(user(1)));

        saveService.saveAppointment(dto);

        verify(appointmentJpaRepo).save(any(AppointmentEntity.class));
    }

    @Test
    void saveAcceptAppointment_shouldPersistWithDoctor() {
        AppointmentBDTO dto = AppointmentBDTO.builder()
                .pacientId(1).doctorId(2).appointmentId(5L)
                .specialization("CARDIOLOGIE").appointmentDate(LocalDateTime.now()).build();
        when(userJpaRepo.findById(1)).thenReturn(Optional.of(user(1)));
        when(userJpaRepo.findById(2)).thenReturn(Optional.of(user(2)));

        saveService.SaveAcceptAppointment(dto);

        verify(appointmentJpaRepo).save(any(AppointmentEntity.class));
    }

    @Test
    void saveReview_shouldPersist() {
        TransferReviewDTO dto = TransferReviewDTO.builder()
                .doctorId(2).patientId(1).anonymousReview(false).rating(5).aspect("kindness").review("great").build();
        when(userJpaRepo.findById(2)).thenReturn(Optional.of(user(2)));
        when(userJpaRepo.findById(1)).thenReturn(Optional.of(user(1)));

        saveService.saveReview(dto);

        verify(reviewJpaRepo).save(any(ReviewEntity.class));
    }

    @Test
    void saveToken_whenUserAndTokenExist_shouldNotThrow() {
        TokenDTO dto = TokenDTO.builder().userId(1).token("abc").build();
        when(userJpaRepo.findById(1)).thenReturn(Optional.of(user(1)));
        when(tokenJpaRepo.findByToken("abc")).thenReturn(Optional.of(Token.builder().token("abc").build()));

        assertDoesNotThrow(() -> saveService.saveToken(dto));
        verify(tokenJpaRepo).findByToken("abc");
    }

    @Test
    void saveToken_whenUserNotFound_shouldThrow() {
        TokenDTO dto = TokenDTO.builder().userId(99).token("abc").build();
        when(userJpaRepo.findById(99)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> saveService.saveToken(dto));
    }

    @Test
    void saveToken_whenTokenNotFound_shouldThrow() {
        TokenDTO dto = TokenDTO.builder().userId(1).token("ghost").build();
        when(userJpaRepo.findById(1)).thenReturn(Optional.of(user(1)));
        when(tokenJpaRepo.findByToken("ghost")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> saveService.saveToken(dto));
    }

    @Test
    void authenticate_shouldReturnToken() throws MessagingException {
        User principal = user(1);
        AuthenticationRequestDto request = AuthenticationRequestDto.builder()
                .email("1@test.com").password("password12").build();
        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList()));
        when(jwtService.generateToken(any(), any())).thenReturn("jwt-token");

        AuthenticationResponse response = saveService.authenticate(request);

        assertEquals("jwt-token", response.getToken());
    }
}
