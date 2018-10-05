package org.cs4239.team1.protectPMLeefrontendserver.repository;

import org.cs4239.team1.protectPMLeefrontendserver.model.Record;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecordRepository extends JpaRepository<Record, String> {
    Optional<Record> findByRecordID(String recordID);

    Page<Record> findByCreatedBy(String nric, Pageable pageable);

    Page<Record> findByPatientIC(String nric, Pageable pageable);

    List<Record> findByRecordIDIn(List<String> recordIDs);

    List<Record> findByRecordIDIn(List<String> recordIDs, Sort sort);
}