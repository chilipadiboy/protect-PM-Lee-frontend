package org.cs4239.team1.protectPMLeefrontendserver.controller;

import java.net.URI;

import javax.validation.Valid;

import org.cs4239.team1.protectPMLeefrontendserver.model.Treatment;
import org.cs4239.team1.protectPMLeefrontendserver.model.User;
import org.cs4239.team1.protectPMLeefrontendserver.payload.ApiResponse;
import org.cs4239.team1.protectPMLeefrontendserver.payload.EndTreatmentRequest;
import org.cs4239.team1.protectPMLeefrontendserver.payload.PagedResponse;
import org.cs4239.team1.protectPMLeefrontendserver.payload.TreatmentRequest;
import org.cs4239.team1.protectPMLeefrontendserver.security.CurrentUser;
import org.cs4239.team1.protectPMLeefrontendserver.service.TreatmentService;
import org.cs4239.team1.protectPMLeefrontendserver.util.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/treatments")
public class TreatmentController {

    @Autowired
    private TreatmentService treatmentService;

    private static final Logger logger = LoggerFactory.getLogger(RecordController.class);

    //Admin assigns therapist-patient treatment pair
    @PostMapping("/start/")
    public ResponseEntity<?> startTreatment(@Valid @RequestBody TreatmentRequest treatmentRequest) {
        treatmentService.assignTherapistPatient(treatmentRequest);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{patientNric}")
                .buildAndExpand(treatmentRequest.getPatientNric())
                .toUri();

        return ResponseEntity.created(location)
                .body(new ApiResponse(true, "Therapist_" + treatmentRequest.getTherapistNric() + " treats Patient_" + treatmentRequest.getPatientNric() + " until " + treatmentRequest.getEndDate()));
    }

    //Admin terminates therapist-patient treatment pair
    @PostMapping("/stop/")
    public ResponseEntity<?> stopTreatment(@Valid @RequestBody EndTreatmentRequest endTreatmentRequest) {

        treatmentService.stopTherapistPatient(endTreatmentRequest);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{patientNric}")
                .buildAndExpand(endTreatmentRequest.getPatientNric())
                .toUri();

        return ResponseEntity.created(location)
                .body(new ApiResponse(true, "Therapist_" + endTreatmentRequest.getTherapistNric() + " STOP treating Patient_" + endTreatmentRequest.getPatientNric()));
    }

    //List ALL treatments
    @GetMapping("/getAll/")
    public PagedResponse<Treatment> getAllTreatments(@CurrentUser User currentUser,
                                                    @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                                    @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return treatmentService.getAllTreatments(page, size);
    }

    //Therapist get list of all his patients
    @GetMapping("/getPatients/")
    public PagedResponse<Treatment> getPatients(@CurrentUser User currentUser,
                                                @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                                @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        String type = "getPatients";
        return treatmentService.getUsers(currentUser, type, page, size);
    }

    //Patient get list of all his Therapists
    @GetMapping("/getTherapists/")
    public PagedResponse<Treatment> getTherapists(@CurrentUser User currentUser,
                                                  @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                                  @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        String type = "getTherapists";
        return treatmentService.getUsers(currentUser, type, page, size);
    }
}