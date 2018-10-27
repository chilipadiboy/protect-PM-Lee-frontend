package org.cs4239.team1.protectPMLeefrontendserver.service;

import org.cs4239.team1.protectPMLeefrontendserver.exception.BadRequestException;
import org.cs4239.team1.protectPMLeefrontendserver.exception.ResourceNotFoundException;
import org.cs4239.team1.protectPMLeefrontendserver.model.Note;
import org.cs4239.team1.protectPMLeefrontendserver.model.Role;
import org.cs4239.team1.protectPMLeefrontendserver.model.TreatmentId;
import org.cs4239.team1.protectPMLeefrontendserver.model.User;
import org.cs4239.team1.protectPMLeefrontendserver.payload.NotePermissionRequest;
import org.cs4239.team1.protectPMLeefrontendserver.payload.NoteRequest;
import org.cs4239.team1.protectPMLeefrontendserver.payload.NoteResponse;
import org.cs4239.team1.protectPMLeefrontendserver.payload.PagedResponse;
import org.cs4239.team1.protectPMLeefrontendserver.repository.NoteRepository;
import org.cs4239.team1.protectPMLeefrontendserver.repository.TreatmentRepository;
import org.cs4239.team1.protectPMLeefrontendserver.repository.UserRepository;
import org.cs4239.team1.protectPMLeefrontendserver.util.AppConstants;
import org.cs4239.team1.protectPMLeefrontendserver.util.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class NoteService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TreatmentRepository treatmentRepository;

    @Autowired
    private NoteRepository noteRepository;

    private static final Logger logger = LoggerFactory.getLogger(NoteService.class);

    @PreAuthorize("hasRole('THERAPIST') or hasRole('PATIENT')")
    public Note createNote(NoteRequest noteRequest, User creator) {

        boolean isVisibleToPatient;
        boolean isVisibleToTherapist;

        User patient = userRepository.findByNric(noteRequest.getPatientNric())
                .orElseThrow(() -> new ResourceNotFoundException("User", "nric", noteRequest.getPatientNric()));
        if (!patient.getRoles().contains(Role.ROLE_PATIENT)){
            throw new BadRequestException("User_" + patient.getNric() + " is not a patient!");
        }

        // Creator is the patient
        if (creator.getNric().equals(noteRequest.getPatientNric())){
            isVisibleToPatient = true;
            isVisibleToTherapist = false;
        }
        // Treatment pair exist
        else if(treatmentRepository.findByTreatmentId(new TreatmentId(creator.getNric(), patient.getNric())) != null){
            isVisibleToPatient = false;
            isVisibleToTherapist = true;
        }
        else{
            throw new AssertionError("Should not happen.");
        }

        return noteRepository.save(new Note(creator, patient, noteRequest.getNoteContent(), isVisibleToPatient, isVisibleToTherapist));
    }

    @PreAuthorize("hasRole('THERAPIST')")
    public Note setNotePermission(NotePermissionRequest notePermissionRequest, User user) {

        boolean isVisibleToPatient;

        //valid note
        Note note = noteRepository.findByNoteID(notePermissionRequest.getNoteID())
                .orElseThrow(() -> new ResourceNotFoundException("Note", "noteID", notePermissionRequest.getNoteID()));

        //if user created the note and treatment period is still valid
        if( note.getCreator().getNric().equals(user.getNric()) &&
                treatmentRepository.findByTreatmentId(new TreatmentId(user.getNric(), note.getPatient().getNric())) != null){
            note.setIsVisibleToPatient(Boolean.parseBoolean(notePermissionRequest.getIsVisibleToPatient()));
        }
        else{
            throw new AssertionError("Should not happen.");
        }

        return noteRepository.save(note);
    }

    @PreAuthorize("hasRole('THERAPIST')")
    public PagedResponse<NoteResponse> getNotesOf(User currentUser, String patientNric, int page, int size) {
        validatePageNumberAndSize(size);
        boolean isVisbleToTherapist = true;

        //check if valid patient
        User patient = userRepository.findByNric(patientNric) //current user is the creator
                .orElseThrow(() -> new ResourceNotFoundException("User", "nric", patientNric));
        //check if treatment pair is valid
        if (treatmentRepository.findByTreatmentId(new TreatmentId(currentUser.getNric(), patientNric)) == null){
            throw new AssertionError("Should not happen.");
        }

        // Retrieve Records
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<Note> notes = noteRepository.findByPatientAndIsVisibleToTherapist(patient, isVisbleToTherapist, pageable);

        if(notes.getNumberOfElements() == 0) {
            return new PagedResponse<>(Collections.emptyList(), notes.getNumber(),
                    notes.getSize(), notes.getTotalElements(), notes.getTotalPages(), notes.isLast());
        }

        List<NoteResponse> note = notes.map(ModelMapper::mapNotetoNoteResponce).getContent();

        return new PagedResponse<>(note, notes.getNumber(),
                notes.getSize(), notes.getTotalElements(), notes.getTotalPages(), notes.isLast());
    }

    @PreAuthorize("hasRole('PATIENT')")
    public PagedResponse<NoteResponse> getOwnNotes(User currentUser, int page, int size) {
        validatePageNumberAndSize(size);

        // Retrieve Records
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<Note> notes = noteRepository.findByCreator(currentUser, pageable);

        if(notes.getNumberOfElements() == 0) {
            return new PagedResponse<>(Collections.emptyList(), notes.getNumber(),
                    notes.getSize(), notes.getTotalElements(), notes.getTotalPages(), notes.isLast());
        }
        List<NoteResponse> note = notes.map(ModelMapper::mapNotetoNoteResponce).getContent();

        return new PagedResponse<>(note, notes.getNumber(),
                notes.getSize(), notes.getTotalElements(), notes.getTotalPages(), notes.isLast());
    }

    @PreAuthorize("hasRole('PATIENT')")
    public PagedResponse<NoteResponse> getPermittedNotes(User currentUser, int page, int size) {
        validatePageNumberAndSize(size);
        boolean isVisibleToPatient = true;

        // Retrieve Records
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<Note> notes = noteRepository.findByPatientAndCreatorNotAndIsVisibleToPatient(currentUser, currentUser, isVisibleToPatient, pageable);

        if(notes.getNumberOfElements() == 0) {
            return new PagedResponse<>(Collections.emptyList(), notes.getNumber(),
                    notes.getSize(), notes.getTotalElements(), notes.getTotalPages(), notes.isLast());
        }
        List<NoteResponse> note = notes.map(ModelMapper::mapNotetoNoteResponce).getContent();

        return new PagedResponse<>(note, notes.getNumber(),
                notes.getSize(), notes.getTotalElements(), notes.getTotalPages(), notes.isLast());
    }


    private void validatePageNumberAndSize(int size) {
        if(size > AppConstants.MAX_PAGE_SIZE) {
            throw new BadRequestException("Page size must not be greater than " + AppConstants.MAX_PAGE_SIZE);
        }
    }
}