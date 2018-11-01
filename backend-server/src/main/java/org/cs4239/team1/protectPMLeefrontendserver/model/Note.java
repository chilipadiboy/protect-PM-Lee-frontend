package org.cs4239.team1.protectPMLeefrontendserver.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cs4239.team1.protectPMLeefrontendserver.model.audit.UserDateAudit;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "Notes")
public class Note extends UserDateAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long noteID;

    @ManyToOne(fetch = FetchType.LAZY)
    private User creator;

    @ManyToOne(fetch = FetchType.LAZY)
    private User patient;

    @Setter
    @Size(max = 140)
    private String noteContent;

    @Setter
    private boolean isVisibleToPatient;
    private boolean isVisibleToTherapist;

    public Note(User creator, User patient, String noteContent, boolean isVisibleToPatient, boolean isVisibleToTherapist){
        this.creator = creator;
        this.patient = patient;
        this.noteContent = noteContent;
        this.isVisibleToPatient = isVisibleToPatient;
        this.isVisibleToTherapist = isVisibleToTherapist;
    }
}