
package org.cs4239.team1.protectPMLeefrontendserver.controller;

import java.io.IOException;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;

import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.cs4239.team1.protectPMLeefrontendserver.exception.BadRequestException;
import org.cs4239.team1.protectPMLeefrontendserver.exception.ResourceNotFoundException;
import org.cs4239.team1.protectPMLeefrontendserver.model.Permission;
import org.cs4239.team1.protectPMLeefrontendserver.model.Record;
import org.cs4239.team1.protectPMLeefrontendserver.model.Role;
import org.cs4239.team1.protectPMLeefrontendserver.model.Treatment;
import org.cs4239.team1.protectPMLeefrontendserver.model.TreatmentId;
import org.cs4239.team1.protectPMLeefrontendserver.model.User;
import org.cs4239.team1.protectPMLeefrontendserver.payload.ApiResponse;
import org.cs4239.team1.protectPMLeefrontendserver.payload.PagedResponse;
import org.cs4239.team1.protectPMLeefrontendserver.payload.PermissionRequest;
import org.cs4239.team1.protectPMLeefrontendserver.payload.RecordRequest;
import org.cs4239.team1.protectPMLeefrontendserver.payload.RecordSignatureRequest;
import org.cs4239.team1.protectPMLeefrontendserver.payload.ServerSignatureResponse;
import org.cs4239.team1.protectPMLeefrontendserver.repository.TreatmentRepository;
import org.cs4239.team1.protectPMLeefrontendserver.repository.UserRepository;
import org.cs4239.team1.protectPMLeefrontendserver.security.AESEncryptionDecryptionTool;
import org.cs4239.team1.protectPMLeefrontendserver.security.CurrentUser;
import org.cs4239.team1.protectPMLeefrontendserver.security.Hasher;
import org.cs4239.team1.protectPMLeefrontendserver.service.FileStorageService;
import org.cs4239.team1.protectPMLeefrontendserver.service.RecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.crypto.tink.subtle.Ed25519Sign;
import com.google.crypto.tink.subtle.Ed25519Verify;

@RestController
@RequestMapping("/api/records")
public class RecordController {

    @Autowired
    private TreatmentRepository treatmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecordService recordService;

    @Autowired
    private FileStorageService fileStorageService;

    @Value("${app.privateKey}")
    private String privateKey;

    @Autowired
    private AESEncryptionDecryptionTool aesEncryptionDecryptionTool;


    private static final Logger logger = LoggerFactory.getLogger(RecordController.class);

    @PostMapping("/create/")
    public ResponseEntity<?> createRecord(@RequestPart(value = "recordRequest") String recordRequest,
            @RequestPart(value = "file") MultipartFile file, @CurrentUser User therapist) {
        logger.info("NRIC_" + therapist.getNric() + " ROLE_" + therapist.getSelectedRole() + " accessing RecordController#createRecord", recordRequest, file);
        try {
            RecordRequest recordRequest1 = validate(new ObjectMapper().readValue(recordRequest, RecordRequest.class));
            Record record = createRecord(therapist, recordRequest1, file);

            //Auto permitted when therapist create record for patient( i.e Default permission after creation is allowed)
            Treatment treatment = treatmentRepository.findByTreatmentId(new TreatmentId(record.getCreatedBy(),record.getPatientIC()));
            String endDate = treatment.getEndDate().toString().substring(0,10);
            PermissionRequest permissionRequest = new PermissionRequest(record.getRecordID(), record.getCreatedBy(), endDate);
            User patient = userRepository.findByNric(record.getPatientIC())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "nric", record.getPatientIC()));

            Permission permission = recordService.grantPermission(permissionRequest, patient);

            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest().path("/{recordId}")
                    .buildAndExpand(record.getRecordID()).toUri();

            return ResponseEntity.created(location)
                    .body(new ApiResponse(true, "Record Created Successfully"));
        } catch (FileUploadException fue) {
            return new ResponseEntity<>(new ApiResponse(false, "Invalid file type."), HttpStatus.BAD_REQUEST);
        } catch (JsonParseException | JsonMappingException je) {
            return new ResponseEntity<>(new ApiResponse(false, "Invalid JSON."), HttpStatus.BAD_REQUEST);
        } catch (IOException ioe) {
            throw new AssertionError("Not expected to happen.");
        }
    }

    @PostMapping("/create/signature")
    public ResponseEntity<?> createRecordSignature(@RequestPart(value = "recordRequest") String recordRequest,
                                          @RequestPart(value = "file") MultipartFile file, @CurrentUser User currentUser) {
        logger.info("NRIC_" + currentUser.getNric() + " ROLE_" + currentUser.getSelectedRole() + " accessing RecordController#createRecordSignature", recordRequest, file);
        try {
            RecordRequest recordRequest1 = validate(new ObjectMapper().readValue(recordRequest, RecordRequest.class));
            User patient = userRepository.findByNric(recordRequest1.getPatientIC())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "nric", recordRequest1.getPatientIC()));
            if (!patient.getRoles().contains(Role.ROLE_PATIENT)){
                throw new BadRequestException("User_" + patient.getNric() + " is not a patient!");
            }

            int nonce = patient.getNonce();
            if (nonce > 30) {
                return new ResponseEntity<>(new ApiResponse(false, "Number of nonces requested for the day exceeded."), HttpStatus.BAD_REQUEST);
            }
            byte[] msgHash = Hasher.hash(nonce);
            byte[] fileBytesHash = Hasher.hash(file.getBytes());

            byte[] ivBytes = new byte[16];
            SecureRandom.getInstanceStrong().nextBytes(ivBytes);
            byte[] uploadCode = Integer.toString(1).getBytes();
            byte[] combinedNonceAndFile = new byte[msgHash.length + fileBytesHash.length];
            System.arraycopy(msgHash, 0, combinedNonceAndFile, 0, msgHash.length);
            System.arraycopy(fileBytesHash, 0, combinedNonceAndFile, msgHash.length, fileBytesHash.length);
            byte[] encrypted = new AESEncryptionDecryptionTool().encrypt(combinedNonceAndFile, patient.getSymmetricKey(), ivBytes, "AES/CBC/NOPADDING");
            byte[] pubKey = Base64.getDecoder().decode(patient.getPublicKey());
            byte[] combined = new byte[uploadCode.length + pubKey.length + ivBytes.length + encrypted.length];
            System.arraycopy(uploadCode, 0, combined, 0, uploadCode.length);
            System.arraycopy(pubKey, 0, combined, uploadCode.length, pubKey.length);
            System.arraycopy(ivBytes, 0, combined, uploadCode.length+pubKey.length, ivBytes.length);
            System.arraycopy(encrypted, 0, combined, uploadCode.length+pubKey.length+ivBytes.length, encrypted.length);

            Ed25519Sign signer = new Ed25519Sign(Base64.getDecoder().decode(privateKey));
            byte[] signature = signer.sign(combined);

            return new ResponseEntity<>(new ServerSignatureResponse(ivBytes, combined, signature), HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            throw new AssertionError("Errors should not happen.");
        }
    }


    @PostMapping("/create/signature/verify")
    public ResponseEntity<?> verifyCreateRecordTagSignature(@CurrentUser User therapist,
                                                            @RequestPart(value = "recordRequest") String recordRequest,
                                                            @RequestPart(value = "file") MultipartFile file,
                                                            @RequestPart(value = "signatureRequest") String sigRequest,
                                                            @CurrentUser User currentUser) {
        logger.info("NRIC_" + currentUser.getNric() + " ROLE_" + currentUser.getSelectedRole() + " accessing RecordController#verifyCreateRecordTagSignature", recordRequest, file, sigRequest);
        try {
            RecordRequest recordRequest1 = validate(new ObjectMapper().readValue(recordRequest, RecordRequest.class));
            RecordSignatureRequest recordSigRequest = validate(new ObjectMapper().readValue(sigRequest, RecordSignatureRequest.class));
            User patient = userRepository.findByNric(recordRequest1.getPatientIC())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "nric", recordRequest1.getPatientIC()));
            if (!patient.getRoles().contains(Role.ROLE_PATIENT)){
                throw new BadRequestException("User_" + patient.getNric() + " is not a patient!");
            }

            byte[] decrypted = aesEncryptionDecryptionTool
                    .decrypt(Base64.getDecoder().decode(recordSigRequest.getEncryptedString()), patient.getSymmetricKey(), recordSigRequest.getIv(), "AES/CBC/NOPADDING");
            byte[] msgHash = Arrays.copyOfRange(decrypted, 0, 64);
            byte[] fileSignature = Arrays.copyOfRange(decrypted, 64, 128);
            byte[] fileBytesHash = Hasher.hash(file.getBytes());
            byte[] verifyHash = Hasher.hash(patient.getNonce());


            if (!Arrays.equals(msgHash, verifyHash)) {
                throw new GeneralSecurityException();
            }

            Ed25519Verify verifier = new Ed25519Verify(Base64.getDecoder().decode(patient.getPublicKey()));
            verifier.verify(fileSignature, fileBytesHash);
            String fileSignatureStr = new String(Base64.getEncoder().encode(fileSignature));
            patient.setNonce(patient.getNonce() + 1);
            patient.setNumOfNonceUsed(patient.getNumOfNonceUsed() + 1);
            userRepository.save(patient);
            Record record = createRecordWithSignature(therapist, recordRequest1, file, fileSignatureStr);

            //Auto permitted when therapist create record for patient( i.e Default permission after creation is allowed)
            Treatment treatment = treatmentRepository.findByTreatmentId(new TreatmentId(record.getCreatedBy(),record.getPatientIC()));
            String endDate = treatment.getEndDate().toString().substring(0,10);
            PermissionRequest permissionRequest = new PermissionRequest(record.getRecordID(), record.getCreatedBy(), endDate);

            Permission permission = recordService.grantPermission(permissionRequest, patient);

            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest().path("/{recordId}")
                    .buildAndExpand(record.getRecordID()).toUri();

            return ResponseEntity.created(location)
                    .body(new ApiResponse(true, "Record Created Successfully"));

            } catch (FileUploadException fue) {
                return new ResponseEntity<>(new ApiResponse(false, "Invalid file type."), HttpStatus.BAD_REQUEST);
            } catch (JsonParseException | JsonMappingException je) {
                return new ResponseEntity<>(new ApiResponse(false, "Invalid JSON."), HttpStatus.BAD_REQUEST);
            } catch (IOException | GeneralSecurityException ioe) {
                throw new AssertionError("Not expected to happen.");
            }
    }

    private <T> T validate(T input) {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<T>> constraintViolations = validator.validate(input);
        if (constraintViolations.isEmpty()) {
            return input;
        } else {
            throw new ConstraintViolationException(constraintViolations);
        }
    }

    private Record createRecord(User therapist, RecordRequest recordRequest, MultipartFile file) throws FileUploadException {
        String fileName = fileStorageService.storeFile(file, recordRequest.getPatientIC());
        return recordService.createRecord(therapist, recordRequest, fileName);
    }

    private Record createRecordWithSignature(User therapist, RecordRequest recordRequest, MultipartFile file, String signature) throws FileUploadException {
        String fileName = fileStorageService.storeFile(file, recordRequest.getPatientIC());
        return recordService.createRecordWithSignature(therapist, recordRequest, fileName, signature);
    }

    @GetMapping("/patient/")
    public PagedResponse<Record> getRecordByPatient(@CurrentUser User currentUser) {
        logger.info("NRIC_" + currentUser.getNric() + " ROLE_" + currentUser.getSelectedRole() + " accessing RecordController#getRecordByPatient");
        return recordService.getRecordsBelongingTo(currentUser);
    }

    //Therapist get patient-specific permitted records
    @GetMapping("/therapist/patient/{patient}")
    public PagedResponse<Record> getRecordsPermittedByPatient(@CurrentUser User currentUser,
                                                                      @PathVariable String patient) {
        logger.info("NRIC_" + currentUser.getNric() + " ROLE_" + currentUser.getSelectedRole() + " accessing RecordController#getRecordsPermittedByPatient", patient);
        return recordService.getRecordsPermittedByPatient(currentUser, patient);
    }
}
