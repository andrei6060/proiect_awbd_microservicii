package com.clinic.clinic.e2e;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies method-level authorization on a DOCTOR-only endpoint:
 *  - no authentication      -> 401 (RestAuthenticationEntryPoint)
 *  - authenticated USER      -> 403 (RestAccessDeniedHandler / @PreAuthorize denies)
 *  - authenticated DOCTOR    -> 200
 * This exercises the security layer rather than bypassing it.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "application.security.jwt.secret-key=0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
        "application.security.jwt.expiration=86400000",
        "application.security.jwt.refresh-token.expiration=604800000",
        "application.mailing.frontend.activation-url=http://localhost:4200/activate-account"
})
class AuthorizationSecurityTest {

    private static final String DOCTOR_ONLY_ENDPOINT = "/medication/getAllAvailableMedicine";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JavaMailSender javaMailSender;

    @Test
    void unauthenticated_isUnauthorized() throws Exception {
        mockMvc.perform(get(DOCTOR_ONLY_ENDPOINT))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "USER")
    void userCallingDoctorEndpoint_isForbidden() throws Exception {
        mockMvc.perform(get(DOCTOR_ONLY_ENDPOINT))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "DOCTOR")
    void doctorCallingDoctorEndpoint_isOk() throws Exception {
        mockMvc.perform(get(DOCTOR_ONLY_ENDPOINT))
                .andExpect(status().isOk());
    }
}
