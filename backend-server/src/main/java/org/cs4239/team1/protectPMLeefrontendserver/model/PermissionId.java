package org.cs4239.team1.protectPMLeefrontendserver.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class PermissionId implements Serializable {

    @Column(name = "recordID")
    public Long recordId;
    @Column(name = "nric")
    public String nric;

}
