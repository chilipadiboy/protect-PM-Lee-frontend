package org.cs4239.team1.protectPMLeefrontendserver.model;

import org.cs4239.team1.protectPMLeefrontendserver.model.audit.UserDateAudit;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.MapsId;
import javax.persistence.FetchType;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "permissions")
public class Permission extends UserDateAudit {

    @EmbeddedId
    private PermissionId permissionID;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("recordId")
    private Record record;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("nric")
    private User user;

    @Column(name = "expiry")
    private Instant expiryDateTime;

    @Column(name = "OwnerPatient")
    private String patientNric;

    public Permission(Record record, User user, Instant expiryDateTime, String patientNric) {
        this.record = record;
        this.user = user;
        this.permissionID = new PermissionId(record.getRecordID(), user.getNric());
        this.expiryDateTime = expiryDateTime;
        this.patientNric = patientNric;
    }
}