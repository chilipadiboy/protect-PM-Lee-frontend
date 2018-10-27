package org.cs4239.team1.protectPMLeefrontendserver;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashSet;
import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.cs4239.team1.protectPMLeefrontendserver.model.Gender;
import org.cs4239.team1.protectPMLeefrontendserver.model.Note;
import org.cs4239.team1.protectPMLeefrontendserver.model.Permission;
import org.cs4239.team1.protectPMLeefrontendserver.model.Record;
import org.cs4239.team1.protectPMLeefrontendserver.model.Role;
import org.cs4239.team1.protectPMLeefrontendserver.model.Treatment;
import org.cs4239.team1.protectPMLeefrontendserver.model.User;
import org.cs4239.team1.protectPMLeefrontendserver.repository.NoteRepository;
import org.cs4239.team1.protectPMLeefrontendserver.repository.PermissionRepository;
import org.cs4239.team1.protectPMLeefrontendserver.repository.RecordRepository;
import org.cs4239.team1.protectPMLeefrontendserver.repository.TreatmentRepository;
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


	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {

		User admin = new User("S1234567A",
				"admin",
				"admin@gmail.com",
				"61111111",
				"Admin's House",
				21,
				Gender.MALE,
				passwordEncoder.encode("administrator"),
				"MW6ID/qlELbKxjap8tpzKRHmhhHwZ2w2GLp+vQByqss=",
				new HashSet<>(Collections.singletonList(Role.ROLE_ADMINISTRATOR)));
		User therapist01 = new User("S1234501T",
				"therapist01",
				"therapist01@gmail.com",
				"61111111",
				"therapist01's House",
				21,
				Gender.MALE,
				passwordEncoder.encode("therapist01"),
				"MW6ID/qlELbKxjap8tpzKRHmhhHwZ2w2GLp+vQByqss=",
				new HashSet<>(Collections.singletonList(Role.ROLE_THERAPIST)));
		User therapist02 = new User("S1234502T",
				"therapist02",
				"therapist02@gmail.com",
				"61111111",
				"therapist02's House",
				21,
				Gender.MALE,
				passwordEncoder.encode("therapist02"),
				"MW6ID/qlELbKxjap8tpzKRHmhhHwZ2w2GLp+vQByqss=",
				new HashSet<>(Collections.singletonList(Role.ROLE_THERAPIST)));
		User patient01 = new User("S1234501P",
				"patient01",
				"patient01@gmail.com",
				"61111111",
				"patient01's House",
				21,
				Gender.MALE,
				passwordEncoder.encode("patient01"),
				"MW6ID/qlELbKxjap8tpzKRHmhhHwZ2w2GLp+vQByqss=",
				new HashSet<>(Collections.singletonList(Role.ROLE_PATIENT)));
		User patient02 = new User("S1234502P",
				"patient02",
				"patient02@gmail.com",
				"61111111",
				"patient02's House",
				21,
				Gender.MALE,
				passwordEncoder.encode("patient02"),
				"MW6ID/qlELbKxjap8tpzKRHmhhHwZ2w2GLp+vQByqss=",
				new HashSet<>(Collections.singletonList(Role.ROLE_PATIENT)));
		Instant endDate = Instant.now().plus(10, ChronoUnit.DAYS);
		Record recordP01 = new Record("Document",
				"P",
				"Heart Beat",
				"linkToP01Record",
				"S1234501P");
		Record recordP02 = new Record("Document",
				"B2P",
				"Heart Beat",
				"linkToP02Record",
				"S1234502P");

		return args -> {
			userRepository.save(admin);
			userRepository.save(therapist01);
			userRepository.save(therapist02);
			userRepository.save(patient01);
			userRepository.save(patient02);
			treatmentRepository.save(new Treatment(therapist01,
					patient01,
					endDate));
			treatmentRepository.save(new Treatment(therapist02,
					patient01,
					endDate));
			recordRepository.save(recordP01);
			recordRepository.save(recordP02);
			permissionRepository.save(new Permission(recordP01,
					therapist01,
					endDate,
					patient01.getNric()));
			permissionRepository.save(new Permission(recordP02,
					therapist01,
					endDate,
					patient02.getNric()));
			noteRepository.save(new Note(therapist01,
					patient01,
					"Whats your problem la",
					false,
					true
			));
			noteRepository.save(new Note(patient01,
					patient01,
					"Help leh",
					true,
					false
			));
		};
	}
}
