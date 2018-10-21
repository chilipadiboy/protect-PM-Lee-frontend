package org.cs4239.team1.protectPMLeefrontendserver.service;

import org.cs4239.team1.protectPMLeefrontendserver.exception.BadRequestException;
import org.cs4239.team1.protectPMLeefrontendserver.exception.ResourceNotFoundException;
import org.cs4239.team1.protectPMLeefrontendserver.model.Treatment;
import org.cs4239.team1.protectPMLeefrontendserver.model.User;
import org.cs4239.team1.protectPMLeefrontendserver.payload.PagedResponse;
import org.cs4239.team1.protectPMLeefrontendserver.payload.TreatmentRequest;
import org.cs4239.team1.protectPMLeefrontendserver.payload.TreatmentResponse;
import org.cs4239.team1.protectPMLeefrontendserver.repository.TreatmentRepository;
import org.cs4239.team1.protectPMLeefrontendserver.repository.UserRepository;
import org.cs4239.team1.protectPMLeefrontendserver.util.AppConstants;
import org.cs4239.team1.protectPMLeefrontendserver.util.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Collections;
import java.util.List;

import static java.time.ZoneOffset.UTC;

@Service
public class TreatmentService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TreatmentRepository treatmentRepository;

    private static final Logger logger = LoggerFactory.getLogger(TreatmentService.class);

    public PagedResponse<TreatmentResponse> getAllTreatments(User currentUser, int page, int size) {
        validatePageNumberAndSize(page, size);

        // Retrieve all treatments
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<Treatment> treatments = treatmentRepository.findAll(pageable);

        if(treatments.getNumberOfElements() == 0) {
            return new PagedResponse<>(Collections.emptyList(), treatments.getNumber(),
                    treatments.getSize(), treatments.getTotalElements(), treatments.getTotalPages(), treatments.isLast());
        }

        List<TreatmentResponse> treatmentResponse = treatments.map(treatment -> {
            return ModelMapper.mapTreatmentToTreatmentResponse(treatment);
        }).getContent();

        return new PagedResponse<>(treatmentResponse, treatments.getNumber(),
                treatments.getSize(), treatments.getTotalElements(), treatments.getTotalPages(), treatments.isLast());
    }

    public Treatment assignTherapistPatient(TreatmentRequest treatmentRequest){

        User therapist = userRepository.findByNric(treatmentRequest.getTherapistNric())
                .orElseThrow(() -> new ResourceNotFoundException("User", "nric", treatmentRequest.getTherapistNric()));
        User patient = userRepository.findByNric(treatmentRequest.getPatientNric())
                .orElseThrow(() -> new ResourceNotFoundException("User", "nric", treatmentRequest.getPatientNric()));

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

    public Treatment stopTherapistPatient(TreatmentRequest treatmentRequest){

        User therapist = userRepository.findByNric(treatmentRequest.getTherapistNric())
                .orElseThrow(() -> new ResourceNotFoundException("User", "nric", treatmentRequest.getTherapistNric()));
        User patient = userRepository.findByNric(treatmentRequest.getPatientNric())
                .orElseThrow(() -> new ResourceNotFoundException("User", "nric", treatmentRequest.getPatientNric()));
        Instant expirationDateTime = Instant.now();

        Treatment treatment = new Treatment(therapist, patient, expirationDateTime);

        treatmentRepository.delete(treatment);

        return treatment;
    }

    public PagedResponse<TreatmentResponse> getPatients(User currentUser, int page, int size) {
        validatePageNumberAndSize(page, size);

        // Retrieve all patients under this user(therapist)
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<Treatment> treatments = treatmentRepository.findByTherapist(currentUser, pageable);

        if (treatments.getNumberOfElements() == 0) {
            return new PagedResponse<>(Collections.emptyList(), treatments.getNumber(),
                    treatments.getSize(), treatments.getTotalElements(), treatments.getTotalPages(), treatments.isLast());
        }

        // Map treatments to TreatmentResponse
        List<TreatmentResponse> treatmentResponses = treatments.map(treatment -> {
            return ModelMapper.mapTreatmentToTreatmentResponse(treatment);
        }).getContent();

        return new PagedResponse<>(treatmentResponses, treatments.getNumber(),
                treatments.getSize(), treatments.getTotalElements(), treatments.getTotalPages(), treatments.isLast());
    }

    public PagedResponse<TreatmentResponse> getTherapists(User currentUser, int page, int size) {
        validatePageNumberAndSize(page, size);

        // Retrieve all therapists treating this user(patient)
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<Treatment> treatments = treatmentRepository.findByPatient(currentUser, pageable);

        if (treatments.getNumberOfElements() == 0) {
            return new PagedResponse<>(Collections.emptyList(), treatments.getNumber(),
                    treatments.getSize(), treatments.getTotalElements(), treatments.getTotalPages(), treatments.isLast());
        }

        // Map treatments to TreatmentResponse
        List<TreatmentResponse> treatmentResponses = treatments.map(treatment -> {
            return ModelMapper.mapTreatmentToTreatmentResponse(treatment);
        }).getContent();

        return new PagedResponse<>(treatmentResponses, treatments.getNumber(),
                treatments.getSize(), treatments.getTotalElements(), treatments.getTotalPages(), treatments.isLast());
    }


    private void validatePageNumberAndSize(int page, int size) {
        if(page < 0) {
            throw new BadRequestException("Page number cannot be less than zero.");
        }

        if(size > AppConstants.MAX_PAGE_SIZE) {
            throw new BadRequestException("Page size must not be greater than " + AppConstants.MAX_PAGE_SIZE);
        }
    }
}