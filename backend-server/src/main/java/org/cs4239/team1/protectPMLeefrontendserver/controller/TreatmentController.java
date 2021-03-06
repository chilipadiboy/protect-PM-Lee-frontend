package org.cs4239.team1.protectPMLeefrontendserver.controller;

import java.net.URI;

import javax.validation.Valid;

import org.cs4239.team1.protectPMLeefrontendserver.exception.BadRequestException;
import org.cs4239.team1.protectPMLeefrontendserver.exception.ResourceNotFoundException;
import org.cs4239.team1.protectPMLeefrontendserver.model.Role;
import org.cs4239.team1.protectPMLeefrontendserver.model.Treatment;
import org.cs4239.team1.protectPMLeefrontendserver.model.TreatmentId;
import org.cs4239.team1.protectPMLeefrontendserver.model.User;
import org.cs4239.team1.protectPMLeefrontendserver.payload.ApiResponse;
import org.cs4239.team1.protectPMLeefrontendserver.payload.EndTreatmentRequest;
import org.cs4239.team1.protectPMLeefrontendserver.payload.PagedResponse;
import org.cs4239.team1.protectPMLeefrontendserver.payload.TreatmentRequest;
import org.cs4239.team1.protectPMLeefrontendserver.payload.TreatmentResponseWithName;
import org.cs4239.team1.protectPMLeefrontendserver.payload.UserSummary;
import org.cs4239.team1.protectPMLeefrontendserver.repository.TreatmentRepository;
import org.cs4239.team1.protectPMLeefrontendserver.repository.UserRepository;
import org.cs4239.team1.protectPMLeefrontendserver.security.CurrentUser;
import org.cs4239.team1.protectPMLeefrontendserver.service.TreatmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/treatments")
public class TreatmentController {

    @Autowired
    private TreatmentService treatmentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TreatmentRepository treatmentRepository;

    private static final Logger logger = LoggerFactory.getLogger(TreatmentController.class);

    //Admin assigns therapist-patient treatment pair
    @PostMapping("/start/")
    public ResponseEntity<?> startTreatment(@CurrentUser User currentUser, @Valid @RequestBody TreatmentRequest treatmentRequest) {
        logger.info("NRIC_" + currentUser.getNric() + " ROLE_" + currentUser.getSelectedRole() + " accessing TreatmentController#startTreatment", treatmentRequest);
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
    public ResponseEntity<?> stopTreatment(@CurrentUser User currentUser, @Valid @RequestBody EndTreatmentRequest endTreatmentRequest) {
        logger.info("NRIC_" + currentUser.getNric() + " ROLE_" + currentUser.getSelectedRole() + " accessing TreatmentController#endTreatment", endTreatmentRequest);
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
    public PagedResponse<Treatment> getAllTreatments(@CurrentUser User currentUser) {
        logger.info("NRIC_" + currentUser.getNric() + " ROLE_" + currentUser.getSelectedRole() + " accessing TreatmentController#getAllTreatments");
        return treatmentService.getAllTreatments(currentUser);
    }

    //Therapist get list of all his patients
    @GetMapping("/getPatients/")
    public PagedResponse<TreatmentResponseWithName> getPatients(@CurrentUser User currentUser) {
        logger.info("NRIC_" + currentUser.getNric() + " ROLE_" + currentUser.getSelectedRole() + " accessing TreatmentController#getPatients");
        return treatmentService.getUsers(currentUser, Role.ROLE_PATIENT);
    }

    //Patient get list of all his Therapists
    @GetMapping("/getTherapists/")
    public PagedResponse<TreatmentResponseWithName> getTherapists(@CurrentUser User currentUser) {
        logger.info("NRIC_" + currentUser.getNric() + " ROLE_" + currentUser.getSelectedRole() + " accessing TreatmentController#getTherapists");
        String type = "getTherapists";
        return treatmentService.getUsers(currentUser, Role.ROLE_THERAPIST);
    }

    @GetMapping("/getUserSummary/{nric}")
    public UserSummary getUserSummary(@CurrentUser User currentUser, @PathVariable(value = "nric") String nric) {
        logger.info("NRIC_" + currentUser.getNric() + " ROLE_" + currentUser.getSelectedRole() + " accessing TreatmentController#getUserSummary", nric);
        User patient = userRepository.findByNric(nric)
                .orElseThrow(() -> new ResourceNotFoundException("User", "nric", nric));

        if (treatmentRepository.findByTreatmentId(new TreatmentId(currentUser.getNric(),patient.getNric())) == null) {
            throw new BadRequestException("Not allowed to retrieve this user.");
        }

        return new UserSummary(patient.getNric(), patient.getName(),
                "Patient",
                patient.getPhone(), patient.getEmail());
    }
}
