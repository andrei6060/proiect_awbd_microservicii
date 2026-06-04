package com.clinic.clinic.JpaRepo;

import com.clinic.clinic.Entity.User.Token;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenJpaRepo extends JpaRepository<Token, Integer> {
    Optional<Token> findByToken(String token);
}
