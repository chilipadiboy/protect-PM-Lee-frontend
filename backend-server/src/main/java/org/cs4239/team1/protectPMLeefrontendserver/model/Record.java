package org.cs4239.team1.protectPMLeefrontendserver.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.cs4239.team1.protectPMLeefrontendserver.model.audit.Type;
import org.cs4239.team1.protectPMLeefrontendserver.model.audit.UserDateAudit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Records")
public class Record extends UserDateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recordID;

    @NonNull
    private Type type;

    @NonNull
    private Subtype subtype;

    @NonNull
    @NotBlank
    @Size(max = 140)
    private String title;

    @NonNull
    @Size(max = 140)
    private String document;

    @NonNull
    @NotBlank
    @Size(max = 140)
    private String patientIC;

    @NonNull
    @Size(max = 300)
    private String fileSignature;

}