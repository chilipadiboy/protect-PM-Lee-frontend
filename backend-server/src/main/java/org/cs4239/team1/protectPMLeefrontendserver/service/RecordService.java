package org.cs4239.team1.protectPMLeefrontendserver.service;

import org.cs4239.team1.protectPMLeefrontendserver.exception.BadRequestException;
import org.cs4239.team1.protectPMLeefrontendserver.exception.ResourceNotFoundException;
import org.cs4239.team1.protectPMLeefrontendserver.model.*;
import org.cs4239.team1.protectPMLeefrontendserver.payload.PagedResponse;
import org.cs4239.team1.protectPMLeefrontendserver.payload.RecordRequest;
import org.cs4239.team1.protectPMLeefrontendserver.payload.RecordResponse;
import org.cs4239.team1.protectPMLeefrontendserver.repository.RecordRepository;
import org.cs4239.team1.protectPMLeefrontendserver.repository.UserRepository;
import org.cs4239.team1.protectPMLeefrontendserver.util.AppConstants;
import org.cs4239.team1.protectPMLeefrontendserver.util.ModelMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RecordService {

    @Autowired
    private RecordRepository recordRepository;

    @Autowired
    private UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(RecordService.class);

    public PagedResponse<RecordResponse> getAllRecords(User currentUser, int page, int size) {
        validatePageNumberAndSize(page, size);

        // Retrieve Records
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<Record> records = recordRepository.findAll(pageable);

        if(records.getNumberOfElements() == 0) {
            return new PagedResponse<>(Collections.emptyList(), records.getNumber(),
                    records.getSize(), records.getTotalElements(), records.getTotalPages(), records.isLast());
        }

        // Map Records to RecordResponses containing vote counts and record creator details
        List<Long> recordIDs = records.map(Record::getRecordID).getContent();
        Map<String, User> creatorMap = getRecordCreatorMap(records.getContent());

        List<RecordResponse> recordResponses = records.map(record -> {
            return ModelMapper.mapRecordToRecordResponse(record,
                    creatorMap.get(record.getCreatedBy()));
        }).getContent();

        return new PagedResponse<>(recordResponses, records.getNumber(),
                records.getSize(), records.getTotalElements(), records.getTotalPages(), records.isLast());
    }

    public PagedResponse<RecordResponse> getRecordsCreatedBy(String nric, User currentUser, int page, int size) {
        validatePageNumberAndSize(page, size);

        User user = userRepository.findByNric(nric)
                .orElseThrow(() -> new ResourceNotFoundException("User", "nric", nric));

        // Retrieve all records created by the given nric
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<Record> records = recordRepository.findByCreatedBy(user.getNric(), pageable);

        if (records.getNumberOfElements() == 0) {
            return new PagedResponse<>(Collections.emptyList(), records.getNumber(),
                    records.getSize(), records.getTotalElements(), records.getTotalPages(), records.isLast());
        }

        // Map Records to RecordResponses containing vote counts and record creator details
        List<Long> recordIDs = records.map(Record::getRecordID).getContent();

        List<RecordResponse> recordResponses = records.map(record -> {
            return ModelMapper.mapRecordToRecordResponse(record, user);
        }).getContent();

        return new PagedResponse<>(recordResponses, records.getNumber(),
                records.getSize(), records.getTotalElements(), records.getTotalPages(), records.isLast());
    }

    public PagedResponse<RecordResponse> getRecordsBelongingTo(String nric, User currentUser, int page, int size) {
        validatePageNumberAndSize(page, size);

        User user = userRepository.findByNric(nric)
                .orElseThrow(() -> new ResourceNotFoundException("User", "nric", nric));

        // Retrieve all records belong to the given nric
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<Record> records = recordRepository.findByPatientIC(user.getNric(), pageable);

        if (records.getNumberOfElements() == 0) {
            return new PagedResponse<>(Collections.emptyList(), records.getNumber(),
                    records.getSize(), records.getTotalElements(), records.getTotalPages(), records.isLast());
        }

        // Map Records to RecordResponses containing vote counts and record creator details
        List<Long> recordIDs = records.map(Record::getRecordID).getContent();

        List<RecordResponse> recordResponses = records.map(record -> {
            return ModelMapper.mapRecordToRecordResponse(record, user);
        }).getContent();

        return new PagedResponse<>(recordResponses, records.getNumber(),
                records.getSize(), records.getTotalElements(), records.getTotalPages(), records.isLast());
    }

    public Record createRecord(RecordRequest recordRequest) {
        Record record = new Record();

        record.setType(recordRequest.getType());
        record.setSubtype(recordRequest.getSubtype());
        record.setTitle(recordRequest.getTitle());
        record.setDocument(recordRequest.getDocument());
        record.setPatientIC(recordRequest.getPatientIC());

        return recordRepository.save(record);
    }

    public RecordResponse getRecordByRecordID(String recordId, User currentUser) {
        Record record = recordRepository.findByRecordID(recordId).orElseThrow(
                () -> new ResourceNotFoundException("Record", "id", recordId));

        // Retrieve record creator details
        User creator = userRepository.findByNric(record.getCreatedBy())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", record.getCreatedBy()));

        return ModelMapper.mapRecordToRecordResponse(record, creator);
    }

    private void validatePageNumberAndSize(int page, int size) {
        if(page < 0) {
            throw new BadRequestException("Page number cannot be less than zero.");
        }

        if(size > AppConstants.MAX_PAGE_SIZE) {
            throw new BadRequestException("Page size must not be greater than " + AppConstants.MAX_PAGE_SIZE);
        }
    }

    Map<String, User> getRecordCreatorMap(List<Record> records) {
        // Get Record Creator details of the given list of records
        List<String> creatorIds = records.stream()
                .map(Record::getCreatedBy)
                .distinct()
                .collect(Collectors.toList());

        List<User> creators = userRepository.findByNricIn(creatorIds);
        Map<String, User> creatorMap = creators.stream()
                .collect(Collectors.toMap(User::getNric, Function.identity()));

        return creatorMap;
    }
}