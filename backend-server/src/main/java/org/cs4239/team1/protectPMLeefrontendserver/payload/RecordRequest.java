package org.cs4239.team1.protectPMLeefrontendserver.payload;

import java.util.List;
import java.time.Instant;
import javax.validation.constraints.NotEmpty;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecordRequest {

    @NotBlank
    @Size(max = 140)
    private String recordID;

    @NotBlank
    @Size(max = 140)
    private String type;

    @NotBlank
    @Size(max = 140)
    private String subtype;

    @NotBlank
    @Size(max = 140)
    private String title;

    //@NotEmpty
    //@Size(max = 140)
    private Instant date_time;

    @NotBlank
    @Size(max = 140)
    private String document;

    @NotBlank
    @Size(max = 140)
    private String patientIC;
}