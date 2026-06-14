package com.clinic.clinic.e2e;

import com.clinic.clinic.Entity.Medication.MedicationEntity;
import com.clinic.clinic.JpaRepo.MedicationJpaRepo;
import org.junit.jupiter.api.BeforeEach;
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

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end scenarios exercising the full web -> service -> repository stack
 * against the in-memory H2 database (test profile). Security is exercised, not
 * bypassed: protected endpoints require the DOCTOR authority via @WithMockUser.
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
class MedicationEndToEndTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MedicationJpaRepo medicationJpaRepo;

    @MockitoBean
    private JavaMailSender javaMailSender;

    @BeforeEach
    void cleanDatabase() {
        medicationJpaRepo.deleteAll();
    }

    private void seed(String name, int quantity) {
        medicationJpaRepo.save(MedicationEntity.builder()
                .name(name).description(name + " desc").quantity(quantity).active(true).build());
    }

    /** Scenario 1: create a medication through the API, then retrieve it back. */
    @Test
    @WithMockUser(authorities = "DOCTOR")
    void createMedication_thenRetrieveItInAvailableList() throws Exception {
        mockMvc.perform(post("/medication/addNewMedication")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Paracetamol",
                                  "description": "Pain relief",
                                  "quantity": 25
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/medication/getAllAvailableMedicine"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Paracetamol"))
                .andExpect(jsonPath("$[0].quantity").value(25));
    }

    /** Scenario 2: paginated + sorted listing returns correct page metadata and order. */
    @Test
    @WithMockUser(authorities = "DOCTOR")
    void paginatedListing_returnsExpectedPageMetadataAndOrder() throws Exception {
        seed("Aspirina", 10);
        seed("Ibuprofen", 20);
        seed("Paracetamol", 30);

        mockMvc.perform(get("/medication/getAllMedications/page")
                        .param("page", "0")
                        .param("size", "2")
                        .param("sortBy", "name")
                        .param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false))
                .andExpect(jsonPath("$.sortBy").value("name"))
                .andExpect(jsonPath("$.direction").value("asc"))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Aspirina"))
                .andExpect(jsonPath("$.content[1].name").value("Ibuprofen"));
    }

    /** Scenario 3: requesting a non-existent resource returns 404 with the ExceptionResponse shape. */
    @Test
    @WithMockUser(authorities = "DOCTOR")
    void requestingMissingMedication_returns404WithErrorBody() throws Exception {
        mockMvc.perform(get("/medication/getMedicine")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "DoesNotExist"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.path").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
