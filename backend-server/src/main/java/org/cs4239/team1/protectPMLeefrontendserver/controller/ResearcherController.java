package org.cs4239.team1.protectPMLeefrontendserver.controller;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.validation.Valid;

import org.cs4239.team1.protectPMLeefrontendserver.exception.BadRequestException;
import org.cs4239.team1.protectPMLeefrontendserver.model.Age;
import org.cs4239.team1.protectPMLeefrontendserver.model.BloodPressure;
import org.cs4239.team1.protectPMLeefrontendserver.model.Gender;
import org.cs4239.team1.protectPMLeefrontendserver.model.Location;
import org.cs4239.team1.protectPMLeefrontendserver.model.Record;
import org.cs4239.team1.protectPMLeefrontendserver.model.Subtype;
import org.cs4239.team1.protectPMLeefrontendserver.model.Type;
import org.cs4239.team1.protectPMLeefrontendserver.model.User;
import org.cs4239.team1.protectPMLeefrontendserver.model.Value;
import org.cs4239.team1.protectPMLeefrontendserver.payload.AnonymisedRecordRequest;
import org.cs4239.team1.protectPMLeefrontendserver.payload.AnonymisedRecordResponse;
import org.cs4239.team1.protectPMLeefrontendserver.repository.RecordRepository;
import org.cs4239.team1.protectPMLeefrontendserver.repository.UserRepository;
import org.cs4239.team1.protectPMLeefrontendserver.security.CurrentUser;
import org.cs4239.team1.protectPMLeefrontendserver.service.FileStorageService;
import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.AttributeType.Hierarchy.DefaultHierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.Data.DefaultData;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.aggregates.HierarchyBuilderIntervalBased;
import org.deidentifier.arx.criteria.KAnonymity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/researcher")
public class ResearcherController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecordRepository recordRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final Logger logger = LoggerFactory.getLogger(ResearcherController.class);

    @PostMapping("/getAnonymousData")
    public List<AnonymisedRecordResponse> getAnonymousData(@CurrentUser User currentUser, @Valid @RequestBody AnonymisedRecordRequest request) {
        logger.info("NRIC_" + currentUser.getNric() + " ROLE_" + currentUser.getSelectedRole() + " accessing ResearcherController#getAnonymousData", request);
        Value value = getFilter(Type.create(request.getType()), Subtype.create(request.getSubtype()));
        Data data = getData(request, value);
        return anonymize(data);
    }

    private Value getFilter(Type type, Subtype subtype) {
        if (type.equals(Type.ILLNESS) && !subtype.equals(Subtype.BLOOD_PRESSURE)) {
            return new Value(getIllness(), this::getIllness);
        } else if (type.equals(Type.READING) && subtype.equals(Subtype.BLOOD_PRESSURE)) {
            return new Value(getBloodPressure(), this::getBloodPressure);
        } else {
            throw new BadRequestException("Invalid Type and Subtype pairs.");
        }
    }

    private Data getData(@Valid @RequestBody AnonymisedRecordRequest request, Value value) {
        DefaultData data = DefaultData.create();
        data.add("location", "age", "gender", "value");

        data.getDefinition().setAttributeType("location", getLocation());
        data.getDefinition().setAttributeType("age", getAge());
        data.getDefinition().setAttributeType("gender", getGender());
        data.getDefinition().setAttributeType("value", value.getHierarchy());

        Location requestedLocation = Location.create(request.getLocation());
        Age requestedAge = Age.create(request.getAge());
        Gender requestedGender = Gender.create(request.getGender());
        Subtype requestedSubtype = Subtype.create(request.getSubtype());

        if (requestedSubtype.equals(Subtype.BLOOD_PRESSURE)) {
            userRepository.findAll().stream()
                    .filter(user -> requestedLocation == Location.ALL
                            || requestedLocation.isInLocation(Integer.valueOf(user.getPostalCode())))
                    .filter(user -> requestedAge == Age.ALL || requestedAge.isInRange(user.getAge()))
                    .filter(user -> requestedGender == Gender.ALL || requestedGender == user.getGender())
                    .forEach(user -> {
                        Record record = recordRepository.findByPatientIC(user.getNric()).stream()
                                .filter(r -> requestedSubtype == r.getSubtype())
                                .sorted(Comparator.comparing(Record::getCreatedAt))
                                .reduce((first, second) -> second)
                                .orElse(null);
                        if (record == null) {
                            return;
                        }
                        data.add(user.getPostalCode().substring(0, 2), String.valueOf(user.getAge()),
                                user.getGender().toString(), value.getValueSupplier().apply(record));
                    });
        } else {
            userRepository.findAll().stream()
                    .filter(user -> requestedLocation == Location.ALL
                            || requestedLocation.isInLocation(Integer.valueOf(user.getPostalCode())))
                    .filter(user -> requestedAge == Age.ALL || requestedAge.isInRange(user.getAge()))
                    .filter(user -> requestedGender == Gender.ALL || requestedGender == user.getGender())
                    .forEach(user -> recordRepository.findByPatientIC(user.getNric()).stream()
                            .filter(record -> (requestedSubtype == Subtype.ALL && record.getSubtype() != Subtype.BLOOD_PRESSURE)
                                    || requestedSubtype == record.getSubtype())
                            .forEach(record -> data.add(user.getPostalCode().substring(0, 2), String.valueOf(user.getAge()),
                                    user.getGender().toString(), value.getValueSupplier().apply(record))));
        }

        return data;
    }

    private List<AnonymisedRecordResponse> anonymize(Data toAnonymize) {
        if (toAnonymize.getHandle().getNumRows() == 0) {
            throw new BadRequestException("No data in the database for this request.");
        } else if (toAnonymize.getHandle().getNumRows() == 1) {
            throw new BadRequestException("Insufficient data in the database for this request.");
        }

        try {
            ARXConfiguration config = ARXConfiguration.create().addPrivacyModel(new KAnonymity(2));
            ARXResult result = new ARXAnonymizer().anonymize(toAnonymize, config);
            LinkedList<AnonymisedRecordResponse> response = new LinkedList<>();
            result.getOutput().iterator().forEachRemaining(array ->
                    response.add(new AnonymisedRecordResponse(array[0], array[1], array[2], array[3])));
            response.removeFirst();
            return response;
        } catch (IOException ioe) {
            throw new AssertionError("Should not happen.");
        }
    }

    private Hierarchy getBloodPressure() {
        DefaultHierarchy bpHierarchy = Hierarchy.create();

        for (int i = 0; i < 200; i++) {
            for (int j = 0; j < 200; j++) {
                BloodPressure bp = new BloodPressure(i, j);
                bpHierarchy.add(bp.toString(), bp.getCategory().toString(), "*");
            }
        }

        return bpHierarchy;
    }

    private HierarchyBuilderIntervalBased<Long> getLocation() {
        return HierarchyBuilderIntervalBased.create(DataType.INTEGER)
                .addInterval(1l, 14l, Location.SOUTH.toString())
                .addInterval(14l, 17l, Location.SOUTH_WEST.toString())
                .addInterval(17l, 31l, Location.CENTRAL.toString())
                .addInterval(31l, 38l, Location.NORTH_EAST.toString())
                .addInterval(38l, 53l, Location.EAST.toString())
                .addInterval(53l, 56l, Location.NORTH_EAST.toString())
                .addInterval(56l, 58l, Location.NORTH.toString())
                .addInterval(58l, 60l, Location.SOUTH_WEST.toString())
                .addInterval(60l, 65l, Location.WEST.toString())
                .addInterval(65l, 69l, Location.NORTH_WEST.toString())
                .addInterval(69l, 72l, Location.WEST.toString())
                .addInterval(72l, 75l, Location.NORTH_WEST.toString())
                .addInterval(75l, 77l, Location.NORTH_WEST.toString())
                .addInterval(77l, 81l, Location.NORTH.toString())
                .addInterval(81l, 82l, Location.EAST.toString())
                .addInterval(82l, 83l, Location.NORTH_EAST.toString());
    }

    private HierarchyBuilderIntervalBased<Long> getAge() {
        return HierarchyBuilderIntervalBased.create(DataType.INTEGER)
                .addInterval(1l, 13l, Age.BELOW_13.toString())
                .addInterval(13l, 19l, Age.FROM_13_TO_18.toString())
                .addInterval(19l, 26l, Age.FROM_19_TO_25.toString())
                .addInterval(26l, 36l, Age.FROM_26_TO_35.toString())
                .addInterval(36l, 56l, Age.FROM_36_TO_55.toString())
                .addInterval(56l, 121l, Age.ABOVE_55.toString());
    }

    private DefaultHierarchy getGender() {
        DefaultHierarchy gender = Hierarchy.create();
        gender.add("Male", "*");
        gender.add("Female", "*");

        return gender;
    }

    private Hierarchy getIllness() {
        DefaultHierarchy illness = Hierarchy.create();
        illness.add(Subtype.ALLERGY.toString(), "Common");
        illness.add(Subtype.COLD.toString(), "Common");
        illness.add(Subtype.HEADACHES_AND_MIGRAINES.toString(), "Common");
        illness.add(Subtype.ASTHMA.toString(), "Chronic");
        illness.add(Subtype.HIGH_BLOOD_CHOLESTEROL.toString(), "Chronic");
        illness.add(Subtype.CANCER.toString(), "Chronic");
        illness.add(Subtype.DIABETES.toString(), "Chronic");
        illness.add(Subtype.HEART_DISEASE.toString(), "Chronic");
        illness.add(Subtype.HYPERTENSION.toString(), "Chronic");
        illness.add(Subtype.STROKE.toString(), "Chronic");
        illness.add(Subtype.PANIC_ATTACK.toString(), "Mental");
        illness.add(Subtype.DEPRESSION.toString(), "Mental");
        illness.add(Subtype.EATING_DISORDERS.toString(), "Mental");
        illness.add(Subtype.OBSESSIVE_COMPULSIVE_DISORDER.toString(), "Mental");
        illness.add(Subtype.SCHIZOPHRENIA.toString(), "Mental");
        illness.add(Subtype.BRONCHITIS.toString(), "Physical");
        illness.add(Subtype.BACK_PAIN.toString(), "Physical");
        illness.add(Subtype.CATARACTS.toString(), "Physical");
        illness.add(Subtype.CARIES.toString(), "Physical");
        illness.add(Subtype.CHICKENPOX.toString(), "Physical");
        illness.add(Subtype.GINGIVITIS.toString(), "Physical");
        illness.add(Subtype.GOUT.toString(), "Physical");
        illness.add(Subtype.HAEMORRHOIDS.toString(), "Physical");
        illness.add(Subtype.URINARY.toString(), "Physical");

        return illness;
    }

    private String getIllness(Record record) {
        return record.getSubtype().toString();
    }

    private String getBloodPressure(Record record) {
        try (BufferedReader br = new BufferedReader(
                new FileReader(fileStorageService.loadFileAsResource(record.getDocument()).getFile()))) {
            String line;
            int avgSystolic = 0;
            int avgDiastolic = 0;
            int t = 1;
            while ((line = br.readLine()) != null) {
                BloodPressure pressure = BloodPressure.create(line.split(",")[1].trim());
                avgSystolic += (pressure.getSystolic() - avgSystolic) / t;
                avgDiastolic += (pressure.getDiastolic() - avgDiastolic) / t;
                t++;
            }

            return avgSystolic + "/" + avgDiastolic;
        } catch (IOException ioe) {
            throw new AssertionError("Should not happen.");
        }
    }
}
