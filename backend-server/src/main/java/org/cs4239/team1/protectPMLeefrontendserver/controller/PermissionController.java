package org.cs4239.team1.protectPMLeefrontendserver.controller;

import java.net.URI;

import javax.validation.Valid;

import org.cs4239.team1.protectPMLeefrontendserver.model.Permission;
import org.cs4239.team1.protectPMLeefrontendserver.model.Record;
import org.cs4239.team1.protectPMLeefrontendserver.model.User;
import org.cs4239.team1.protectPMLeefrontendserver.payload.ApiResponse;
import org.cs4239.team1.protectPMLeefrontendserver.payload.EndPermissionRequest;
import org.cs4239.team1.protectPMLeefrontendserver.payload.PagedResponse;
import org.cs4239.team1.protectPMLeefrontendserver.payload.PermissionRequest;
import org.cs4239.team1.protectPMLeefrontendserver.payload.RecordResponseWithTherapistIdentifier;
import org.cs4239.team1.protectPMLeefrontendserver.repository.PermissionRepository;
import org.cs4239.team1.protectPMLeefrontendserver.repository.RecordRepository;
import org.cs4239.team1.protectPMLeefrontendserver.repository.UserRepository;
import org.cs4239.team1.protectPMLeefrontendserver.security.CurrentUser;
import org.cs4239.team1.protectPMLeefrontendserver.service.RecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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

    private static final Logger logger = LoggerFactory.getLogger(PermissionController.class);

    //give permission to view
    @PostMapping("/permit/")
    public ResponseEntity<?> grantPermission(@Valid @RequestBody PermissionRequest permissionRequest, @CurrentUser User currentUser) {
        logger.info("NRIC_" + currentUser.getNric() + " ROLE_" + currentUser.getSelectedRole() + " accessing PermissionController#grantPermission", permissionRequest);
        Permission permission = recordService.grantPermission(permissionRequest, currentUser);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{recordID}")
                .buildAndExpand(permissionRequest.getRecordID())
                .toUri();

        return ResponseEntity.created(location)
                .body(new ApiResponse(true, "Record_" + permissionRequest.getRecordID() + "'s permission has been GRANTED to Therapist_" + permissionRequest.getTherapistNric()));
    }

    //revoke permission to view
    @PostMapping("/revoke/")
    public ResponseEntity<?> revokePermission(@Valid @RequestBody EndPermissionRequest permissionRequest, @CurrentUser User currentUser) {
        logger.info("NRIC_" + currentUser.getNric() + " ROLE_" + currentUser.getSelectedRole() + " accessing PermissionController#revokePermission", permissionRequest);
        recordService.revokePermission(permissionRequest, currentUser);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{recordID}")
                .buildAndExpand(permissionRequest.getRecordID())
                .toUri();

        return ResponseEntity.created(location)
                .body(new ApiResponse(true, "Record_" + permissionRequest.getRecordID() + "'s permission has been REVOKED from Therapist_" + permissionRequest.getTherapistNric()));
    }

    //Get all permissions that currentUser (the therapist) has been allowed to see
    @GetMapping("/therapist/allowed/")
    public PagedResponse<Record> getAllowedRecords(@CurrentUser User currentUser) {
        logger.info("NRIC_" + currentUser.getNric() + " ROLE_" + currentUser.getSelectedRole() + " accessing PermissionController#getAllowedRecords");
        return recordService.getAllowedRecords(currentUser);
    }

    //Get all permissions that currentUser(the patient) has granted
    @GetMapping("/patient/given/")
    public PagedResponse<RecordResponseWithTherapistIdentifier> getGivenRecords(@CurrentUser User currentUser) {
        logger.info("NRIC_" + currentUser.getNric() + " ROLE_" + currentUser.getSelectedRole() + " accessing PermissionController#getGivenRecords");
        return recordService.getGivenRecords(currentUser);
    }

}
