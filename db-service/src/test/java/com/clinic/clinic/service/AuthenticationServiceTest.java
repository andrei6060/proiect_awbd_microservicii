package com.clinic.clinic.service;

import com.clinic.clinic.Entity.User.*;
import com.clinic.clinic.JpaRepo.TokenJpaRepo;
import com.clinic.clinic.JpaRepo.UserJpaRepo;
import com.clinic.clinic.Service.AuthenticationService;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock private PasswordEncoder passwordEncoder;
    @Mock private RoleJpaRepo roleJpaRepo;
    @Mock private UserJpaRepo userJpaRepo;
    @Mock private TokenJpaRepo tokenJpaRepo;
    @Mock private EmailService emailService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void register_whenNoSpecialization_shouldCreateUserAndSendEmail() throws MessagingException {
        RegistrationRequestDto request = RegistrationRequestDto.builder()
                .firstname("F").lastname("L").email("user@test.com").password("password12").build();
        when(roleJpaRepo.findByName("USER")).thenReturn(Optional.of(Role.builder().name("USER").build()));
        when(passwordEncoder.encode("password12")).thenReturn("enc");

        authenticationService.register(request);

        verify(userJpaRepo).save(any(User.class));
        verify(emailService).sendEmail(any(), any(), any(), any(), any(), any());
    }

    @Test
    void register_whenSpecialization_shouldCreateDoctorAndSendEmail() throws MessagingException {
        RegistrationRequestDto request = RegistrationRequestDto.builder()
                .firstname("F").lastname("L").email("doc@test.com").password("password12")
                .specialization(Specializations.CARDIOLOGIE).build();
        when(roleJpaRepo.findByName("DOCTOR")).thenReturn(Optional.of(Role.builder().name("DOCTOR").build()));
        when(passwordEncoder.encode("password12")).thenReturn("enc");

        authenticationService.register(request);

        verify(userJpaRepo).save(any(User.class));
        verify(emailService).sendEmail(any(), any(), any(), any(), any(), any());
    }

    @Test
    void authenticate_shouldReturnToken() throws MessagingException {
        User principal = User.builder().id(1).email("1@test.com").firstName("F").lastName("L").build();
        AuthenticationRequestDto request = AuthenticationRequestDto.builder()
                .email("1@test.com").password("password12").build();
        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList()));
        when(jwtService.generateToken(any(), any())).thenReturn("jwt-token");

        AuthenticationResponse response = authenticationService.authenticate(request);

        assertEquals("jwt-token", response.getToken());
    }

    @Test
    void deleteAccount_shouldDelegateToRepo() throws MessagingException {
        authenticationService.deleteAccount(7);

        verify(userJpaRepo).deleteById(7);
    }
}
