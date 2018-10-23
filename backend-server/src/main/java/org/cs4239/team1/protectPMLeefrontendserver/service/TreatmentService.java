package org.cs4239.team1.protectPMLeefrontendserver.service;

import java.time.Instant;
import java.util.Collections;

import org.cs4239.team1.protectPMLeefrontendserver.exception.BadRequestException;
import org.cs4239.team1.protectPMLeefrontendserver.exception.ResourceNotFoundException;
import org.cs4239.team1.protectPMLeefrontendserver.model.Role;
import org.cs4239.team1.protectPMLeefrontendserver.model.Treatment;
import org.cs4239.team1.protectPMLeefrontendserver.model.TreatmentId;
import org.cs4239.team1.protectPMLeefrontendserver.model.User;
import org.cs4239.team1.protectPMLeefrontendserver.payload.EndTreatmentRequest;
import org.cs4239.team1.protectPMLeefrontendserver.payload.PagedResponse;
import org.cs4239.team1.protectPMLeefrontendserver.payload.TreatmentRequest;
import org.cs4239.team1.protectPMLeefrontendserver.repository.TreatmentRepository;
import org.cs4239.team1.protectPMLeefrontendserver.repository.UserRepository;
import org.cs4239.team1.protectPMLeefrontendserver.util.AppConstants;
import org.cs4239.team1.protectPMLeefrontendserver.util.FormatDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class TreatmentService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TreatmentRepository treatmentRepository;

    private static final Logger logger = LoggerFactory.getLogger(TreatmentService.class);

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public PagedResponse<Treatment> getAllTreatments(int page, int size) {
        validatePageNumberAndSize(size);

        // Retrieve all treatments
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<Treatment> treatments = treatmentRepository.findAll(pageable);

        if(treatments.getNumberOfElements() == 0) {
            return new PagedResponse<>(Collections.emptyList(), treatments.getNumber(),
                    treatments.getSize(), treatments.getTotalElements(), treatments.getTotalPages(), treatments.isLast());
        }

        return new PagedResponse<Treatment>(treatments.getContent(), treatments.getNumber(),
                treatments.getSize(), treatments.getTotalElements(), treatments.getTotalPages(), treatments.isLast());
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public Treatment assignTherapistPatient(TreatmentRequest treatmentRequest){

        User therapist = userRepository.findByNric(treatmentRequest.getTherapistNric())
                .orElseThrow(() -> new ResourceNotFoundException("User", "nric", treatmentRequest.getTherapistNric()));
        if (!therapist.getRoles().contains(Role.ROLE_THERAPIST)){
            throw new BadRequestException(therapist.getNric() + " is not a therapist!");
        }
        User patient = userRepository.findByNric(treatmentRequest.getPatientNric())
                .orElseThrow(() -> new ResourceNotFoundException("User", "nric", treatmentRequest.getPatientNric()));
        if (!patient.getRoles().contains(Role.ROLE_PATIENT)){
            throw new BadRequestException(therapist.getNric() + " is not a patient!");
        }
        Instant expirationDateTime = FormatDate.formatDate(treatmentRequest.getEndDate());

        Treatment treatment = new Treatment(therapist, patient, expirationDateTime);

        //update treatment endDate if treatment has not expired by deleting old entry
        if (treatmentRepository.findByTreatmentId(treatment.getTreatmentId()) != null){
            treatmentRepository.delete(treatment);
        }

        return treatmentRepository.save(treatment);
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public void stopTherapistPatient(EndTreatmentRequest endTreatmentRequest){

        User therapist = userRepository.findByNric(endTreatmentRequest.getTherapistNric())
                .orElseThrow(() -> new ResourceNotFoundException("User", "nric", endTreatmentRequest.getTherapistNric()));
        if (!therapist.getRoles().contains(Role.ROLE_THERAPIST)){
            throw new BadRequestException(therapist.getNric() + " is not a therapist!");
        }
        User patient = userRepository.findByNric(endTreatmentRequest.getPatientNric())
                .orElseThrow(() -> new ResourceNotFoundException("User", "nric", endTreatmentRequest.getPatientNric()));
        if (!patient.getRoles().contains(Role.ROLE_THERAPIST)){
            throw new BadRequestException(therapist.getNric() + " is not a therapist!");
        }

        treatmentRepository.deleteById(new TreatmentId(therapist.getNric(), patient.getNric()));
    }

    @PreAuthorize("hasRole('THERAPIST') or hasRole('PATIENT')")
    public PagedResponse<Treatment> getUsers(User currentUser, Role role, int page, int size) {
        validatePageNumberAndSize(size);

        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");

        Page<Treatment> treatments = getTreatments(currentUser, role, pageable);

        if (treatments.getNumberOfElements() == 0) {
            return new PagedResponse<>(Collections.emptyList(), treatments.getNumber(),
                    treatments.getSize(), treatments.getTotalElements(), treatments.getTotalPages(), treatments.isLast());
        }

        return new PagedResponse<>(treatments.getContent(), treatments.getNumber(),
                treatments.getSize(), treatments.getTotalElements(), treatments.getTotalPages(), treatments.isLast());
    }

    private Page<Treatment> getTreatments(User currentUser, Role role, Pageable pageable) {
        if (role.equals(Role.ROLE_PATIENT)) {
            //Retrieve all patients treated by this user(therapist)
            return treatmentRepository.findByTherapist(currentUser, pageable);
        } else if (role.equals(Role.ROLE_THERAPIST)) {
            //Retrieve all therapist treating this user(patient)
            return treatmentRepository.findByPatient(currentUser, pageable);
        } else {
            throw new AssertionError("Should not happen.");
        }
    }

    private void validatePageNumberAndSize(int size) {
        if(size > AppConstants.MAX_PAGE_SIZE) {
            throw new BadRequestException("Page size must not be greater than " + AppConstants.MAX_PAGE_SIZE);
        }
    }
}