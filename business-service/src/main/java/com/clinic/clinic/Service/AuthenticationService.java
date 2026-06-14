package com.clinic.clinic.Service;

import com.clinic.clinic.Entity.User.*;
import com.clinic.clinic.JpaRepo.UserJpaRepo;
import com.clinic.clinic.Entity.User.AuthenticationResponse;
import com.clinic.clinic.email.EmailService;
import com.clinic.clinic.email.EmailTemplateName;
import com.clinic.clinic.JpaRepo.TokenJpaRepo;
import com.clinic.clinic.role.RoleJpaRepo;
import com.clinic.clinic.security.JwtService;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {


    private final PasswordEncoder passwordEncoder;

    private final RoleJpaRepo roleJpaRepo;
    private final UserJpaRepo userJpaRepo;
//    private final DoctorJpaRepo doctorJpaRepo;
    private final TokenJpaRepo tokenJpaRepo;


    private final EmailService emailService;
//    private final TokenDoctorJpaRepo tokenDoctorJpaRepo;
    @Value("${application.mailing.frontend.activation-url}")
    private String activationUrl;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;


    public void register(RegistrationRequestDto request) throws MessagingException {
        if(request.getSpecialization() == null) {
            log.info("Registering new USER account for {}", request.getEmail());
            var userRole = roleJpaRepo.findByName("USER")
                    .orElseThrow(() -> new IllegalArgumentException("Role not found"));
            var user = User.builder()
                    .firstName(request.getFirstname())
                    .lastName(request.getLastname())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .accountLocked(false)
                    .enabled(false)
                    .roles(List.of(userRole))
                    .build();
            userJpaRepo.save(user);
            sendValidationEmail(user);
        } else {
            log.info("Registering new DOCTOR account for {}", request.getEmail());
            var doctorRole = roleJpaRepo.findByName("DOCTOR")
                    .orElseThrow(() -> new IllegalArgumentException("Role not found"));
            var doctor = User.builder()
                    .firstName(request.getFirstname())
                    .lastName(request.getLastname())
                    .email(request.getEmail())
                    .specialization(Specializations.valueOf(String.valueOf(request.getSpecialization())))
                    .password(passwordEncoder.encode(request.getPassword()))
                    .accountLocked(false)
                    .enabled(false)
                    .roles(List.of(doctorRole))
                    .build();
            userJpaRepo.save(doctor);
            sendValidationEmail(doctor);
        }
    }

//    public void registerDoctor(DoctorRegistrationRequest request) throws MessagingException {
//        var doctorRole = roleJpaRepo.findByName("DOCTOR")
//                .orElseThrow(() -> new IllegalArgumentException("Role not found"));
//        var doctor = DoctorEntity.builder()
//                .firstName(request.getFirstname())
//                .lastName(request.getLastname())
//                .email(request.getEmail())
//                .specialization(request.getSpecialization())
//                .password(passwordEncoder.encode(request.getPassword()))
//                .accountLocked(false)
//                .enabled(false)
//                .roles(List.of(doctorRole))
//                .build();
//        doctorJpaRepo.save(doctor);
//        sendValidationEmail(doctor);
//    }

    private void sendValidationEmail(User userEntity) throws MessagingException {
//        if(user instanceof DoctorEntity doctor) {
//            var newToken = generateAndSaveAuthenticationTokenDoctor(doctor);
//            emailService.sendEmail(
//                    doctor.getEmail(),
//                    doctor.getName(),
//                    "Account activation",
//                    EmailTemplateName.ACTIVATE_DOCTOR,
//                    activationUrl,
//                    newToken
//            );
//        }
  //      else if(user instanceof User userEntity) {
            var newToken = generateAndSaveAuthenticationToken(userEntity);
            emailService.sendEmail(
                    userEntity.getEmail(),
                    userEntity.getFullName(),
                    "Account activation",
                    EmailTemplateName.ACTIVATE_ACCOUNT,
                    activationUrl,
                    newToken
            );
    //    }

    }
//    private String generateAndSaveAuthenticationTokenDoctor(DoctorEntity user) {
//        String generatedToken = generateActivationCode(6);
//        var token = TokenDoctor.builder()
//                .token(generatedToken)
//                .createdAt(LocalDateTime.now())
//                .expiresAt(LocalDateTime.now().plusMinutes(30))
//                .doctor(user)
//                .build();
//        tokenDoctorJpaRepo.save(token);
//        return generatedToken;
//    }

    private String generateAndSaveAuthenticationToken(User user) {
        String generatedToken = generateActivationCode(6);
        var token = Token.builder()
                .token(generatedToken)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .user(user)
                .build();
        tokenJpaRepo.save(token);
        return generatedToken;
    }
    private String generateActivationCode(int length) {
        String characters = "0123456789";
        StringBuilder result = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < length; i++) {
            result.append(characters.charAt(random.nextInt(characters.length())));
        }
        return result.toString();
    }
    public AuthenticationResponse authenticate(AuthenticationRequestDto request) throws MessagingException {
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var claims = new HashMap<String, Object>();
        var user = ((User)auth.getPrincipal());
        claims.put("fullName", user.getFullName());
        var jwtToken = jwtService.generateToken(claims, user);
        return AuthenticationResponse.builder().token(jwtToken).build();
    }

//    public AuthenticationResponse authenticateDoctor(AuthenticationRequestDto request) throws MessagingException {
//        var auth = authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(
//                        request.getEmail(),
//                        request.getPassword()
//                )
//        );
//        var claims = new HashMap<String, Object>();
//        var doctor = ((DoctorEntity)auth.getPrincipal());
//        claims.put("fullName", doctor.getFullName());
//        var jwtToken = jwtService.generateToken(claims, doctor);
//        return AuthenticationResponse.builder().token(jwtToken).build();
//    }

    @Transactional
    public void activateAccount(String token) throws MessagingException {
        Token savedToken = tokenJpaRepo.findByToken(token).orElseThrow(
                () -> new RuntimeException("Invalid token")
        );
        if(LocalDateTime.now().isAfter(savedToken.getExpiresAt())) {
            sendValidationEmail(savedToken.getUser());
            throw new RuntimeException("Token expired. New mail was sent.");
        }
        var user = userJpaRepo.findById(savedToken.getUser().getId()).orElseThrow(
                () -> new UsernameNotFoundException("User not found")
        );
        user.setEnabled(true);
        userJpaRepo.save(user);
        savedToken.setValidatedAt(LocalDateTime.now());
        tokenJpaRepo.save(savedToken);
    }

//    @Transactional
//    public void activateDoctorAccount(String token) throws MessagingException {
//        TokenDoctor savedDoctorToken = tokenDoctorJpaRepo.findByToken(token).orElseThrow(
//                () -> new RuntimeException("Invalid token")
//        );
//        if(LocalDateTime.now().isAfter(savedDoctorToken.getExpiresAt())) {
//            sendValidationEmail(savedDoctorToken.getDoctor());
//            throw new RuntimeException("Token expired. New mail was sent.");
//        }
//        var doctor = doctorJpaRepo.findById(savedDoctorToken.getDoctor().getId()).orElseThrow(
//                () -> new UsernameNotFoundException("User not found")
//        );
//        doctor.setEnabled(true);
//        doctorJpaRepo.save(doctor);
//        savedDoctorToken.setValidatedAt(LocalDateTime.now());
//        tokenDoctorJpaRepo.save(savedDoctorToken);
//    }

    @Transactional
    public void deleteAccount(Integer userId) throws MessagingException {
        userJpaRepo.deleteById(userId);
    }
}
