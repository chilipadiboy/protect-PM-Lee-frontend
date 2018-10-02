package org.cs4239.team1.protectPMLeefrontendserver.model;

import lombok.Setter;
import org.cs4239.team1.protectPMLeefrontendserver.model.audit.UserDateAudit;

import java.time.Instant;

import javax.persistence.Id;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import javax.ws.rs.core.Link;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.cs4239.team1.protectPMLeefrontendserver.payload.UserSummary;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Records")
public class Record extends UserDateAudit {
    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
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