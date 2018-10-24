package org.cs4239.team1.protectPMLeefrontendserver.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RecordResponseWithTherapistIdentifier {
    private Long recordID;
    private String type;
    private String subtype;
    private String title;
    private String document;
    private String therapistNric;
}