package org.cs4239.team1.protectPMLeefrontendserver.util;

import org.cs4239.team1.protectPMLeefrontendserver.model.Record;
import org.cs4239.team1.protectPMLeefrontendserver.model.User;
import org.cs4239.team1.protectPMLeefrontendserver.payload.RecordResponse;
import org.cs4239.team1.protectPMLeefrontendserver.payload.UserSummary;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ModelMapper {

    public static RecordResponse mapRecordToRecordResponse(Record record, User therapist) {

        RecordResponse recordResponse = new RecordResponse();

        recordResponse.setRecordID(record.getRecordID());
        recordResponse.setType(record.getType());
        recordResponse.setSubtype(record.getSubtype());
        recordResponse.setTitle(record.getTitle());
        recordResponse.setDate_time(record.getDate_time());
        recordResponse.setDocument(record.getDocument());
        recordResponse.setPatientIC(record.getPatientIC());

        return recordResponse;
    }
}