package org.cs4239.team1.protectPMLeefrontendserver.repository;

import org.cs4239.team1.protectPMLeefrontendserver.model.Note;
import org.cs4239.team1.protectPMLeefrontendserver.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    Page<Note> findByPatientAndCreatorNotAndIsVisibleToPatient(User patient, User creator, boolean isVisibleToPatient,Pageable pageable);
    Page<Note> findByCreator(User creator, Pageable pageable);
    Page<Note> findByPatientAndIsVisibleToTherapist (User patient, boolean isVisibleToTherapist, Pageable pageable);
    Optional<Note> findByNoteID(long noteID);
}
