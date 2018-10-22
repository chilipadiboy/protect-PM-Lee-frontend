package org.cs4239.team1.protectPMLeefrontendserver.util;

import org.cs4239.team1.protectPMLeefrontendserver.model.Permission;
import org.cs4239.team1.protectPMLeefrontendserver.model.Record;
import org.cs4239.team1.protectPMLeefrontendserver.model.User;
import org.cs4239.team1.protectPMLeefrontendserver.payload.RecordResponse;
import org.cs4239.team1.protectPMLeefrontendserver.payload.RecordResponseWithTherapistIdentifier;

public class ModelMapper {

    public static RecordResponse mapRecordToRecordResponse(Record record, User therapist) {

        return new RecordResponse(record.getRecordID(),
                record.getType(),
                record.getSubtype(),
                record.getTitle(),
                record.getDocument(),
                record.getPatientIC());
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
}