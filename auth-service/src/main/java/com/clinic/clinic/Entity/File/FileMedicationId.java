package com.clinic.clinic.Entity.File;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileMedicationId implements Serializable {

    private Integer fileId;
    private Integer medicationId;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileMedicationId that)) return false;
        return Objects.equals(fileId, that.fileId) &&
                Objects.equals(medicationId, that.medicationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileId, medicationId);
    }
}

