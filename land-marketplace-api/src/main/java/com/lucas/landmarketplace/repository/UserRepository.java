package com.lucas.landmarketplace.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lucas.landmarketplace.model.User;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByVerificationToken(String verificationToken);

    Optional<User> findByResetToken(String resetToken);

    boolean existsByEmail(String email);
}
