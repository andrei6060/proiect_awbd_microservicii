package com.clinic.clinic.JpaRepo;

import com.clinic.clinic.Entity.File.FileEntity;
import com.clinic.clinic.Entity.User.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileJpaRepo extends JpaRepository<FileEntity, Long> {
    Optional<FileEntity> findByUser(User user);
}
