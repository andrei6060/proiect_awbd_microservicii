package com.clinic.clinic.medication;


import com.clinic.clinic.Controller.MedicationController;
import com.clinic.clinic.Entity.Medication.*;
import com.clinic.clinic.Service.MedicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class MedicationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MedicationService medicationService;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new MedicationController(medicationService)).build();
    }

    @Test
    void addNewMedication_shouldReturnOk() throws Exception {
        mockMvc.perform(post("/medication/addNewMedication")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Paracetamol",
                                  "description": "Pain relief medication",
                                  "quantity": 20
                                }
                                """))
                .andExpect(status().isOk());

        verify(medicationService).addNewMedication(any(AddMedicationDto.class));
    }

    @Test
    void supplyMedication_shouldReturnOk() throws Exception {
        mockMvc.perform(put("/medication/supplyMedication")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "medicationName": "Paracetamol",
                                  "quantity": 10
                                }
                                """))
                .andExpect(status().isOk());

        verify(medicationService).supplyMedication(any(SupplyMedicationDto.class));
    }

    @Test
    void discontinueMedication_shouldReturnOk() throws Exception {
        mockMvc.perform(delete("/medication/discontinueMedication")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Paracetamol"
                                }
                                """))
                .andExpect(status().isOk());

        verify(medicationService).discontinueMedication(any(DiscontinueMedicationDto.class));
    }

    @Test
    void reactivateMedication_shouldReturnOk() throws Exception {
        mockMvc.perform(put("/medication/reactivateMedication")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Paracetamol"
                                }
                                """))
                .andExpect(status().isOk());

        verify(medicationService).reactivateMedication(any(DiscontinueMedicationDto.class));
    }

    @Test
    void giveMedication_shouldReturnOk() throws Exception {
        mockMvc.perform(put("/medication/giveMedication")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "idPatient": 1,
                                  "medicationName": "Paracetamol",
                                  "quantity": 2
                                }
                                """))
                .andExpect(status().isOk());

        verify(medicationService).giveMedication(any(GiveMedicationDto.class));
    }

    @Test
    void getMedicine_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/medication/getMedicine")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Paracetamol"
                                }
                                """))
                .andExpect(status().isOk());

        verify(medicationService).getMedicine(any(GetMedicineDto.class));
    }

    @Test
    void getAllAvailableMedicine_shouldReturnOk() throws Exception {
        when(medicationService.getAllAvailableMedicine()).thenReturn(List.of());

        mockMvc.perform(get("/medication/getAllAvailableMedicine"))
                .andExpect(status().isOk());

        verify(medicationService).getAllAvailableMedicine();
    }

    @Test
    void getAllActiveMedicine_shouldReturnOk() throws Exception {
        when(medicationService.getAllActiveMedicine()).thenReturn(List.of());

        mockMvc.perform(get("/medication/getAllActiveMedicine"))
                .andExpect(status().isOk());

        verify(medicationService).getAllActiveMedicine();
    }
}