package org.cs4239.team1.protectPMLeefrontendserver.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class TreatmentId implements Serializable {

    @Column(name = "therapist", insertable = false, updatable = false)
    public String therapist;
    @Column(name = "patient", insertable = false, updatable = false)
    public String patient;
}
