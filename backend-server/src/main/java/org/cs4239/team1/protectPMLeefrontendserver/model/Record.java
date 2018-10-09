package org.cs4239.team1.protectPMLeefrontendserver.model;

import java.time.Instant;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.cs4239.team1.protectPMLeefrontendserver.model.audit.UserDateAudit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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