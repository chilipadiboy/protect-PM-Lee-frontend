package org.cs4239.team1.protectPMLeefrontendserver.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EndPermissionRequest {

    @NonNull
    private Long recordID;

    @NonNull
    @NotBlank
    @Size(min = 9, max = 9)
    private String therapistNric;
}