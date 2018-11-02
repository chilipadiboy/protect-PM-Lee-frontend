package org.cs4239.team1.protectPMLeefrontendserver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.cs4239.team1.protectPMLeefrontendserver.exception.ResourceNotFoundException;
import org.cs4239.team1.protectPMLeefrontendserver.model.Gender;
import org.cs4239.team1.protectPMLeefrontendserver.model.Note;
import org.cs4239.team1.protectPMLeefrontendserver.model.Permission;
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
            Files.lines(Paths.get("/Users/zhiyuan/protect-PM-Lee-frontend/Mock Data/Patients.csv")).forEach(line -> {
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
                        new HashSet<>(Collections.singletonList(Role.create(values[11])))));
            });

            Files.lines(Paths.get("/Users/zhiyuan/protect-PM-Lee-frontend/Mock Data/Therapists.csv")).forEach(line -> {
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
                        new HashSet<>(Collections.singletonList(Role.create(values[11])))));
            });

            Files.lines(Paths.get("/Users/zhiyuan/protect-PM-Lee-frontend/Mock Data/Patients_Therapists.csv")).forEach(line -> {
                String[] values = line.split(",");
                User patient = userRepository.findByNric(values[1])
                        .orElseThrow(() -> new ResourceNotFoundException("User", "nric", values[1]));
                User therapist = userRepository.findByNric(values[2])
                        .orElseThrow(() -> new ResourceNotFoundException("User", "nric", values[2]));
                treatmentRepository.save(new Treatment(therapist, patient, Instant.now().plus(Duration.ofDays(20))));
            });

            Files.lines(Paths.get("/Users/zhiyuan/protect-PM-Lee-frontend/Mock Data/Patients_Records.csv")).forEach(line -> {
                String[] values = line.split("\t");
                if (values.length == 6) {
                    recordRepository.save(new Record(Type.create(values[2]), Subtype.create(values[3]), "foo", values[5], values[1], ""));
                } else {
                    recordRepository.save(new Record(Type.create(values[2]), Subtype.create(values[3]), "foo", "", values[1], ""));
                }
            });

            Files.lines(Paths.get("/Users/zhiyuan/protect-PM-Lee-frontend/Mock Data/Secret data.csv")).forEach(line -> {
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
                        "",
                        new HashSet<>(roleList)));
            });
        } catch (IOException ioe) {
	        throw new AssertionError("Should not happen.", ioe);
        }
    }


	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        readUsers();
		User admin = new User("S1234567A",
				"admin",
				"admin@gmail.com",
				"61111111",
				"Admin's House",
				"511111",
				21,
				Gender.MALE,
				passwordEncoder.encode("administrator"),
				"MW6ID/qlELbKxjap8tpzKRHmhhHwZ2w2GLp+vQByqss=",
                "",
                new HashSet<>(Arrays.asList(Role.ROLE_ADMINISTRATOR, Role.ROLE_RESEARCHER)));
		User external = new User("S1234567E",
				"external",
				"external@gmail.com",
				"61111111",
				"External's House",
				"381112",
				21,
				Gender.MALE,
				passwordEncoder.encode("external"),
				"MW6ID/qlELbKxjap8tpzKRHmhhHwZ2w2GLp+vQByqss=",
				"",
				new HashSet<>(Collections.singletonList(Role.ROLE_EXTERNAL_PARTNER)));
		User therapist01 = new User("S1234501T",
				"therapist01",
				"therapist01@gmail.com",
				"61111111",
				"therapist01's House",
				"381111",
				21,
				Gender.MALE,
				passwordEncoder.encode("therapist01"),
				"MW6ID/qlELbKxjap8tpzKRHmhhHwZ2w2GLp+vQByqss=",
				"",
				new HashSet<>(Collections.singletonList(Role.ROLE_THERAPIST)));
		User therapist02 = new User("S1234502T",
				"therapist02",
				"therapist02@gmail.com",
				"61111111",
				"therapist02's House",
				"400123",
				21,
				Gender.MALE,
				passwordEncoder.encode("therapist02"),
				"MW6ID/qlELbKxjap8tpzKRHmhhHwZ2w2GLp+vQByqss=",
				"",
				new HashSet<>(Collections.singletonList(Role.ROLE_THERAPIST)));
		User patient01 = new User("S1234501P",
				"patient01",
				"patient01@gmail.com",
				"61111111",
				"patient01's House",
				"500123",
				21,
				Gender.MALE,
				passwordEncoder.encode("patient01"),
				"MW6ID/qlELbKxjap8tpzKRHmhhHwZ2w2GLp+vQByqss=",
				"",
				new HashSet<>(Collections.singletonList(Role.ROLE_PATIENT)));
		User patient02 = new User("S1234502P",
				"patient02",
				"patient02@gmail.com",
				"61111111",
				"patient02's House",
				"520123",
				21,
				Gender.MALE,
				passwordEncoder.encode("patient02"),
				"MW6ID/qlELbKxjap8tpzKRHmhhHwZ2w2GLp+vQByqss=",
				"",
				new HashSet<>(Collections.singletonList(Role.ROLE_PATIENT)));
		Instant endDate = Instant.now().plus(10, ChronoUnit.DAYS);
		Record recordP01 = new Record(Type.ILLNESS,
				Subtype.ALLERGY,
				"Heart Beat",
				"linkToP01Record",
				"S1234501P",
				"");
		Record recordP02 = new Record(Type.ILLNESS,
				Subtype.ALLERGY,
				"Heart Beat",
				"linkToP02Record",
				"S1234502P",
				"");

		return args -> {
			userRepository.save(admin);
			userRepository.save(external);
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

			userRepository.save(new User("S1111111A",
					"foo",
					"foo1@bar.com",
					"61111111",
					"foo",
					"010000",
					21,
					Gender.MALE,
					passwordEncoder.encode("foobarbaz"),
					"MW6ID/qlELbKxjap8tpzKRHmhhHwZ2w2GLp+vQByqss=",
					"",
					new HashSet<>(Arrays.asList(Role.ROLE_PATIENT, Role.ROLE_THERAPIST))));

			userRepository.save(new User("S1111111B",
					"foo",
					"foo2@bar.com",
					"61111111",
					"foo",
					"023930",
					22,
					Gender.MALE,
					passwordEncoder.encode("foobarbaz"),
					"MW6ID/qlELbKxjap8tpzKRHmhhHwZ2w2GLp+vQByqss=",
					"",
					new HashSet<>(Arrays.asList(Role.ROLE_PATIENT, Role.ROLE_THERAPIST))));

			userRepository.save(new User("S1111111C",
					"foo",
					"foo3@bar.com",
					"61111111",
					"foo",
					"035412",
					23,
					Gender.MALE,
					passwordEncoder.encode("foobarbaz"),
					"MW6ID/qlELbKxjap8tpzKRHmhhHwZ2w2GLp+vQByqss=",
					"",
					new HashSet<>(Arrays.asList(Role.ROLE_PATIENT, Role.ROLE_THERAPIST))));

			userRepository.save(new User("S1111111D",
					"foo",
					"foo4@bar.com",
					"61111111",
					"foo",
					"150012",
					24,
					Gender.MALE,
					passwordEncoder.encode("foobarbaz"),
					"MW6ID/qlELbKxjap8tpzKRHmhhHwZ2w2GLp+vQByqss=",
					"",
					new HashSet<>(Arrays.asList(Role.ROLE_PATIENT, Role.ROLE_THERAPIST))));

			userRepository.save(new User("S1111111E",
					"foo",
					"foo7@bar.com",
					"61111111",
					"foo",
					"162012",
					35,
					Gender.MALE,
					passwordEncoder.encode("foobarbaz"),
					"MW6ID/qlELbKxjap8tpzKRHmhhHwZ2w2GLp+vQByqss=",
					"",
					new HashSet<>(Arrays.asList(Role.ROLE_PATIENT, Role.ROLE_THERAPIST))));

			userRepository.save(new User("S1111111F",
					"foo",
					"foo8@bar.com",
					"61111111",
					"foo",
					"172012",
					37,
					Gender.MALE,
					passwordEncoder.encode("foobarbaz"),
					"MW6ID/qlELbKxjap8tpzKRHmhhHwZ2w2GLp+vQByqss=",
					"",
					new HashSet<>(Arrays.asList(Role.ROLE_PATIENT, Role.ROLE_THERAPIST))));

			userRepository.save(new User("S1111111G",
					"foo",
					"foo6@bar.com",
					"61111111",
					"foo",
					"200000",
					38,
					Gender.MALE,
					passwordEncoder.encode("foobarbaz"),
					"MW6ID/qlELbKxjap8tpzKRHmhhHwZ2w2GLp+vQByqss=",
					"",
					new HashSet<>(Arrays.asList(Role.ROLE_PATIENT, Role.ROLE_THERAPIST))));

			recordRepository.save(new Record(Type.ILLNESS,
					Subtype.ALLERGY,
					"foo",
					"linkToP01Record",
					"S1111111A",
                    ""));

			recordRepository.save(new Record(Type.ILLNESS,
					Subtype.CANCER,
					"foo",
					"linkToP01Record",
					"S1111111A",
                    ""));

			recordRepository.save(new Record(Type.ILLNESS,
					Subtype.ALLERGY,
					"foo",
					"linkToP01Record",
					"S1111111B",
                    ""));

			recordRepository.save(new Record(Type.ILLNESS,
					Subtype.CANCER,
					"foo",
					"linkToP01Record",
					"S1111111B",
                    ""));

			recordRepository.save(new Record(Type.ILLNESS,
					Subtype.COLD,
					"foo",
					"linkToP01Record",
					"S1111111C",
                    ""));

			recordRepository.save(new Record(Type.ILLNESS,
					Subtype.DIABETES,
					"foo",
					"linkToP01Record",
					"S1111111C",
                    ""));

			recordRepository.save(new Record(Type.ILLNESS,
					Subtype.COLD,
					"foo",
					"linkToP01Record",
					"S1111111D",
                    ""));

			recordRepository.save(new Record(Type.ILLNESS,
					Subtype.DIABETES,
					"foo",
					"linkToP01Record",
					"S1111111D",
                    ""));

			recordRepository.save(new Record(Type.ILLNESS,
					Subtype.HEADACHES_AND_MIGRAINES,
					"foo",
					"linkToP01Record",
					"S1111111E",
                    ""));

			recordRepository.save(new Record(Type.ILLNESS,
					Subtype.HYPERTENSION,
					"foo",
					"linkToP01Record",
					"S1111111E",
                    ""));

			recordRepository.save(new Record(Type.ILLNESS,
					Subtype.HEADACHES_AND_MIGRAINES,
					"foo",
					"linkToP01Record",
					"S1111111F",
                    ""));

			recordRepository.save(new Record(Type.ILLNESS,
					Subtype.HYPERTENSION,
					"foo",
					"linkToP01Record",
					"S1111111F",
                    ""));

			recordRepository.save(new Record(Type.ILLNESS,
					Subtype.HEADACHES_AND_MIGRAINES,
					"foo",
					"linkToP01Record",
					"S1111111G",
                    ""));

			recordRepository.save(new Record(Type.ILLNESS,
					Subtype.HYPERTENSION,
					"foo",
					"linkToP01Record",
					"S1111111G",
                    ""));

			recordRepository.save(new Record(Type.READING,
					Subtype.BLOOD_PRESSURE,
					"foo",
					"bp1.csv",
					"S1111111A",
                    ""));

			recordRepository.save(new Record(Type.READING,
					Subtype.BLOOD_PRESSURE,
					"foo",
					"bp1.csv",
					"S1111111B",
                    ""));

			recordRepository.save(new Record(Type.READING,
					Subtype.BLOOD_PRESSURE,
					"foo",
					"bp1.csv",
					"S1111111C",
                    ""));

			recordRepository.save(new Record(Type.READING,
					Subtype.BLOOD_PRESSURE,
					"foo",
					"bp2.csv",
					"S1111111D",
                    ""));

			recordRepository.save(new Record(Type.READING,
					Subtype.BLOOD_PRESSURE,
					"foo",
					"bp2.csv",
					"S1111111E",
                    ""));

			recordRepository.save(new Record(Type.READING,
					Subtype.BLOOD_PRESSURE,
					"foo",
					"bp2.csv",
					"S1111111F",
                    ""));

			recordRepository.save(new Record(Type.READING,
					Subtype.BLOOD_PRESSURE,
					"foo",
					"bp2.csv",
					"S1111111G",
                    ""));
		};
	}
}
