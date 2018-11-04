package org.cs4239.team1.protectPMLeefrontendserver.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import com.google.crypto.tink.subtle.Ed25519Verify;
import org.apache.commons.io.IOUtils;
import org.cs4239.team1.protectPMLeefrontendserver.exception.BadRequestException;
import org.cs4239.team1.protectPMLeefrontendserver.exception.ResourceNotFoundException;
import org.cs4239.team1.protectPMLeefrontendserver.exception.UnauthorisedException;
import org.cs4239.team1.protectPMLeefrontendserver.model.Permission;
import org.cs4239.team1.protectPMLeefrontendserver.model.PermissionId;
import org.cs4239.team1.protectPMLeefrontendserver.model.Record;
import org.cs4239.team1.protectPMLeefrontendserver.model.Role;
import org.cs4239.team1.protectPMLeefrontendserver.model.Subtype;
import org.cs4239.team1.protectPMLeefrontendserver.model.Treatment;
import org.cs4239.team1.protectPMLeefrontendserver.model.User;
import org.cs4239.team1.protectPMLeefrontendserver.model.Type;
import org.cs4239.team1.protectPMLeefrontendserver.payload.EndPermissionRequest;
import org.cs4239.team1.protectPMLeefrontendserver.payload.PagedResponse;
import org.cs4239.team1.protectPMLeefrontendserver.payload.PermissionRequest;
import org.cs4239.team1.protectPMLeefrontendserver.payload.RecordRequest;
import org.cs4239.team1.protectPMLeefrontendserver.payload.RecordResponseWithTherapistIdentifier;
import org.cs4239.team1.protectPMLeefrontendserver.repository.PermissionRepository;
import org.cs4239.team1.protectPMLeefrontendserver.repository.RecordRepository;
import org.cs4239.team1.protectPMLeefrontendserver.repository.TreatmentRepository;
import org.cs4239.team1.protectPMLeefrontendserver.repository.UserRepository;
import org.cs4239.team1.protectPMLeefrontendserver.security.Hasher;
import org.cs4239.team1.protectPMLeefrontendserver.util.FormatDate;
import org.cs4239.team1.protectPMLeefrontendserver.util.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class RecordService {

    @Autowired
    private RecordRepository recordRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    TreatmentRepository treatmentRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    private static final Logger logger = LoggerFactory.getLogger(RecordService.class);


    @PreAuthorize("hasRole('THERAPIST')")
    public PagedResponse<Record> getRecordsCreatedBy(User currentUser) {

        // Retrieve all records created by the current user
        Pageable pageable = PageRequest.of(0,60, Sort.Direction.DESC, "createdAt");
        Page<Record> records = recordRepository.findByCreatedBy(currentUser.getNric(), pageable);
        //Todo can only view created records of patient he is still treating

        if (records.getNumberOfElements() == 0) {
            return new PagedResponse<>(Collections.emptyList(), records.getNumber(),
                    records.getSize(), records.getTotalElements(), records.getTotalPages(), records.isLast());
        }

        return new PagedResponse<>(records.getContent(), records.getNumber(),
                records.getSize(), records.getTotalElements(), records.getTotalPages(), records.isLast());
    }

    @PreAuthorize("hasRole('PATIENT')")
    public PagedResponse<Record> getRecordsBelongingTo(User currentUser) {

        // Retrieve all records belong to the given nric
        Pageable pageable = PageRequest.of(0,60, Sort.Direction.DESC, "createdAt");
        Page<Record> records = recordRepository.findByPatientIC(currentUser.getNric(), pageable);

        if (records.getNumberOfElements() == 0) {
            return new PagedResponse<>(Collections.emptyList(), records.getNumber(),
                    records.getSize(), records.getTotalElements(), records.getTotalPages(), records.isLast());
        }

        return new PagedResponse<>(records.getContent(), records.getNumber(),
                records.getSize(), records.getTotalElements(), records.getTotalPages(), records.isLast());
    }

    @PreAuthorize("hasRole('THERAPIST')")
    public Record createRecord(User therapist, RecordRequest recordRequest, String fileName) {

        //check if user exist
        User user = userRepository.findByNric(recordRequest.getPatientIC())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", recordRequest.getPatientIC()));
        //check if user has role patient
        if (!user.getRoles().contains(Role.ROLE_PATIENT)){
            throw new BadRequestException("User_" + user.getNric() + " is not a patient!");
        }

        Treatment treatment = treatmentRepository.findByTherapistAndPatient(therapist, user)
                .orElseThrow(() -> new UnauthorisedException("Not allowed to create this record"));

        return recordRepository.save(new Record(Type.create(recordRequest.getType()),
                Subtype.create(recordRequest.getSubtype()),
                recordRequest.getTitle(),
                fileName,
                recordRequest.getPatientIC(), ""));
    }

    @PreAuthorize("hasRole('THERAPIST')")
    public Record createRecordWithSignature(User therapist, RecordRequest recordRequest, String fileName, String signature) {

        //check if user exist
        User user = userRepository.findByNric(recordRequest.getPatientIC())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", recordRequest.getPatientIC()));
        //check if user has role patient
        if (!user.getRoles().contains(Role.ROLE_PATIENT)){
            throw new BadRequestException("User_" + user.getNric() + " is not a patient!");
        }

        Treatment treatment = treatmentRepository.findByTherapistAndPatient(therapist, user)
                .orElseThrow(() -> new UnauthorisedException("Not allowed to create this record"));

        return recordRepository.save(new Record(Type.create(recordRequest.getType()),
                Subtype.create(recordRequest.getSubtype()),
                recordRequest.getTitle(),
                fileName,
                recordRequest.getPatientIC(),
                signature));
    }


    @PreAuthorize("hasRole('PATIENT') or hasRole('THERAPIST') or hasRole('ADMINISTRATOR') or hasRole('EXTERNAL_PARTNER')")

    public Permission grantPermission(PermissionRequest permissionRequest, User currentUser){

        Record record = recordRepository.findByRecordID(permissionRequest.getRecordID()).orElseThrow(
                () -> new ResourceNotFoundException("Record", "id", permissionRequest.getRecordID()));
        User user = userRepository.findByNric(permissionRequest.getTherapistNric())
                .orElseThrow(() -> new ResourceNotFoundException("User", "nric", permissionRequest.getTherapistNric()));
        String patientIC = currentUser.getNric();

        if(!record.getPatientIC().equals(patientIC)){
            throw new BadRequestException("You do not have permission to grant record " + record.getRecordID());
        }
        Instant expirationDateTime = FormatDate.formatDate(permissionRequest.getEndDate());

        Permission permission = new Permission(record,user, expirationDateTime, patientIC);

        if (permissionRepository.findByPermissionID(permission.getPermissionID()) != null){
            throw new BadRequestException(record.getRecordID() + " has already been granted");
        }

        return permissionRepository.save(permission);
    }

    @PreAuthorize("hasRole('PATIENT')")
    public void revokePermission(EndPermissionRequest permissionRequest, User currentUser){

        Record record = recordRepository.findByRecordID(permissionRequest.getRecordID()).orElseThrow(
                () -> new ResourceNotFoundException("Record", "id", permissionRequest.getRecordID()));
        User therapist = userRepository.findByNric(permissionRequest.getTherapistNric())
                .orElseThrow(() -> new ResourceNotFoundException("User", "nric", permissionRequest.getTherapistNric()));

        if(!record.getPatientIC().equals(currentUser.getNric())){
            throw new BadRequestException("You do not have permission to revoke record " + record.getRecordID());
        }

        PermissionId permissionId = new PermissionId(permissionRequest.getRecordID(),permissionRequest.getTherapistNric());

        if (permissionRepository.findByPermissionID(permissionId) == null){
            throw new BadRequestException(record.getRecordID() + " has not been granted");
        }

        permissionRepository.deleteById(permissionId);
    }

    @PreAuthorize("hasRole('THERAPIST')")
    public PagedResponse<Record> getAllowedRecords(User currentUser) {

        // Retrieve all records belong to the given nric
        Pageable pageable = PageRequest.of(0,60, Sort.Direction.DESC, "createdAt");
        Page<Permission> permission = permissionRepository.findByUser(currentUser, pageable);

        if (permission.getNumberOfElements() == 0) {
            return new PagedResponse<>(Collections.emptyList(), permission.getNumber(),
                    permission.getSize(), permission.getTotalElements(), permission.getTotalPages(), permission.isLast());
        }

        // Map Permissions to Records
        List<Record> records = permission.map(ModelMapper::mapPermissionToRecord).getContent();

        return new PagedResponse<>(records, permission.getNumber(),
                permission.getSize(), permission.getTotalElements(), permission.getTotalPages(), permission.isLast());
    }

    @PreAuthorize("hasRole('PATIENT')")
    public PagedResponse<RecordResponseWithTherapistIdentifier> getGivenRecords(User patient) {

        // Retrieve all records belong to the given nric
        Pageable pageable = PageRequest.of(0,60, Sort.Direction.DESC, "createdAt");
        Page<Permission> permission = permissionRepository.findByPatientNric(patient.getNric(), pageable);

        if (permission.getNumberOfElements() == 0) {
            return new PagedResponse<>(Collections.emptyList(), permission.getNumber(),
                    permission.getSize(), permission.getTotalElements(), permission.getTotalPages(), permission.isLast());
        }

        // Map Records to RecordResponses
        List<RecordResponseWithTherapistIdentifier> recordResponses = permission.map(
                ModelMapper::mapRecordToRecordResponseWithTherapistIdentifier).getContent();

        return new PagedResponse<>(recordResponses, permission.getNumber(),
                permission.getSize(), permission.getTotalElements(), permission.getTotalPages(), permission.isLast());
    }

    @PreAuthorize("hasRole('THERAPIST')")
    public PagedResponse<Record> getRecordsPermittedByPatient(User currentUser, String patientNric) {
        // Retrieve all records that matches the user and patient Nric pair
        Pageable pageable = PageRequest.of(0,60, Sort.Direction.DESC, "createdAt");
        Page<Permission> permission = permissionRepository.findByUserAndPatientNric(currentUser, patientNric, pageable);

        if (permission.getNumberOfElements() == 0) {
            return new PagedResponse<>(Collections.emptyList(), permission.getNumber(),
                    permission.getSize(), permission.getTotalElements(), permission.getTotalPages(), permission.isLast());
        }

        // Map Permissions to Records
        List<Record> records = permission.map(ModelMapper::mapPermissionToRecord).getContent();

        return new PagedResponse<>(records, permission.getNumber(),
                permission.getSize(), permission.getTotalElements(), permission.getTotalPages(), permission.isLast());
    }

    @Scheduled(fixedRate = 1000 * 60 * 60 * 24)
    private void validateSignatures() {
        List<Record> records = recordRepository.findAll();
        for (Record rec : records) {
            try {
                if (rec.getFileSignature().length()>0) {
                    byte[] fileBytes = IOUtils.toByteArray(fileStorageService.loadFileAsResource(rec.getDocument()).getInputStream());
                    byte[] fileBytesHash = Hasher.hash(fileBytes);
                    User patient = userRepository.findByNric(rec.getPatientIC())
                            .orElseThrow(() -> new ResourceNotFoundException("User", "nric", rec.getPatientIC()));
                    Ed25519Verify verifier = new Ed25519Verify(Base64.getDecoder().decode(patient.getPublicKey()));
                    verifier.verify(Base64.getDecoder().decode(rec.getFileSignature()), fileBytesHash);
                }
            } catch (IOException | GeneralSecurityException e) {
                logger.error("File Signature for " + rec.getPatientIC() + "not verified!", e);
                e.printStackTrace();
            }
        }
    }
}
