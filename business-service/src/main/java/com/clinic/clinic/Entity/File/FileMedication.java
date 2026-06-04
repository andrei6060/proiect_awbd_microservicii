package com.clinic.clinic.Entity.File;

import com.clinic.clinic.Entity.Medication.MedicationEntity;
import jakarta.persistence.*;

@Entity
public class FileMedication {

    @EmbeddedId
    private FileMedicationId id = new FileMedicationId();

    @ManyToOne
    @MapsId("fileId")
    @JoinColumn(name = "file_id")
    private FileEntity file;

    @ManyToOne
    @MapsId("medicationId")
    @JoinColumn(name = "medication_id")
    private MedicationEntity medication;

    private Integer quantity;

    public FileMedication() {}

    public FileMedication(FileEntity file, MedicationEntity medication, Integer quantity) {
        this.file = file;
        this.medication = medication;
        this.quantity = quantity;
        this.id = new FileMedicationId(file.getId(), medication.getId());
    }

    public FileMedicationId getId() {
        return id;
    }

    public void setId(FileMedicationId id) {
        this.id = id;
    }

    public FileEntity getFile() {
        return file;
    }

    public void setFile(FileEntity file) {
        this.file = file;
        if (this.id == null) this.id = new FileMedicationId();
        this.id.setFileId(file.getId());
    }

    public MedicationEntity getMedication() {
        return medication;
    }

    public void setMedication(MedicationEntity medication) {
        this.medication = medication;
        if (this.id == null) this.id = new FileMedicationId();
        this.id.setMedicationId(medication.getId());
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
