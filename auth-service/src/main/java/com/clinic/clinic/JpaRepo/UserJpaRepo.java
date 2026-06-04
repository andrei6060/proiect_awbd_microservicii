package com.clinic.clinic.JpaRepo;

import com.clinic.clinic.Entity.User.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepo extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

}
