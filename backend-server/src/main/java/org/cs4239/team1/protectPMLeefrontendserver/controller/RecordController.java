
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

import com.google.crypto.tink.subtle.Ed25519Sign;
import com.google.crypto.tink.subtle.Ed25519Verify;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.cs4239.team1.protectPMLeefrontendserver.exception.NonceExceededException;
import org.cs4239.team1.protectPMLeefrontendserver.exception.ResourceNotFoundException;
import org.cs4239.team1.protectPMLeefrontendserver.model.Permission;
import org.cs4239.team1.protectPMLeefrontendserver.model.Record;
import org.cs4239.team1.protectPMLeefrontendserver.model.Treatment;
import org.cs4239.team1.protectPMLeefrontendserver.model.TreatmentId;
import org.cs4239.team1.protectPMLeefrontendserver.model.User;
import org.cs4239.team1.protectPMLeefrontendserver.payload.*;
import org.cs4239.team1.protectPMLeefrontendserver.repository.TreatmentRepository;
import org.cs4239.team1.protectPMLeefrontendserver.repository.UserRepository;
import org.cs4239.team1.protectPMLeefrontendserver.security.AESEncryptionDecryptionTool;
import org.cs4239.team1.protectPMLeefrontendserver.security.CurrentUser;
import org.cs4239.team1.protectPMLeefrontendserver.security.Hasher;
import org.cs4239.team1.protectPMLeefrontendserver.security.NonceGenerator;
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

    @Value("${bluetooth.tag.encryptionKey}")
    private String tagKey;

    @Autowired
    private AESEncryptionDecryptionTool aesEncryptionDecryptionTool;


    private static final Logger logger = LoggerFactory.getLogger(RecordController.class);

    @PostMapping("/create/")
    public ResponseEntity<?> createRecord(@RequestPart(value = "recordRequest") String recordRequest,
            @RequestPart(value = "file") MultipartFile file) {
        try {
            RecordRequest recordRequest1 = validate(new ObjectMapper().readValue(recordRequest, RecordRequest.class));
            Record record = createRecord(recordRequest1, file);

            //Auto permitted when therapist create record for patient( i.e Default permission after creation is allowed)
            //Need to be assigned to start treatment. Else record will be created but not auto permitted
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
                                          @RequestPart(value = "file", required = false) MultipartFile file) {

        try {
            RecordRequest recordRequest1 = validate(new ObjectMapper().readValue(recordRequest, RecordRequest.class));
            Record record = createRecord(recordRequest1, file);
            int nonceInServer = NonceGenerator.generateNonce(record.getPatientIC());
            byte[] msgHash = Hasher.hash(nonceInServer);
            byte[] fileBytesHash = Hasher.hash(file.getBytes());

            byte[] ivBytes = new byte[16];
            SecureRandom.getInstanceStrong().nextBytes(ivBytes);
            byte[] uploadCode = Integer.toString(1).getBytes();
            byte[] combinedNonceAndFile = new byte[msgHash.length + fileBytesHash.length];
            System.arraycopy(msgHash, 0, combinedNonceAndFile, 0, msgHash.length);
            System.arraycopy(fileBytesHash, 0, combinedNonceAndFile, msgHash.length, fileBytesHash.length);
            byte[] encrypted = new AESEncryptionDecryptionTool().encrypt(combinedNonceAndFile, tagKey, ivBytes, "AES/CBC/NOPADDING");

            byte[] combined = new byte[uploadCode.length + ivBytes.length + encrypted.length];
            System.arraycopy(uploadCode, 0, combined, 0, uploadCode.length);
            System.arraycopy(ivBytes, 0, combined, uploadCode.length, ivBytes.length);
            System.arraycopy(encrypted, 0, combined, uploadCode.length+ivBytes.length, encrypted.length);

            Ed25519Sign signer = new Ed25519Sign(Base64.getDecoder().decode(privateKey));
            byte[] signature = signer.sign(combined);

            return new ResponseEntity<>(new ServerSignatureResponse(ivBytes, combined, signature), HttpStatus.OK);

        } catch (NonceExceededException nce) {
            return new ResponseEntity<>(new ApiResponse(false, "Number of nonces requested for the day exceeded."), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            throw new AssertionError("Errors should not happen.");
        }
    }


    //TODO: not done
    @PostMapping("/create/signature/verify")
    public ResponseEntity<?> verifyCreateRecordTagSignature(@RequestPart(value = "recordRequest") String recordRequest,
                                                            @RequestPart(value = "file", required = false) MultipartFile file,
                                                            @RequestPart(value = "signatureRequest") String sigRequest) {

        try {
            RecordRequest recordRequest1 = validate(new ObjectMapper().readValue(recordRequest, RecordRequest.class));
            Record record = createRecord(recordRequest1, file);
            RecordSignatureRequest recordSigRequest = validate(new ObjectMapper().readValue(sigRequest, RecordSignatureRequest.class));

            byte[] decrypted = aesEncryptionDecryptionTool
                    .decrypt(Base64.getDecoder().decode(recordSigRequest.getEncryptedString()), tagKey, recordSigRequest.getIv(), "AES/CBC/NOPADDING");
            byte[] msgHash = Arrays.copyOfRange(decrypted, 0, 64);
            byte[] fileSignature = Arrays.copyOfRange(decrypted, 64, 128);
            byte[] fileBytesHash = Hasher.hash(file.getBytes());
            System.out.println(record.getPatientIC());
            byte[] verifyHash = Hasher.hash(NonceGenerator.getNonce(record.getPatientIC()));


            //TODO: err not sure am i supposed to throw it here like that?
            if (!Arrays.equals(msgHash, verifyHash)) {
                throw new GeneralSecurityException();
            }

            //TODO: need you to help me call user.getPublicKey or something...
            Ed25519Verify verifier = new Ed25519Verify(Base64.getDecoder().decode("MW6ID/qlELbKxjap8tpzKRHmhhHwZ2w2GLp+vQByqss="));
            verifier.verify(fileSignature, fileBytesHash);

            //Auto permitted when therapist create record for patient( i.e Default permission after creation is allowed)
            //Need to be assigned to start treatment. Else record will be created but not auto permitted
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

    private Record createRecord(RecordRequest recordRequest, MultipartFile file) throws FileUploadException {
        if (file == null) {
            return recordService.createRecord(recordRequest, "");
        } else {
            String fileName = fileStorageService.storeFile(file, recordRequest.getPatientIC());
            return recordService.createRecord(recordRequest, fileName);
        }
    }

    //Get all records
    @GetMapping
    public PagedResponse<Record> getRecords(@CurrentUser User currentUser) {
        return recordService.getAllRecords(currentUser, 0, 30);
    }


    //Get specific records by RecordID
    @GetMapping("/recordid/{recordId}")
    public Record getRecordByRecordID(@CurrentUser User currentUser,
                                              @PathVariable Long recordId) {
        return recordService.getRecordByRecordID(recordId);
    }

    @GetMapping("/therapist/")
    public PagedResponse<Record> getRecordByTherapist(@CurrentUser User currentUser) {
        return recordService.getRecordsCreatedBy(currentUser, 0, 30);
    }

    @GetMapping("/patient/")
    public PagedResponse<Record> getRecordByPatient(@CurrentUser User currentUser) {
        return recordService.getRecordsBelongingTo(currentUser, 0, 30);
    }

    //Therapist get patient-specific permitted records
    @GetMapping("/therapist/patient/{patient}")
    public PagedResponse<Record> getRecordsPermittedByPatient(@CurrentUser User currentUser,
                                                                      @PathVariable String patient) {
        return recordService.getRecordsPermittedByPatient(currentUser, patient, 0, 30);
    }
}
