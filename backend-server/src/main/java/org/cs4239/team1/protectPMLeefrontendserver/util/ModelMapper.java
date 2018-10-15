package org.cs4239.team1.protectPMLeefrontendserver.util;

import org.cs4239.team1.protectPMLeefrontendserver.model.Record;
import org.cs4239.team1.protectPMLeefrontendserver.model.User;
import org.cs4239.team1.protectPMLeefrontendserver.payload.RecordResponse;

public class ModelMapper {

    public static RecordResponse mapRecordToRecordResponse(Record record, User therapist) {

        RecordResponse recordResponse = new RecordResponse(record.getRecordID(),
                record.getType(),
                record.getSubtype(),
                record.getTitle(),
                record.getDocument(),
                record.getPatientIC());

        return recordResponse;
    }
}