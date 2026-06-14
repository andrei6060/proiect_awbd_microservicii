package com.clinic.clinic.Controller;

import com.clinic.clinic.Entity.User.AuthenticationRequestDto;
import com.clinic.clinic.Entity.User.RegistrationRequestDto;
import com.clinic.clinic.Entity.User.AuthenticationResponse;
import com.clinic.clinic.Service.AuthenticationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
@Tag(name = "authentication")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    //DONE
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegistrationRequestDto request) throws MessagingException {
        authenticationService.register(request);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticateUser(@Valid @RequestBody AuthenticationRequestDto request) throws MessagingException {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }


    @GetMapping("/activateUser")
    public void confirm(@RequestParam String token) throws MessagingException {
        authenticationService.activateAccount(token);
    }

    @DeleteMapping("/deleteUser")
    @PreAuthorize("hasAuthority('DOCTOR')")
    public void deleteUser(@RequestParam Integer token) throws MessagingException {
        authenticationService.deleteAccount(token);
    }



}
