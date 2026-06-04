package com.clinic.clinic.Entity.File;

import com.clinic.clinic.Entity.Medication.MedicationEntity;
import com.clinic.clinic.Entity.User.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Entity
@Table(name = "file")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    @Enumerated(EnumType.STRING)
    private BloodType bloodType;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @OneToMany(mappedBy = "file", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FileMedication> medications = new ArrayList<>();


    public void addMedication(MedicationEntity medication, int quantity) {
        FileMedication fileMed = new FileMedication(this, medication, quantity);
        this.medications.add(fileMed);
    }

    public Map<MedicationEntity, Integer> getMedicationMap() {
        return medications.stream()
                .collect(Collectors.toMap(FileMedication::getMedication, FileMedication::getQuantity));
    }
}
