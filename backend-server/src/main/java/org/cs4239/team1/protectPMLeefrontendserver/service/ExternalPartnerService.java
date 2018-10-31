package org.cs4239.team1.protectPMLeefrontendserver.service;

import org.cs4239.team1.protectPMLeefrontendserver.exception.BadRequestException;
import org.cs4239.team1.protectPMLeefrontendserver.exception.ResourceNotFoundException;
import org.cs4239.team1.protectPMLeefrontendserver.model.Gender;
import org.cs4239.team1.protectPMLeefrontendserver.model.Note;
import org.cs4239.team1.protectPMLeefrontendserver.model.Record;
import org.cs4239.team1.protectPMLeefrontendserver.model.Role;
import org.cs4239.team1.protectPMLeefrontendserver.model.Treatment;
import org.cs4239.team1.protectPMLeefrontendserver.model.User;
import org.cs4239.team1.protectPMLeefrontendserver.repository.NoteRepository;
import org.cs4239.team1.protectPMLeefrontendserver.repository.RecordRepository;
import org.cs4239.team1.protectPMLeefrontendserver.repository.TreatmentRepository;
import org.cs4239.team1.protectPMLeefrontendserver.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

import static org.cs4239.team1.protectPMLeefrontendserver.util.FormatDate.formatDate;

@Service
public class ExternalPartnerService {

    @Autowired
    private RecordRepository recordRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TreatmentRepository treatmentRepository;

    @Autowired
    private NoteRepository noteRepository;

    private static final Logger logger = LoggerFactory.getLogger(ExternalPartnerService.class);

    @PreAuthorize("hasRole('EXTERNAL_PARTNER')")
    public String externalPartnerFile(String recordsType, MultipartFile file) throws IOException {

        InputStream inputStream = file.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        switch (recordsType){
            case "records":
                //check input headers are correct.
                if (!bufferedReader.readLine().equals("type,subtype,title,document,patientIC")){
                    throw new BadRequestException("Bad column headers");
                }
                String newRecord;
                while ((newRecord = bufferedReader.readLine()) != null) {
                    String[] parts =  newRecord.split(",");

                    String type = parts[0];
                    String subtype = parts[1];
                    String title = parts[2];
                    String document = parts[3];
                    String patientNric =parts[4];

                    //No point taking in a record if it does not belong to any patient in this hospital
                    User patient = userRepository.findByNric(patientNric)
                            .orElseThrow(() -> new ResourceNotFoundException("User", "id", patientNric));
                    if (!patient.getRoles().contains(Role.ROLE_PATIENT)){
                        throw new BadRequestException("User_" + patient.getNric() + " is not a patient!");
                    }

                    recordRepository.save(new Record(type,subtype,title,document,patientNric));
                }
                break;
            case "users":
                //check input headers are correct.
                if (!bufferedReader.readLine().equals("nric,name,email,phone,address,age,gender,password,publicKey,roles")){
                    throw new BadRequestException("Bad column headers");
                }
                String newUser;
                while ((newUser = bufferedReader.readLine()) != null) {
                    String[] parts = newUser.split(",");

                    if (userRepository.existsByNric(parts[0])) {
                        throw new BadRequestException("Nric is already taken!");
                    }

                    if (userRepository.existsByEmail(parts[2])) {
                        throw new BadRequestException("Email Address already in use!");
                    }

                    userRepository.save(new User(parts[0], //nric
                            parts[1], //name
                            parts[2], //email
                            parts[3], //phone
                            parts[4], //address
                            Integer.parseInt(parts[5]), //age
                            Gender.valueOf(parts[6].toUpperCase()), //gender
                            parts[7], //password Assume already encrypted?
                            parts[8], //key
                            new HashSet<>(Arrays.stream(parts[9] //roles
                                    .split("\\|"))
                                    .map(Role::create)
                                    .collect(Collectors.toList()))));
                }
                break;
            case "notes":
                //check input headers are correct.
                if (!bufferedReader.readLine().equals("creatorIC,patientIC,noteContent")){
                    throw new BadRequestException("Bad column headers");
                }
                String newNote;
                while ((newNote = bufferedReader.readLine()) != null) {
                    String[] parts =  newNote.split(",");

                    User creator = userRepository.findByNric(parts[0])
                            .orElseThrow(() -> new ResourceNotFoundException("User", "id", parts[0]));
                    if (!creator.getRoles().contains(Role.ROLE_PATIENT) || !creator.getRoles().contains(Role.ROLE_THERAPIST)){
                        throw new BadRequestException("User_" + creator.getNric() + " cannot create note!");
                    }

                    User patient = userRepository.findByNric(parts[1])
                            .orElseThrow(() -> new ResourceNotFoundException("User", "id", parts[1]));
                    if (!patient.getRoles().contains(Role.ROLE_PATIENT)){
                        throw new BadRequestException("User_" + patient.getNric() + " is not a patient!");
                    }

                    noteRepository.save(new Note(creator,
                            patient,
                            parts[2], //noteContent
                            //set Notes permissions to default
                            parts[0].equals(parts[1]), //isVisibleToPatient
                            !parts[0].equals(parts[1]))); //isVisibleToTherapist
                }
                break;
            case "treatments":
                //check input headers are correct.
                if (!bufferedReader.readLine().equals("therapistIC,patientIC,endDate")){
                    throw new BadRequestException("Bad column headers");
                }
                String newTreatment;
                while ((newTreatment = bufferedReader.readLine()) != null) {
                    String[] parts =  newTreatment.split(",");

                    //check if therapist exist
                    User therapist = userRepository.findByNric(parts[0])
                            .orElseThrow(() -> new ResourceNotFoundException("User", "id", parts[0]));
                    if (!therapist.getRoles().contains(Role.ROLE_THERAPIST)){
                        throw new BadRequestException("User_" + therapist.getNric() + " is not a therapist!");
                    }

                    //check if patient exist
                    User patient = userRepository.findByNric(parts[1])
                            .orElseThrow(() -> new ResourceNotFoundException("User", "id", parts[1]));
                    if (!patient.getRoles().contains(Role.ROLE_PATIENT)){
                        throw new BadRequestException("User_" + patient.getNric() + " is not a patient!");
                    }

                    treatmentRepository.save(new Treatment(therapist,
                            patient,
                            formatDate(parts[2])));
                }
                break;
        }

        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        return fileName;
    }
}
