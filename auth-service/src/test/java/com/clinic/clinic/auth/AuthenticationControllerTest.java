package com.clinic.clinic.auth;


import com.clinic.clinic.Controller.AuthenticationController;
import com.clinic.clinic.Entity.User.AuthenticationRequestDto;
import com.clinic.clinic.Entity.User.AuthenticationResponse;
import com.clinic.clinic.Entity.User.RegistrationRequestDto;
import com.clinic.clinic.Entity.User.Specializations;
import com.clinic.clinic.Service.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthenticationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthenticationService authenticationService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        AuthenticationController authenticationController =
                new AuthenticationController(authenticationService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(authenticationController)
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void registerUser_shouldReturnAccepted_forUser() throws Exception {
        RegistrationRequestDto request = RegistrationRequestDto.builder()
                .firstname("Andrei")
                .lastname("Const")
                .email("andrei.const@gmail.com")
                .password("password1234")
                .specialization(null)
                .build();

        doNothing().when(authenticationService).register(any(RegistrationRequestDto.class));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

        verify(authenticationService).register(any(RegistrationRequestDto.class));
    }

    @Test
    void registerUser_shouldReturnAccepted_forDoctor() throws Exception {
        RegistrationRequestDto request = RegistrationRequestDto.builder()
                .firstname("Doctor")
                .lastname("Test")
                .email("doctor@test.com")
                .password("password1234")
                .specialization(Specializations.CARDIOLOGIE)
                .build();

        doNothing().when(authenticationService).register(any(RegistrationRequestDto.class));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

        verify(authenticationService).register(any(RegistrationRequestDto.class));
    }

    @Test
    void authenticateUser_shouldReturnToken() throws Exception {
        AuthenticationRequestDto request = AuthenticationRequestDto.builder()
                .email("andrei.const@gmail.com")
                .password("password1234")
                .build();

        AuthenticationResponse response = AuthenticationResponse.builder()
                .token("jwt-token-test")
                .build();

        when(authenticationService.authenticate(any(AuthenticationRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(post("/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-test"));

        verify(authenticationService).authenticate(any(AuthenticationRequestDto.class));
    }

    @Test
    void activateUser_shouldCallService() throws Exception {
        doNothing().when(authenticationService).activateAccount("123456");

        mockMvc.perform(get("/auth/activateUser")
                        .param("token", "123456"))
                .andExpect(status().isOk());

        verify(authenticationService).activateAccount("123456");
    }

    @Test
    void deleteUser_shouldCallService() throws Exception {
        doNothing().when(authenticationService).deleteAccount(1);

        mockMvc.perform(delete("/auth/deleteUser")
                        .param("token", "1"))
                .andExpect(status().isOk());

        verify(authenticationService).deleteAccount(1);
    }

    @Test
    void registerUser_shouldReturnBadRequest_whenInvalidBody() throws Exception {
        RegistrationRequestDto request = RegistrationRequestDto.builder()
                .firstname("")
                .lastname("")
                .email("invalid-email")
                .password("123")
                .build();

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).register(any());
    }

    @Test
    void authenticateUser_shouldReturnBadRequest_whenInvalidBody() throws Exception {
        AuthenticationRequestDto request = AuthenticationRequestDto.builder()
                .email("")
                .password("")
                .build();

        mockMvc.perform(post("/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).authenticate(any());
    }
}
