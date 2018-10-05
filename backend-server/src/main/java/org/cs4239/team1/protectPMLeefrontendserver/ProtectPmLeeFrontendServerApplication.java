package org.cs4239.team1.protectPMLeefrontendserver;

import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.cs4239.team1.protectPMLeefrontendserver.storage.FileStorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

@SpringBootApplication
@EntityScan(basePackageClasses = {
		ProtectPmLeeFrontendServerApplication.class,
		Jsr310JpaConverters.class
})
@EnableConfigurationProperties({
		FileStorageProperties.class
})
public class ProtectPmLeeFrontendServerApplication {

	@PostConstruct
	void init() {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}

	public static void main(String[] args) {
		SpringApplication.run(ProtectPmLeeFrontendServerApplication.class, args);
	}
}
