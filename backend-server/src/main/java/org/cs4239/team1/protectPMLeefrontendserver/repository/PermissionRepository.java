package org.cs4239.team1.protectPMLeefrontendserver.repository;

import org.cs4239.team1.protectPMLeefrontendserver.model.Permission;
import org.cs4239.team1.protectPMLeefrontendserver.model.PermissionId;
import org.cs4239.team1.protectPMLeefrontendserver.model.Record;
import org.cs4239.team1.protectPMLeefrontendserver.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, PermissionId> {
    Page<Permission> findByUser(User user, Pageable pageable);
    Page<Permission> findByPatientNric(String nric, Pageable pageable);
    Page<Permission> findByUserAndPatientNric(User user, String nric, Pageable pageable);
    Permission findByPermissionID(PermissionId permissionId);
    Optional<Permission> findByUserAndRecord(User therapist, Record record);
}
