package org.cs4239.team1.protectPMLeefrontendserver.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.cs4239.team1.protectPMLeefrontendserver.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByNric(String nric);
    Boolean existsByNric(String nric);
    Boolean existsByEmail(String email);
}
