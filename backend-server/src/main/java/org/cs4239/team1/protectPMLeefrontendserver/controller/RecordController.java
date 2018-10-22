
package org.cs4239.team1.protectPMLeefrontendserver.controller;

import java.net.URI;

import javax.validation.Valid;

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
import org.cs4239.team1.protectPMLeefrontendserver.payload.RecordResponse;
import org.cs4239.team1.protectPMLeefrontendserver.repository.TreatmentRepository;
import org.cs4239.team1.protectPMLeefrontendserver.repository.UserRepository;
import org.cs4239.team1.protectPMLeefrontendserver.security.CurrentUser;
import org.cs4239.team1.protectPMLeefrontendserver.service.RecordService;
import org.cs4239.team1.protectPMLeefrontendserver.util.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/records")
public class RecordController {

    @Autowired
    private TreatmentRepository treatmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecordService recordService;

    private static final Logger logger = LoggerFactory.getLogger(RecordController.class);

    //TODO: I think all PreAuthorizes in Controller classes should be placed in SecurityConfig
    @PostMapping("/create/")
    public ResponseEntity<?> createRecord(@Valid @RequestBody RecordRequest recordRequest) {
        // TODO: Need some validation of RecordRequest fields such as whether patientIc exists.
        Record record = recordService.createRecord(recordRequest);

        // TODO: Should be documented elsewhere, not in code.
        // TODO: Should call TreatmentService#assignTherapistPatient
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
    }

    //Get all records
    @GetMapping
    public PagedResponse<RecordResponse> getRecords(@CurrentUser User currentUser,
                                                    @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                                    @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return recordService.getAllRecords(currentUser, page, size);
    }


    //Get specific records by RecordID
    @GetMapping("/recordid/{recordId}")
    public RecordResponse getRecordByRecordID(@CurrentUser User currentUser,
                                              @PathVariable Long recordId) {
        return recordService.getRecordByRecordID(recordId, currentUser);
    }

    @GetMapping("/therapist/")
    public PagedResponse<RecordResponse> getRecordByTherapist(@CurrentUser User currentUser,
                                               @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                               @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return recordService.getRecordsCreatedBy(currentUser, page, size);
    }

    @GetMapping("/patient/")
    public PagedResponse<RecordResponse> getRecordByPatient(@CurrentUser User currentUser,
                                                              @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                                              @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return recordService.getRecordsBelongingTo(currentUser, page, size);
    }

    //Therapist get patient-specific permitted records
    @GetMapping("/therapist/patient/{patient}")
    public PagedResponse<RecordResponse> getRecordsPermittedByPatient(@CurrentUser User currentUser,
                                                                      @PathVariable String patient,
                                                                      @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                                                      @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return recordService.getRecordsPermittedByPatient(currentUser, patient, page, size);
    }
}
