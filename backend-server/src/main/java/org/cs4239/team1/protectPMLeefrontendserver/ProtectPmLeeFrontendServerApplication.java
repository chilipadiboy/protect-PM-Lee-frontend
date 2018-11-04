package org.cs4239.team1.protectPMLeefrontendserver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.cs4239.team1.protectPMLeefrontendserver.exception.ResourceNotFoundException;
import org.cs4239.team1.protectPMLeefrontendserver.model.Gender;
import org.cs4239.team1.protectPMLeefrontendserver.model.Record;
import org.cs4239.team1.protectPMLeefrontendserver.model.Role;
import org.cs4239.team1.protectPMLeefrontendserver.model.Subtype;
import org.cs4239.team1.protectPMLeefrontendserver.model.Treatment;
import org.cs4239.team1.protectPMLeefrontendserver.model.Type;
import org.cs4239.team1.protectPMLeefrontendserver.model.User;
import org.cs4239.team1.protectPMLeefrontendserver.repository.NoteRepository;
import org.cs4239.team1.protectPMLeefrontendserver.repository.PermissionRepository;
import org.cs4239.team1.protectPMLeefrontendserver.repository.RecordRepository;
import org.cs4239.team1.protectPMLeefrontendserver.repository.TreatmentRepository;
import org.cs4239.team1.protectPMLeefrontendserver.repository.UserRepository;
import org.cs4239.team1.protectPMLeefrontendserver.storage.FileStorageProperties;
import org.cs4239.team1.protectPMLeefrontendserver.storage.LogsStorageProperties;
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
        FileStorageProperties.class,
        LogsStorageProperties.class
})
public class ProtectPmLeeFrontendServerApplication {

	@Autowired
	UserRepository userRepository;

	@Autowired
	RecordRepository recordRepository;

	@Autowired
	TreatmentRepository treatmentRepository;

	@Autowired
	PermissionRepository permissionRepository;

	@Autowired
	NoteRepository noteRepository;

	@Autowired
	PasswordEncoder passwordEncoder;

	@PostConstruct
	void init() {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}

	public static void main(String[] args) {
		SpringApplication.run(ProtectPmLeeFrontendServerApplication.class, args);
	}

	private void readUsers() {
	    try {
            Path path = Paths.get(System.getProperty("user.dir")).getParent().resolve("Mock Data/FullUserList.csv");
            Files.lines(path).forEach(line -> {
                String[] values = line.split(",");
                userRepository.save(new User(values[5],
                        values[1],
                        values[3],
                        values[8],
                        values[4],
                        values[7],
                        Integer.valueOf(values[6]),
                        Gender.create(values[2]),
                        passwordEncoder.encode(values[9]),
                        values[10],
                        "",
                        0,
                        0,
                        new HashSet<>(Collections.singletonList(Role.create(values[11])))));
            });

            path = Paths.get(System.getProperty("user.dir")).getParent().resolve("Mock Data/Patients_Therapists.csv");
            Files.lines(path).forEach(line -> {
                String[] values = line.split("\t");
                User patient = userRepository.findByNric(values[1])
                        .orElseThrow(() -> new ResourceNotFoundException("User", "nric", values[1]));
                User therapist = userRepository.findByNric(values[2])
                        .orElseThrow(() -> new ResourceNotFoundException("User", "nric", values[2]));
                treatmentRepository.save(new Treatment(therapist, patient, Instant.now().plus(Duration.ofDays(20))));
            });

            path = Paths.get(System.getProperty("user.dir")).getParent().resolve("Mock Data/Patients_Records.csv");
            Files.lines(path).forEach(line -> {
                String[] values = line.split("\t");
                if (values.length == 6) {
                    recordRepository.save(new Record(Type.create(values[2]), Subtype.valueOf(values[3]), "foo", values[5], values[1], ""));
                } else {
                    recordRepository.save(new Record(Type.create(values[2]), Subtype.valueOf(values[3]), "foo", "", values[1], ""));
                }
            });

            path = Paths.get(System.getProperty("user.dir")).getParent().resolve("Mock Data/Secret data.csv");
            Files.lines(path).forEach(line -> {
                String[] values = line.split(",");
                String[] roles = values[12].split("\\|");
                List<Role> roleList = Arrays.stream(roles).map(Role::create).collect(Collectors.toList());
                userRepository.save(new User(values[5],
                        values[1],
                        values[3],
                        values[8],
                        values[4],
                        values[7],
                        Integer.valueOf(values[6]),
                        Gender.create(values[2]),
                        passwordEncoder.encode(values[9]),
                        values[10],
                        values[11],
						0,
						0,
                        new HashSet<>(roleList)));
            });
        } catch (IOException ioe) {
	        throw new AssertionError("Should not happen.", ioe);
        }
    }


	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        readUsers();
		return args -> {};
	}
}
