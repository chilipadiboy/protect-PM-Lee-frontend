package org.cs4239.team1.protectPMLeefrontendserver.controller;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.cs4239.team1.protectPMLeefrontendserver.exception.NonceExceededException;
import org.cs4239.team1.protectPMLeefrontendserver.payload.ApiResponse;
import org.cs4239.team1.protectPMLeefrontendserver.payload.ServerSignatureResponse;
import org.cs4239.team1.protectPMLeefrontendserver.security.AESEncryptionDecryptionTool;
import org.cs4239.team1.protectPMLeefrontendserver.security.Hasher;
import org.cs4239.team1.protectPMLeefrontendserver.security.NonceGenerator;
import org.cs4239.team1.protectPMLeefrontendserver.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.crypto.tink.subtle.Ed25519Sign;

import net.sf.jmimemagic.Magic;

@RestController
@RequestMapping("/api/file")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    private static final Collection<String> ALLOWED_FILE_TYPES = Arrays.asList("txt", "csv", "jpg", "png", "mp4");

    @Autowired
    private FileStorageService fileStorageService;

    @Value("${app.privateKey}")
    private String privateKey;

    @Value("${bluetooth.tag.encryptionKey}")
    private String tagKey;

    @PostMapping("/getSignature")
    public ResponseEntity<?> getFileSignature(@RequestBody MultipartFile file, @RequestParam("nric") String nric) {
        if (!ALLOWED_FILE_TYPES.contains(FilenameUtils.getExtension(file.getOriginalFilename()))) {
            return new ResponseEntity<>(new ApiResponse(false, "Invalid file type."), HttpStatus.BAD_REQUEST);
        }

        try {
            int nonceInServer = NonceGenerator.generateNonce(nric);
            byte[] msgHash = Hasher.hash(nonceInServer);
            byte[] fileBytesHash = Hasher.hash(file.getBytes());

            Ed25519Sign signer = new Ed25519Sign(Base64.getDecoder().decode(privateKey));
            byte[] signature = signer.sign(msgHash);

            byte[] combined = new byte[msgHash.length + signature.length + fileBytesHash.length];
            System.arraycopy(msgHash, 0, combined, 0, msgHash.length);
            System.arraycopy(signature, 0, combined, msgHash.length, signature.length);
            System.arraycopy(fileBytesHash, 0, combined, msgHash.length + signature.length, fileBytesHash.length);

            byte[] ivBytes = new byte[16];
            SecureRandom.getInstanceStrong().nextBytes(ivBytes);
            byte[] encrypted = new AESEncryptionDecryptionTool().encrypt(combined, tagKey, ivBytes, "AES/CBC/NOPADDING");
            return new ResponseEntity<>(new ServerSignatureResponse(ivBytes, encrypted), HttpStatus.OK);

        } catch (NonceExceededException nce) {
            return new ResponseEntity<>(new ApiResponse(false, "Number of nonces requested for the day exceeded."), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            throw new AssertionError("Errors should not happen.");
        }
    }

    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        try {
            Resource resource = fileStorageService.loadFileAsResource(fileName);
            String contentType = Magic.getMagicMatch(IOUtils.toByteArray(resource.getInputStream())).getMimeType();

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            throw new AssertionError("Should not happen.");
        }
    }
}