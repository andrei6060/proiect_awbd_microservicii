package com.clinic.clinic.Controller;


import com.clinic.clinic.Entity.Medication.*;
import com.clinic.clinic.Service.MedicationService;
import com.clinic.clinic.Entity.Review.ReviewDto;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("medication")
@RequiredArgsConstructor
public class MedicationController {
    private final MedicationService medicationService;

    //bunx2
    @PostMapping("/addNewMedication")
    @PreAuthorize("hasAnyAuthority('DOCTOR')")
    public ResponseEntity<?> addNewMedication(@Valid @RequestBody AddMedicationDto addMedicationDto) throws MessagingException {
        medicationService.addNewMedication(addMedicationDto);
        return ResponseEntity.ok().build();
    }

    //bunx2
    @PutMapping("/supplyMedication")
    @PreAuthorize("hasAnyAuthority('DOCTOR')")
    public ResponseEntity<List<ReviewDto>> supplyMedication(@Valid @RequestBody SupplyMedicationDto supplyMedicationDto) throws MessagingException {
        medicationService.supplyMedication(supplyMedicationDto);
        return ResponseEntity.ok().build();
    }

    //bunx2
    @DeleteMapping("/discontinueMedication")
    @PreAuthorize("hasAnyAuthority('DOCTOR')")
    public ResponseEntity<List<ReviewDto>> discontinueMedication(@Valid @RequestBody DiscontinueMedicationDto discontinueMedicationDto) throws MessagingException {
        medicationService.discontinueMedication(discontinueMedicationDto);
        return ResponseEntity.ok().build();
    }

    //bunx2
    @PutMapping("/reactivateMedication")
    @PreAuthorize("hasAnyAuthority('DOCTOR')")
    public ResponseEntity<List<ReviewDto>> reactivateMedication(@Valid @RequestBody DiscontinueMedicationDto discontinueMedicationDto) throws MessagingException {
        medicationService.reactivateMedication(discontinueMedicationDto);
        return ResponseEntity.ok().build();
    }

    //bun
    @PutMapping("/giveMedication")
    @PreAuthorize("hasAnyAuthority('DOCTOR')")
    public ResponseEntity<?> giveMedication(@Valid @RequestBody GiveMedicationDto giveMedicationDto){
        medicationService.giveMedication(giveMedicationDto);
        return ResponseEntity.ok().build();
    }

    //bun
    @GetMapping("/getMedicine")
    @PreAuthorize("hasAuthority('DOCTOR')")
    public ResponseEntity<?> getMedicine(@Valid @RequestBody GetMedicineDto getMedicineDto){

        return ResponseEntity.ok(medicationService.getMedicine(getMedicineDto));
    }

    @GetMapping("/getAllAvailableMedicine")
    @PreAuthorize("hasAuthority('DOCTOR')")
    public ResponseEntity<?> getAllAvailableMedicine(){
        return ResponseEntity.ok(medicationService.getAllAvailableMedicine());
    }

        @GetMapping("/getAllActiveMedicine")
    @PreAuthorize("hasAuthority('DOCTOR')")
    public ResponseEntity<?> getAllActiveMedicine(){
        return ResponseEntity.ok(medicationService.getAllActiveMedicine());
    }



}
