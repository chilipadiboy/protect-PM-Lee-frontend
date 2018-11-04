package org.cs4239.team1.protectPMLeefrontendserver.repository;

import java.util.List;
import java.util.Optional;

import org.cs4239.team1.protectPMLeefrontendserver.model.Record;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface RecordRepository extends JpaRepository<Record, Long> {

    Optional<Record> findByRecordID(Long recordID);
    Optional<Record> findByDocument(String document);
    Page<Record> findByCreatedBy(String nric, Pageable pageable);

    Page<Record> findByPatientIC(String nric, Pageable pageable);
    List<Record> findByPatientIC(String nric);
}