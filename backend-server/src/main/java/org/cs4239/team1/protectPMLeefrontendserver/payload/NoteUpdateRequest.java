package org.cs4239.team1.protectPMLeefrontendserver.payload;

import lombok.Getter;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
public class NoteUpdateRequest {

    @NonNull
    private Long noteID;

    @NonNull
    @NotBlank
    @Size(max = 140)
    private String noteContent;
}