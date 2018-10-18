package org.cs4239.team1.protectPMLeefrontendserver.service;

import org.cs4239.team1.protectPMLeefrontendserver.exception.BadRequestException;
import org.cs4239.team1.protectPMLeefrontendserver.exception.ResourceNotFoundException;
import org.cs4239.team1.protectPMLeefrontendserver.model.Permission;
import org.cs4239.team1.protectPMLeefrontendserver.model.Record;
import org.cs4239.team1.protectPMLeefrontendserver.model.User;
import org.cs4239.team1.protectPMLeefrontendserver.payload.PagedResponse;
import org.cs4239.team1.protectPMLeefrontendserver.payload.PermissionRequest;
import org.cs4239.team1.protectPMLeefrontendserver.payload.RecordRequest;
import org.cs4239.team1.protectPMLeefrontendserver.payload.RecordResponse;
import org.cs4239.team1.protectPMLeefrontendserver.repository.PermissionRepository;
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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.time.ZoneOffset.UTC;

@Service
public class RecordService {

    @Autowired
    private RecordRepository recordRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PermissionRepository permissionRepository;

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

        List<RecordResponse> recordResponses = records.map(record -> {
            return ModelMapper.mapRecordToRecordResponse(record, user);
        }).getContent();

        return new PagedResponse<>(recordResponses, records.getNumber(),
                records.getSize(), records.getTotalElements(), records.getTotalPages(), records.isLast());
    }

    public Record createRecord(RecordRequest recordRequest) {
        Record record = new Record(recordRequest.getType(),
                recordRequest.getSubtype(),
                recordRequest.getTitle(),
                recordRequest.getDocument(),
                recordRequest.getPatientIC()
        );

        return recordRepository.save(record);
    }


    public RecordResponse getRecordByRecordID(Long recordId, User currentUser) {

        Record record = recordRepository.findByRecordID(recordId).orElseThrow(
                () -> new ResourceNotFoundException("Record", "id", recordId));

        // Retrieve record creator details
        User creator = userRepository.findByNric(record.getCreatedBy())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", record.getCreatedBy()));

        return ModelMapper.mapRecordToRecordResponse(record, creator);
    }


    public Permission grantPermission(PermissionRequest permissionRequest, User currentUser){

        Record record = recordRepository.findByRecordID(permissionRequest.getRecordID()).orElseThrow(
                () -> new ResourceNotFoundException("Record", "id", permissionRequest.getRecordID()));
        User user = userRepository.findByNric(permissionRequest.getNric())
                .orElseThrow(() -> new ResourceNotFoundException("User", "nric", permissionRequest.getNric()));
        String patientIC = currentUser.getNric();

        if(!record.getPatientIC().equals(patientIC)){
            throw new BadRequestException("You do not have permission to grant record " + record.getRecordID());
        }

        String date = permissionRequest.getEndDate() + " 23:59:59";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        TemporalAccessor temporalAccessor = formatter.parse(date);

        LocalDateTime localDateTime = LocalDateTime.from(temporalAccessor);
        ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, UTC);
        Instant expirationDateTime = Instant.from(zonedDateTime);

        Permission permission = new Permission(record,user, expirationDateTime, patientIC);

        //update permission expiry if permission has already been granted by deleting old entry
        if (permissionRepository.findByPermissionID(permission.getPermissionID()) != null){
            //throw new BadRequestException(record.getRecordID() + " has already been granted");
            permissionRepository.delete(permission);
        }

        return permissionRepository.save(permission);
    }

    public Permission revokePermission(PermissionRequest permissionRequest, User currentUser){

        Record record = recordRepository.findByRecordID(permissionRequest.getRecordID()).orElseThrow(
                () -> new ResourceNotFoundException("Record", "id", permissionRequest.getRecordID()));
        User user = userRepository.findByNric(permissionRequest.getNric())
                .orElseThrow(() -> new ResourceNotFoundException("User", "nric", permissionRequest.getNric()));
        Instant now = Instant.now();
        String patientIC = currentUser.getNric();

        if(!record.getPatientIC().equals(patientIC)){
            throw new BadRequestException("You do not have permission to revoke record " + record.getRecordID());
        }

        Permission permission = new Permission(record,user, now, patientIC);

        if (permissionRepository.findByPermissionID(permission.getPermissionID()) == null){
            throw new BadRequestException(record.getRecordID() + " has not been granted");
        }

        permissionRepository.delete(permission);
        return permission;
    }

    public PagedResponse<RecordResponse> getAllowedRecords(User currentUser, int page, int size) {
        validatePageNumberAndSize(page, size);

        String nric = currentUser.getNric();

        User user = userRepository.findByNric(nric)
                .orElseThrow(() -> new ResourceNotFoundException("User", "nric", nric));

        // Retrieve all records belong to the given nric
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<Permission> permission = permissionRepository.findByUser(user, pageable);

        if (permission.getNumberOfElements() == 0) {
            return new PagedResponse<>(Collections.emptyList(), permission.getNumber(),
                    permission.getSize(), permission.getTotalElements(), permission.getTotalPages(), permission.isLast());
        }

        // Map Records to RecordResponses
        List<RecordResponse> recordResponses = permission.map(permissions -> {
            return ModelMapper.mapRecordToRecordResponse(permissions.getRecord(), user);
        }).getContent();

        return new PagedResponse<>(recordResponses, permission.getNumber(),
                permission.getSize(), permission.getTotalElements(), permission.getTotalPages(), permission.isLast());
    }

    public PagedResponse<RecordResponse> getGivenRecords(User currentUser, int page, int size) {
        validatePageNumberAndSize(page, size);

        String nric = currentUser.getNric();

        User user = userRepository.findByNric(nric)
                .orElseThrow(() -> new ResourceNotFoundException("User", "nric", nric));

        // Retrieve all records belong to the given nric
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<Permission> permission = permissionRepository.findByPatientNric(nric, pageable);

        if (permission.getNumberOfElements() == 0) {
            return new PagedResponse<>(Collections.emptyList(), permission.getNumber(),
                    permission.getSize(), permission.getTotalElements(), permission.getTotalPages(), permission.isLast());
        }

        // Map Records to RecordResponses
        List<RecordResponse> recordResponses = permission.map(permissions -> {
            return ModelMapper.mapRecordToRecordResponse(permissions.getRecord(), user);
        }).getContent();

        return new PagedResponse<>(recordResponses, permission.getNumber(),
                permission.getSize(), permission.getTotalElements(), permission.getTotalPages(), permission.isLast());
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