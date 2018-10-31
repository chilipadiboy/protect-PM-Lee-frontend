package org.cs4239.team1.protectPMLeefrontendserver.payload;

import lombok.Getter;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;

@Getter
public class NoteUpdateRequest {

    @NonNull
    private Long noteID;

    @NonNull
    @NotBlank
    private String noteContent;
}