package org.cs4239.team1.protectPMLeefrontendserver.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AnonymisedRecordResponse {
    private String location;
    private String age;
    private String gender;
    private String value;
}
