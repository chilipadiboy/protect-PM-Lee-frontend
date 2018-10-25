
package org.cs4239.team1.protectPMLeefrontendserver.controller;

import java.io.IOException;
import java.net.URI;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;

import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.cs4239.team1.protectPMLeefrontendserver.exception.ResourceNotFoundException;
import org.cs4239.team1.protectPMLeefrontendserver.model.Permission;
import org.cs4239.team1.protectPMLeefrontendserver.model.Record;
import org.cs4239.team1.protectPMLeefrontendserver.model.Treatment;
import org.cs4239.team1.protectPMLeefrontendserver.model.TreatmentId;
import org.cs4239.team1.protectPMLeefrontendserver.model.User;
import org.cs4239.team1.protectPMLeefrontendserver.payload.ApiResponse;
import org.cs4239.team1.protectPMLeefrontendserver.payload.PagedResponse;
import org.cs4239.team1.protectPMLeefrontendserver.payload.PermissionRequest;
import org.cs4239.team1.protectPMLeefrontendserver.payload.RecordRequest;
import org.cs4239.team1.protectPMLeefrontendserver.repository.TreatmentRepository;
import org.cs4239.team1.protectPMLeefrontendserver.repository.UserRepository;
import org.cs4239.team1.protectPMLeefrontendserver.security.CurrentUser;
import org.cs4239.team1.protectPMLeefrontendserver.service.FileStorageService;
import org.cs4239.team1.protectPMLeefrontendserver.service.RecordService;
import org.cs4239.team1.protectPMLeefrontendserver.util.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    private static final Logger logger = LoggerFactory.getLogger(RecordController.class);

    @PostMapping("/create/")
    public ResponseEntity<?> createRecord(@RequestParam(value = "recordRequest") String recordRequest,
            @RequestParam(value = "file", required = false) MultipartFile file) {
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

    private RecordRequest validate(RecordRequest input) {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<RecordRequest>> constraintViolations = validator.validate(input);
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
    public PagedResponse<Record> getRecords(@CurrentUser User currentUser,
                                                    @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                                    @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return recordService.getAllRecords(currentUser, page, size);
    }


    //Get specific records by RecordID
    @GetMapping("/recordid/{recordId}")
    public Record getRecordByRecordID(@CurrentUser User currentUser,
                                              @PathVariable Long recordId) {
        return recordService.getRecordByRecordID(recordId);
    }

    @GetMapping("/therapist/")
    public PagedResponse<Record> getRecordByTherapist(@CurrentUser User currentUser,
                                               @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                               @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return recordService.getRecordsCreatedBy(currentUser, page, size);
    }

    @GetMapping("/patient/")
    public PagedResponse<Record> getRecordByPatient(@CurrentUser User currentUser,
                                                              @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                                              @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return recordService.getRecordsBelongingTo(currentUser, page, size);
    }

    //Therapist get patient-specific permitted records
    @GetMapping("/therapist/patient/{patient}")
    public PagedResponse<Record> getRecordsPermittedByPatient(@CurrentUser User currentUser,
                                                                      @PathVariable String patient,
                                                                      @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                                                      @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return recordService.getRecordsPermittedByPatient(currentUser, patient, page, size);
    }
}
