package org.cs4239.team1.protectPMLeefrontendserver.model;

import lombok.NoArgsConstructor;
import org.cs4239.team1.protectPMLeefrontendserver.model.audit.UserDateAudit;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.Instant;

import lombok.Getter;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "treatments")
public class Treatment extends UserDateAudit {

    @EmbeddedId
    private TreatmentId treatmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    private User therapist;

    @ManyToOne(fetch = FetchType.LAZY)
    private User patient;

    @Column(name = "endDate")
    private Instant endDate;

    public Treatment(User therapist, User patient, Instant endDate) {
        this.treatmentId = new TreatmentId(therapist.getNric(), patient.getNric());
        this.therapist = therapist;
        this.patient = patient;
        this.endDate = endDate;
    }
}