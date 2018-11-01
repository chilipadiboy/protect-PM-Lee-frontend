package org.cs4239.team1.protectPMLeefrontendserver.controller;

import org.cs4239.team1.protectPMLeefrontendserver.model.Note;
import org.cs4239.team1.protectPMLeefrontendserver.model.User;
import org.cs4239.team1.protectPMLeefrontendserver.payload.ApiResponse;
import org.cs4239.team1.protectPMLeefrontendserver.payload.NotePermissionRequest;
import org.cs4239.team1.protectPMLeefrontendserver.payload.NoteRequest;
import org.cs4239.team1.protectPMLeefrontendserver.payload.NoteResponse;
import org.cs4239.team1.protectPMLeefrontendserver.payload.NoteUpdateRequest;
import org.cs4239.team1.protectPMLeefrontendserver.payload.PagedResponse;
import org.cs4239.team1.protectPMLeefrontendserver.security.CurrentUser;
import org.cs4239.team1.protectPMLeefrontendserver.service.NoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    @Autowired
    private NoteService noteService;

    private static final Logger logger = LoggerFactory.getLogger(RecordController.class);

    @PostMapping("/create/")
    //therapist and patient can create their own notes
    public ResponseEntity<?> createNote(@Valid @RequestBody NoteRequest noteRequest, @CurrentUser User currentUser) {

        Note note = noteService.createNote(noteRequest, currentUser);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{noteID}")
                .buildAndExpand(note.getNoteID())
                .toUri();

        return ResponseEntity.created(location)
                .body(new ApiResponse(true, "Note_" + note.getNoteID() + " created"));
    }

    @PostMapping("/update/")
    //therapist and patient can update their own notes
    public ResponseEntity<?> updateNote(@Valid @RequestBody NoteUpdateRequest noteUpdateRequest, @CurrentUser User currentUser) {

        Note note = noteService.updateNote(noteUpdateRequest, currentUser);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{noteID}")
                .buildAndExpand(note.getNoteID())
                .toUri();

        return ResponseEntity.created(location)
                .body(new ApiResponse(true, "Note_" + note.getNoteID() + " updated"));
    }

    @PostMapping("/notePermission/")
    //therapist allow/disallow patient to see therapist notes
    public ResponseEntity<?> setNotePermission(@Valid @RequestBody NotePermissionRequest notePermissionRequest, @CurrentUser User currentUser) {

        Note note = noteService.setNotePermission(notePermissionRequest, currentUser);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{noteID}")
                .buildAndExpand(note.getNoteID())
                .toUri();

        return ResponseEntity.created(location)
                .body(new ApiResponse(true, "Note_" + note.getNoteID() + " permission changed"));
    }

    @GetMapping("/checkNoteIdConsent/{noteID}/")
    //Patient get notes permitted by any other therapists
    public ResponseEntity<?> checkNoteIdConsent(@Valid @PathVariable Long noteID, @CurrentUser User currentUser) {

        Note note = noteService.checkNoteIdConsent(noteID, currentUser);

        if (note.isVisibleToPatient()){
            return new ResponseEntity(new ApiResponse(true, "Patient has permission to view Note_" + note.getNoteID()), HttpStatus.FOUND);
        }else{
            return new ResponseEntity(new ApiResponse(true, "Patient does NOT have permission to view Note_" + note.getNoteID()), HttpStatus.FOUND);
        }
    }

    @GetMapping("/getPatient/{patientNric}/")
    //Therapist get all other therapist notes of this patient
    public PagedResponse<NoteResponse> getNotesOf(@CurrentUser User currentUser,
                                                  @PathVariable String patientNric) {
        return noteService.getNotesOf(currentUser, patientNric);
    }

    @GetMapping("/getOwn/")
    //Patient get all his own notes
    public PagedResponse<NoteResponse> getOwnNotes(@CurrentUser User currentUser) {
        return noteService.getOwnNotes(currentUser);
    }

    @GetMapping("/getPermitted/")
    //Patient get notes permitted by any other therapists
    public PagedResponse<NoteResponse> getPermittedNotes(@CurrentUser User currentUser) {
        return noteService.getPermittedNotes(currentUser);
    }
}