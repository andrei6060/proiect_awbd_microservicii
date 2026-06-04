package com.clinic.clinic.Controller;

import com.clinic.clinic.Entity.File.AddBloodDto;
import com.clinic.clinic.Entity.File.AddFileDto;
import com.clinic.clinic.Entity.File.GetFileDto;
import com.clinic.clinic.Service.FileService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("file")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    //bunx2
    @PostMapping("/addNewFile")
    @PreAuthorize("hasAnyAuthority('DOCTOR')")
    public ResponseEntity<?> addNewFile(@Valid @RequestBody AddFileDto addFileDto) throws MessagingException {
        fileService.addNewFile(addFileDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/getPatientFile")
    @PreAuthorize("hasAnyAuthority('DOCTOR')")
    public ResponseEntity<?> getPatientFile(@Valid @RequestBody GetFileDto getFileDto) throws MessagingException {
        return ResponseEntity.ok(fileService.getPatientFile(getFileDto));
    }

    @GetMapping("/getOwnFile")
    @PreAuthorize("hasAnyAuthority('USER')")
    public ResponseEntity<?> getOwnFile() throws MessagingException {
        return ResponseEntity.ok(fileService.getOwnFile());
    }

    @GetMapping("/getAllFiles")
    @PreAuthorize("hasAnyAuthority('DOCTOR')")
    public ResponseEntity<?> getAllFiles() throws MessagingException {
        return ResponseEntity.ok(fileService.getAllFiles());
    }

    @PutMapping("/addBloodType")
    @PreAuthorize("hasAnyAuthority('DOCTOR')")
    public ResponseEntity<?> addBloodType(@Valid @RequestBody AddBloodDto addBloodDto) throws MessagingException {
        fileService.addBloodType(addBloodDto);
        return ResponseEntity.ok().build();
    }
}
