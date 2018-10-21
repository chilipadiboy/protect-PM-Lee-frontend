package org.cs4239.team1.protectPMLeefrontendserver.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class TreatmentId implements Serializable {

    @Column(name = "therapist", insertable = false, updatable = false)
    public String therapist;
    @Column(name = "patient", insertable = false, updatable = false)
    public String patient;
}
