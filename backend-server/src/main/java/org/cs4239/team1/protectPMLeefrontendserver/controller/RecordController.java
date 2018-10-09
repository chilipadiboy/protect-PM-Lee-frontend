
package org.cs4239.team1.protectPMLeefrontendserver.controller;

import java.net.URI;

import javax.validation.Valid;

import org.cs4239.team1.protectPMLeefrontendserver.model.Record;
import org.cs4239.team1.protectPMLeefrontendserver.payload.ApiResponse;
import org.cs4239.team1.protectPMLeefrontendserver.payload.PagedResponse;
import org.cs4239.team1.protectPMLeefrontendserver.payload.RecordRequest;
import org.cs4239.team1.protectPMLeefrontendserver.payload.RecordResponse;
import org.cs4239.team1.protectPMLeefrontendserver.repository.RecordRepository;
import org.cs4239.team1.protectPMLeefrontendserver.repository.UserRepository;
import org.cs4239.team1.protectPMLeefrontendserver.security.CurrentUser;
import org.cs4239.team1.protectPMLeefrontendserver.security.UserPrincipal;
import org.cs4239.team1.protectPMLeefrontendserver.service.RecordService;
import org.cs4239.team1.protectPMLeefrontendserver.util.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    private RecordRepository recordRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecordService recordService;

    private static final Logger logger = LoggerFactory.getLogger(RecordController.class);

    @PostMapping
    //@PreAuthorize("hasRole('THERAPIST')")
    public ResponseEntity<?> createRecord(@Valid @RequestBody RecordRequest recordRequest) {
        Record record = recordService.createRecord(recordRequest);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{recordId}")
                .buildAndExpand(record.getRecordID()).toUri();

        return ResponseEntity.created(location)
                .body(new ApiResponse(true, "Record Created Successfully"));
    }

    //Get all records
    @GetMapping
    public PagedResponse<RecordResponse> getRecords(@CurrentUser UserPrincipal currentUser,
                                                    @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                                    @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return recordService.getAllRecords(currentUser, page, size);
    }


    //Get specific records by RecordID
    @GetMapping("/recordid/{recordId}")
    public RecordResponse getRecordByRecordID(@CurrentUser UserPrincipal currentUser,
                                              @PathVariable String recordId) {
        return recordService.getRecordByRecordID(recordId, currentUser);
    }

    //@PreAuthorize("checkownership")
    @GetMapping("/therapist/{therapist}")
    public PagedResponse<RecordResponse> getRecordByTherapist(@CurrentUser UserPrincipal currentUser,
                                              @PathVariable String therapist,
                                               @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                               @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return recordService.getRecordsCreatedBy(therapist, currentUser, page, size);
    }

    //@PreAuthorize("checkpatient")
    @GetMapping("/patient/{patient}")
    public PagedResponse<RecordResponse> getRecordByPatient(@CurrentUser UserPrincipal currentUser,
                                                              @PathVariable String patient,
                                                              @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                                              @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return recordService.getRecordsBelongingTo(patient, currentUser, page, size);
    }
}
