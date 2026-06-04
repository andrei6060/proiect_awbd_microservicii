package com.clinic.clinic.auth;


import com.clinic.clinic.Entity.User.User;
import com.clinic.clinic.JpaRepo.UserJpaRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class UserJpaRepoTest {

    @Autowired
    private UserJpaRepo userJpaRepo;

    @Test
    void findByEmail_shouldReturnUser() {
        User user = User.builder()
                .email("test@test.com")
                .firstName("John")
                .lastName("Doe")
                .password("pass")
                .enabled(true)
                .accountLocked(false)
                .build();

        userJpaRepo.save(user);

        Optional<User> found =
                userJpaRepo.findByEmail("test@test.com");

        assertTrue(found.isPresent());
        assertEquals("John", found.get().getFirstName());
    }

    @Test
    void findByEmail_shouldReturnEmpty() {
        Optional<User> found =
                userJpaRepo.findByEmail("missing@test.com");

        assertTrue(found.isEmpty());
    }
}
