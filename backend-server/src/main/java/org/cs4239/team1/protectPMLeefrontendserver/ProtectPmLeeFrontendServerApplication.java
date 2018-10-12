package org.cs4239.team1.protectPMLeefrontendserver;

import java.util.Collections;
import java.util.HashSet;
import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.cs4239.team1.protectPMLeefrontendserver.model.Gender;
import org.cs4239.team1.protectPMLeefrontendserver.model.Role;
import org.cs4239.team1.protectPMLeefrontendserver.model.User;
import org.cs4239.team1.protectPMLeefrontendserver.repository.UserRepository;
import org.cs4239.team1.protectPMLeefrontendserver.storage.FileStorageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EntityScan(basePackageClasses = {
		ProtectPmLeeFrontendServerApplication.class,
		Jsr310JpaConverters.class
})
@EnableConfigurationProperties({
		FileStorageProperties.class
})
public class ProtectPmLeeFrontendServerApplication {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

	@PostConstruct
	void init() {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}

	public static void main(String[] args) {
		SpringApplication.run(ProtectPmLeeFrontendServerApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {
		    userRepository.save(new User("S1234567A",
                    "admin",
                    "admin@gmail.com",
                    "61111111",
                    "Admin's House",
                    21,
                    Gender.MALE,
                    passwordEncoder.encode("admin"),
                    new HashSet<>(Collections.singletonList(Role.ROLE_ADMINISTRATOR))));
		};
	}
}
