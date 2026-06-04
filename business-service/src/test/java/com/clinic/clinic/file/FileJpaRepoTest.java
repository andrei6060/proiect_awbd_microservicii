package com.clinic.clinic.file;

import com.clinic.clinic.Entity.File.BloodType;
import com.clinic.clinic.Entity.File.FileEntity;
import com.clinic.clinic.Entity.User.User;
import com.clinic.clinic.JpaRepo.FileJpaRepo;
import com.clinic.clinic.JpaRepo.UserJpaRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
class FileJpaRepoTest {

    @Autowired
    private FileJpaRepo fileJpaRepo;

    @Autowired
    private UserJpaRepo userJpaRepo;

    @Test
    void findByUser_shouldReturnFile_whenFileExistsForUser() {
        User user = User.builder()
                .email("patient@test.com")
                .firstName("Andrei")
                .lastName("Test")
                .password("password")
                .enabled(true)
                .specialization(null)
                .build();

        User savedUser = userJpaRepo.save(user);

        FileEntity file = FileEntity.builder()
                .user(savedUser)
                .bloodType(BloodType.A_POSITIVE)
                .build();

        fileJpaRepo.save(file);

        Optional<FileEntity> result = fileJpaRepo.findByUser(savedUser);

        assertTrue(result.isPresent());
        assertEquals(BloodType.A_POSITIVE, result.get().getBloodType());
        assertEquals(savedUser.getId(), result.get().getUser().getId());
    }

    @Test
    void findByUser_shouldReturnEmpty_whenFileDoesNotExistForUser() {
        User user = User.builder()
                .email("no-file@test.com")
                .firstName("No")
                .lastName("File")
                .password("password")
                .enabled(true)
                .specialization(null)
                .build();

        User savedUser = userJpaRepo.save(user);

        Optional<FileEntity> result = fileJpaRepo.findByUser(savedUser);

        assertTrue(result.isEmpty());
    }
}
