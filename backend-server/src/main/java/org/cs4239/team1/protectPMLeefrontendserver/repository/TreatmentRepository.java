package org.cs4239.team1.protectPMLeefrontendserver.repository;

import org.cs4239.team1.protectPMLeefrontendserver.model.Treatment;
import org.cs4239.team1.protectPMLeefrontendserver.model.TreatmentId;
import org.cs4239.team1.protectPMLeefrontendserver.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TreatmentRepository extends JpaRepository<Treatment, TreatmentId> {
    Page<Treatment> findByTherapist(User therapist, Pageable pageable);
    Page<Treatment> findByPatient(User patient, Pageable pageable);
    Treatment findByTreatmentId(TreatmentId treatmentId);
}
