package org.cs4239.team1.protectPMLeefrontendserver.controller;

import org.cs4239.team1.protectPMLeefrontendserver.model.Permission;
import org.cs4239.team1.protectPMLeefrontendserver.model.User;
import org.cs4239.team1.protectPMLeefrontendserver.payload.ApiResponse;
import org.cs4239.team1.protectPMLeefrontendserver.payload.PagedResponse;
import org.cs4239.team1.protectPMLeefrontendserver.payload.PermissionRequest;
import org.cs4239.team1.protectPMLeefrontendserver.payload.RecordResponse;
import org.cs4239.team1.protectPMLeefrontendserver.payload.RecordResponseWithTherapistIdentifier;
import org.cs4239.team1.protectPMLeefrontendserver.repository.RecordRepository;
import org.cs4239.team1.protectPMLeefrontendserver.repository.UserRepository;
import org.cs4239.team1.protectPMLeefrontendserver.repository.PermissionRepository;
import org.cs4239.team1.protectPMLeefrontendserver.security.CurrentUser;
import org.cs4239.team1.protectPMLeefrontendserver.service.RecordService;
import org.cs4239.team1.protectPMLeefrontendserver.util.AppConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import javax.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    @Autowired
    private RecordRepository recordRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecordService recordService;

    private static final Logger logger = LoggerFactory.getLogger(RecordController.class);

    //give permission to view
    @PostMapping("/permit/")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> grantPermission(@Valid @RequestBody PermissionRequest permissionRequest, @CurrentUser User currentUser) {

        Permission permission = recordService.grantPermission(permissionRequest, currentUser);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{recordID}")
                .buildAndExpand(permissionRequest.getRecordID())
                .toUri();

        return ResponseEntity.created(location)
                .body(new ApiResponse(true, "Record_" + permissionRequest.getRecordID() + "'s permission has been GRANTED to Therapist_" + permissionRequest.getNric()));
    }

    //revoke permission to view
    @PostMapping("/revoke/")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> revokePermission(@Valid @RequestBody PermissionRequest permissionRequest, @CurrentUser User currentUser) {

        Permission permission = recordService.revokePermission(permissionRequest, currentUser);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{recordID}")
                .buildAndExpand(permissionRequest.getRecordID())
                .toUri();

        return ResponseEntity.created(location)
                .body(new ApiResponse(true, "Record_" + permissionRequest.getRecordID() + "'s permission has been REVOKED from Therapist_" + permissionRequest.getNric()));
    }

    //Get all permissions that currentUser (the therapist) has been allowed to see
    @GetMapping("/therapist/allowed/")
    @PreAuthorize("hasRole('THERAPIST')")
    public PagedResponse<RecordResponse> getAllowedRecords(@CurrentUser User currentUser,
                                                           @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                                           @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return recordService.getAllowedRecords(currentUser, page, size);
    }

    //Get all permissions that currentUser(the patient) has granted
    @GetMapping("/patient/given/")
    @PreAuthorize("hasRole('PATIENT')")
    public PagedResponse<RecordResponseWithTherapistIdentifier> getGivenRecords(@CurrentUser User currentUser,
                                                                                @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                                                                @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return recordService.getGivenRecords(currentUser, page, size);
    }

}