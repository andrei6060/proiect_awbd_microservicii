package com.clinic.clinic.file;



import com.clinic.clinic.Controller.FileController;
import com.clinic.clinic.Entity.File.*;
import com.clinic.clinic.Service.FileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FileControllerTests {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private FileService fileService;

    @InjectMocks
    private FileController fileController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(fileController).build();
    }

    @Test
    void addNewFile_shouldReturnOk() throws Exception {
        AddFileDto dto = AddFileDto.builder()
                .patientId(1)
                .bloodType(BloodType.A_POSITIVE)
                .build();

        mockMvc.perform(post("/file/addNewFile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(fileService).addNewFile(any(AddFileDto.class));
    }

    @Test
    void getPatientFile_shouldReturnFile() throws Exception {
        GetFileDto dto = GetFileDto.builder()
                .patientId(1)
                .build();

        FindFileDto response = FindFileDto.builder()
                .patientId(1)
                .bloodType(BloodType.A_POSITIVE)
                .medications(Map.of("Paracetamol", 2))
                .build();

        when(fileService.getPatientFile(any(GetFileDto.class))).thenReturn(response);

        mockMvc.perform(get("/file/getPatientFile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(1))
                .andExpect(jsonPath("$.bloodType").value("A_POSITIVE"))
                .andExpect(jsonPath("$.medications.Paracetamol").value(2));

        verify(fileService).getPatientFile(any(GetFileDto.class));
    }

    @Test
    void getOwnFile_shouldReturnFile() throws Exception {
        FileEntity file = FileEntity.builder()
                .id(1)
                .bloodType(BloodType.A_POSITIVE)
                .medications(new ArrayList<>())
                .build();

        when(fileService.getOwnFile()).thenReturn(file);

        mockMvc.perform(get("/file/getOwnFile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.bloodType").value("A_POSITIVE"));

        verify(fileService).getOwnFile();
    }

    @Test
    void getAllFiles_shouldReturnFiles() throws Exception {
        FindFileDto file = FindFileDto.builder()
                .patientId(1)
                .bloodType(BloodType.A_POSITIVE)
                .medications(Map.of())
                .build();

        when(fileService.getAllFiles()).thenReturn(List.of(file));

        mockMvc.perform(get("/file/getAllFiles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].patientId").value(1))
                .andExpect(jsonPath("$[0].bloodType").value("A_POSITIVE"));

        verify(fileService).getAllFiles();
    }

    @Test
    void addBloodType_shouldReturnOk() throws Exception {
        AddBloodDto dto = AddBloodDto.builder()
                .patientId(1)
                .bloodType(BloodType.A_POSITIVE)
                .build();

        mockMvc.perform(put("/file/addBloodType")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(fileService).addBloodType(any(AddBloodDto.class));
    }
}
