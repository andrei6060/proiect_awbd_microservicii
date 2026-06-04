package com.clinic.clinic.auth;



import com.clinic.clinic.Entity.User.*;
import com.clinic.clinic.JpaRepo.TokenJpaRepo;
import com.clinic.clinic.JpaRepo.UserJpaRepo;
import com.clinic.clinic.Service.AuthenticationService;
import com.clinic.clinic.email.EmailService;
import com.clinic.clinic.role.Role;
import com.clinic.clinic.role.RoleJpaRepo;
import com.clinic.clinic.security.JwtService;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleJpaRepo roleJpaRepo;

    @Mock
    private UserJpaRepo userJpaRepo;

    @Mock
    private TokenJpaRepo tokenJpaRepo;

    @Mock
    private EmailService emailService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthenticationService authenticationService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(
                authenticationService,
                "activationUrl",
                "http://localhost:8080/activate"
        );
    }

//    @Test
//    void register_shouldRegisterNormalUser() throws MessagingException {
//        RegistrationRequestDto request = new RegistrationRequestDto();
//        request.setFirstname("John");
//        request.setLastname("Doe");
//        request.setEmail("john@test.com");
//        request.setPassword("pass");
//        request.setSpecialization(null);
//
//        Role role = Role.builder()
//                .name("USER")
//                .build();
//
//        when(roleJpaRepo.findByName("USER"))
//                .thenReturn(Optional.of(role));
//
//        when(passwordEncoder.encode("pass"))
//                .thenReturn("encoded");
//
//        authenticationService.register(request);
//
//        verify(userJpaRepo).save(any(User.class));
//        verify(tokenJpaRepo).save(any(Token.class));
//        verify(emailService).sendEmail(
//                eq("john@test.com"),
//                eq("John Doe"),
//                eq("Account activation"),
//                any(),
//                anyString(),
//                anyString()
//        );
//    }

//    @Test
//    void register_shouldRegisterDoctor() throws MessagingException {
//        RegistrationRequestDto request = new RegistrationRequestDto();
//        request.setFirstname("Gregory");
//        request.setLastname("House");
//        request.setEmail("house@test.com");
//        request.setPassword("pass");
//        request.setSpecialization(Specializations.CARDIOLOGIE);
//
//        Role role = Role.builder()
//                .name("DOCTOR")
//                .build();
//
//        when(roleJpaRepo.findByName("DOCTOR"))
//                .thenReturn(Optional.of(role));
//
//        when(passwordEncoder.encode("pass"))
//                .thenReturn("encoded");
//
//        authenticationService.register(request);
//
//        verify(userJpaRepo).save(any(User.class));
//        verify(tokenJpaRepo).save(any(Token.class));
//        verify(emailService).sendEmail(
//                eq("house@test.com"),
//                eq("Gregory House"),
//                eq("Account activation"),
//                any(),
//                anyString(),
//                anyString()
//        );
//    }

    @Test
    void authenticate_shouldReturnJwtToken() throws MessagingException {
        AuthenticationRequestDto request = new AuthenticationRequestDto();
        request.setEmail("test@test.com");
        request.setPassword("pass");

        User user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("test@test.com")
                .build();

        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(
                UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(authentication.getPrincipal()).thenReturn(user);

        when(jwtService.generateToken(anyMap(), eq(user)))
                .thenReturn("jwt-token");

        AuthenticationResponse response =
                authenticationService.authenticate(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
    }

//    @Test
//    void activateAccount_shouldEnableUser() throws MessagingException {
//        User user = User.builder()
//                .id(1)
//                .enabled(false)
//                .build();
//
//        Token token = Token.builder()
//                .token("123456")
//                .user(user)
//                .expiresAt(LocalDateTime.now().plusMinutes(10))
//                .build();
//
//        when(tokenJpaRepo.findByToken("123456"))
//                .thenReturn(Optional.of(token));
//
//        when(userJpaRepo.findById(1))
//                .thenReturn(Optional.of(user));
//
//        authenticationService.activateAccount("123456");
//
//        assertTrue(user.isEnabled());
//
//        verify(userJpaRepo).save(user);
//        verify(tokenJpaRepo).save(token);
//    }

    @Test
    void activateAccount_shouldThrowWhenTokenExpired() {
        User user = User.builder()
                .id(1)
                .email("test@test.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        Token token = Token.builder()
                .token("123456")
                .user(user)
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .build();

        when(tokenJpaRepo.findByToken("123456"))
                .thenReturn(Optional.of(token));

        assertThrows(RuntimeException.class,
                () -> authenticationService.activateAccount("123456"));
    }

    @Test
    void deleteAccount_shouldDeleteUser() throws MessagingException {
        authenticationService.deleteAccount(1);

        verify(userJpaRepo).deleteById(1);
    }
}
