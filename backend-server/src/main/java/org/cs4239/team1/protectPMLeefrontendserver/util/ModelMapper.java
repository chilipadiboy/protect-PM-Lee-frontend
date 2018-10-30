package org.cs4239.team1.protectPMLeefrontendserver.util;

import org.cs4239.team1.protectPMLeefrontendserver.model.Note;
import org.cs4239.team1.protectPMLeefrontendserver.model.Permission;
import org.cs4239.team1.protectPMLeefrontendserver.model.Record;
import org.cs4239.team1.protectPMLeefrontendserver.payload.NoteResponse;
import org.cs4239.team1.protectPMLeefrontendserver.payload.RecordResponseWithTherapistIdentifier;

public class ModelMapper {

    public static Record mapPermissionToRecord(Permission permission
    ) {

        return new Record(permission.getRecord().getRecordID(),
                permission.getRecord().getType(),
                permission.getRecord().getSubtype(),
                permission.getRecord().getTitle(),
                permission.getRecord().getDocument(),
                permission.getRecord().getPatientIC(),
                permission.getRecord().getFileSignature());
    }

    public static RecordResponseWithTherapistIdentifier mapRecordToRecordResponseWithTherapistIdentifier(Permission permission) {
        return new RecordResponseWithTherapistIdentifier(
                permission.getRecord().getRecordID(),
                permission.getRecord().getType(),
                permission.getRecord().getSubtype(),
                permission.getRecord().getTitle(),
                permission.getRecord().getDocument(),
                permission.getUser().getNric());
    }

    public static NoteResponse mapNotetoNoteResponce(Note note) {

        return new NoteResponse(note.getNoteID(),
                note.getCreator().getNric(),
                note.getPatient().getNric(),
                note.getNoteContent());

    }
}