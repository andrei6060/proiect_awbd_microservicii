package com.clinic.clinic.Service;

import com.clinic.clinic.Entity.Appointment.AppointmentBDTO;
import com.clinic.clinic.Entity.Appointment.AppointmentEntity;
import com.clinic.clinic.Entity.Review.ReviewEntity;
import com.clinic.clinic.Entity.Review.TransferReviewDTO;
import com.clinic.clinic.Entity.User.*;
import com.clinic.clinic.JpaRepo.*;
import com.clinic.clinic.email.EmailService;
import com.clinic.clinic.email.EmailTemplateName;
import com.clinic.clinic.role.Role;
import com.clinic.clinic.role.RoleJpaRepo;
import com.clinic.clinic.security.JwtService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SaveService {
    private final UserJpaRepo userJpaRepo;
    private final ReviewJpaRepo reviewJpaRepo;
    private final FileJpaRepo fileJpaRepo;
    private final RoleJpaRepo roleJpaRepo;
    private final EmailService emailService;
    private final TokenJpaRepo tokenJpaRepo;
    private final AppointmentJpaRepo appointmentJpaRepo;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Value("${application.mailing.frontend.activation-url}")
    private String activationUrl;

    public void saveToken(TokenDTO dto) {
        User user = userJpaRepo.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + dto.getUserId()));

        Token tokenEntity = tokenJpaRepo.findByToken(dto.getToken())
                .orElseThrow(() -> new RuntimeException("Token not found: " + dto.getToken()));


        // 🔁 Actualizează doar câmpurile necesare
//        tokenEntity.setValidatedAt(dto.getValidatedAt());
//
//        tokenJpaRepo.save(tokenEntity); // Hibernate face UPDATE, nu INSERT
    }


    public void saveUser(UserDTO user) {
        List<Role> roleEntities = user.getRoles().stream()
                .map(roleName -> roleJpaRepo.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleName)))
                .toList();
        User userentity = userJpaRepo.findByEmail(user.getEmail()).get();
        Integer id = userJpaRepo.findByEmail(user.getEmail()).get().getId();
        var userEntity = User.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .password(user.getPassword())
                .email(user.getEmail())
                .accountLocked(user.isAccountLocked())
                .enabled(user.isEnabled())
                .roles(roleEntities)
                .id(id)
                .tokens(userentity.getTokens())
                .build();
        userJpaRepo.save(userEntity);
    }

//    public void saveUserAuth(UserDTO user) throws MessagingException {
//        List<Role> roleEntities = user.getRoles().stream()
//                .map(roleName -> roleJpaRepo.findByName(roleName)
//                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleName)))
//                .toList();
//        var userEntity = User.builder()
//                .firstName(user.getFirstName())
//                .lastName(user.getLastName())
//                .password(user.getPassword())
//                .email(user.getEmail())
//                .accountLocked(user.isAccountLocked())
//                .enabled(user.isEnabled())
//                .roles(roleEntities)
//                .build();
//        userJpaRepo.save(userEntity);
//        sendValidationEmail(userEntity);
//    }

    private void sendValidationEmail(User userEntity) throws MessagingException {
        var newToken = generateAndSaveAuthenticationToken(userEntity);
        emailService.sendEmail(
                userEntity.getEmail(),
                userEntity.getFullName(),
                "Account activation",
                EmailTemplateName.ACTIVATE_ACCOUNT,
                activationUrl,
                newToken
        );
    }

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
    public void saveUserAuth(UserDTO user) throws MessagingException {
        List<Role> roleEntities = user.getRoles().stream()
                .map(roleName -> roleJpaRepo.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleName)))
                .toList();
        var userEntity = User.builder()
                .specialization(user.getSpecialization()!= null ? user.getSpecialization() : null)
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .password(user.getPassword())
                .email(user.getEmail())
                .accountLocked(user.isAccountLocked())
                .enabled(user.isEnabled())
                .roles(roleEntities)
                .build();
        userJpaRepo.save(userEntity);
        sendValidationEmail(userEntity);
    }

    public void saveAppointment(AppointmentBDTO appointmentBDTO) {
        var appointmentEntity = AppointmentEntity.builder()
                .patientId(userJpaRepo.findById(appointmentBDTO.getPacientId()).get())
                .doctorId(appointmentBDTO.getDoctorId()!= null ? userJpaRepo.findById(appointmentBDTO.getDoctorId()).get() : null)
                .date(appointmentBDTO.getAppointmentDate())
                .neededSpecialization(appointmentBDTO.getSpecialization())
                .build();
        appointmentJpaRepo.save(appointmentEntity);
    }

    public void SaveAcceptAppointment(AppointmentBDTO appointmentBDTO){
        var appointmentEntity = AppointmentEntity.builder()
                .patientId(userJpaRepo.findById(appointmentBDTO.getPacientId()).get())
                .doctorId(appointmentBDTO.getDoctorId()!= null ? userJpaRepo.findById(appointmentBDTO.getDoctorId()).get() : null)
                .date(appointmentBDTO.getAppointmentDate())
                .id(appointmentBDTO.getAppointmentId())
                .neededSpecialization(appointmentBDTO.getSpecialization())
                .build();
        appointmentJpaRepo.save(appointmentEntity);
    }

    public void saveReview(TransferReviewDTO review) {
        var reviewEntity = ReviewEntity.builder()
                .doctorId(userJpaRepo.findById(review.getDoctorId()).get())
                .patientId(userJpaRepo.findById(review.getPatientId()).get())
                .anonymousReview(review.getAnonymousReview())
                .aspect(review.getAspect())
                .rating(review.getRating())
                .comment(review.getReview())
                .build();
        reviewJpaRepo.save(reviewEntity);
    }
}
