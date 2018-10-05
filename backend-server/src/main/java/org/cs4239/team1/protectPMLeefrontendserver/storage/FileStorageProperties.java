package org.cs4239.team1.protectPMLeefrontendserver.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@ConfigurationProperties(prefix = "file")
public class FileStorageProperties {
    private String uploadDir;
}