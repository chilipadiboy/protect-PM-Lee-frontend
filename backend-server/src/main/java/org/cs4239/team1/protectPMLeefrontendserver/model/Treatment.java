package org.cs4239.team1.protectPMLeefrontendserver.model;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.cs4239.team1.protectPMLeefrontendserver.model.audit.UserDateAudit;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "treatments")
public class Treatment extends UserDateAudit {

    @EmbeddedId
    private TreatmentId treatmentId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    private User therapist;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    private User patient;

    @Column(name = "endDate")
    private Instant endDate;

    //TODO: Wonder if need to check that the User exists for that particular role.
    public Treatment(User therapist, User patient, Instant endDate) {
        this.treatmentId = new TreatmentId(therapist.getNric(), patient.getNric());
        this.therapist = therapist;
        this.patient = patient;
        this.endDate = endDate;
    }
}