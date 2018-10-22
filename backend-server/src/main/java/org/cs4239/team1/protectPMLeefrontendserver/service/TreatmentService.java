package org.cs4239.team1.protectPMLeefrontendserver.service;

import static java.time.ZoneOffset.UTC;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Collections;

import org.cs4239.team1.protectPMLeefrontendserver.exception.BadRequestException;
import org.cs4239.team1.protectPMLeefrontendserver.exception.ResourceNotFoundException;
import org.cs4239.team1.protectPMLeefrontendserver.model.Role;
import org.cs4239.team1.protectPMLeefrontendserver.model.Treatment;
import org.cs4239.team1.protectPMLeefrontendserver.model.User;
import org.cs4239.team1.protectPMLeefrontendserver.payload.PagedResponse;
import org.cs4239.team1.protectPMLeefrontendserver.payload.TreatmentRequest;
import org.cs4239.team1.protectPMLeefrontendserver.repository.TreatmentRepository;
import org.cs4239.team1.protectPMLeefrontendserver.repository.UserRepository;
import org.cs4239.team1.protectPMLeefrontendserver.util.AppConstants;
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

    // TODO: Preauthorise can be done here
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

        // TODO: Verify roles
        // TODO: Maybe Time Parser.
        // TODO: Verify that end date > today. TreatmentController may have to throw something.
        String date = treatmentRequest.getEndDate() + " 23:59:59";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        TemporalAccessor temporalAccessor = formatter.parse(date);
        LocalDateTime localDateTime = LocalDateTime.from(temporalAccessor);
        ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, UTC);
        Instant expirationDateTime = Instant.from(zonedDateTime);

        Treatment treatment = new Treatment(therapist, patient, expirationDateTime);

        //update treatment endDate if treatment has not expired by deleting old entry
        if (treatmentRepository.findByTreatmentId(treatment.getTreatmentId()) != null){
            treatmentRepository.delete(treatment);
        }

        return treatmentRepository.save(treatment);
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public Treatment stopTherapistPatient(TreatmentRequest treatmentRequest){
        // TODO: Stopping treatment shouldn't need endDate.
        User therapist = userRepository.findByNric(treatmentRequest.getTherapistNric())
                .orElseThrow(() -> new ResourceNotFoundException("User", "nric", treatmentRequest.getTherapistNric()));
        User patient = userRepository.findByNric(treatmentRequest.getPatientNric())
                .orElseThrow(() -> new ResourceNotFoundException("User", "nric", treatmentRequest.getPatientNric()));
        Instant expirationDateTime = Instant.now();

        Treatment treatment = new Treatment(therapist, patient, expirationDateTime);

        treatmentRepository.delete(treatment);

        return treatment;
    }

    @PreAuthorize("hasRole('THERAPIST')")
    public PagedResponse<Treatment> getPatients(User currentUser, int page, int size) {
        validatePageNumberAndSize(size);

        //TODO: Code repetition as the method below. Extract it out.
        // Retrieve all patients under this user(therapist)
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<Treatment> treatments = treatmentRepository.findByTherapist(currentUser, pageable);

        if (treatments.getNumberOfElements() == 0) {
            return new PagedResponse<>(Collections.emptyList(), treatments.getNumber(),
                    treatments.getSize(), treatments.getTotalElements(), treatments.getTotalPages(), treatments.isLast());
        }

        return new PagedResponse<>(treatments.getContent(), treatments.getNumber(),
                treatments.getSize(), treatments.getTotalElements(), treatments.getTotalPages(), treatments.isLast());
    }

    @PreAuthorize("hasRole('PATIENT')")
    public PagedResponse<Treatment> getTherapists(User currentUser, int page, int size) {
        validatePageNumberAndSize(size);

        // Retrieve all therapists treating this user(patient)
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<Treatment> treatments = treatmentRepository.findByPatient(currentUser, pageable);

        if (treatments.getNumberOfElements() == 0) {
            return new PagedResponse<>(Collections.emptyList(), treatments.getNumber(),
                    treatments.getSize(), treatments.getTotalElements(), treatments.getTotalPages(), treatments.isLast());
        }

        return new PagedResponse<>(treatments.getContent(), treatments.getNumber(),
                treatments.getSize(), treatments.getTotalElements(), treatments.getTotalPages(), treatments.isLast());
    }


    private void validatePageNumberAndSize(int size) {
        if(size > AppConstants.MAX_PAGE_SIZE) {
            throw new BadRequestException("Page size must not be greater than " + AppConstants.MAX_PAGE_SIZE);
        }
    }
}