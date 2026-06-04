package com.clinic.clinic.role;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleJpaRepo extends JpaRepository<Role, Integer> {

    Optional<Role> findByName(String roleName);
}
