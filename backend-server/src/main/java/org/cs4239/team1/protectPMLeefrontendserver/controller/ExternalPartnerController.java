package org.cs4239.team1.protectPMLeefrontendserver.controller;

import java.io.IOException;

import org.cs4239.team1.protectPMLeefrontendserver.model.User;
import org.cs4239.team1.protectPMLeefrontendserver.payload.UploadFileResponse;
import org.cs4239.team1.protectPMLeefrontendserver.security.CurrentUser;
import org.cs4239.team1.protectPMLeefrontendserver.service.ExternalPartnerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/external")
public class ExternalPartnerController {
    private static final Logger logger = LoggerFactory.getLogger(ExternalPartnerController.class);

    @Autowired
    private ExternalPartnerService externalPartnerService;

    @PostMapping("/upload/{type}")
    public UploadFileResponse externalPartnerFile(@CurrentUser User currentUser, @PathVariable String type, @RequestParam("file") MultipartFile file) throws IOException {
        logger.info("NRIC_" + currentUser.getNric() + " ROLE_" + currentUser.getSelectedRole() + " accessing ExternalPartnerController#externalPartnerFile", type, file);
        String fileName = externalPartnerService.externalPartnerFile(type, file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/upload/")
                .path(fileName)
                .toUriString();

        return new UploadFileResponse(fileName, fileDownloadUri, file.getContentType(), file.getSize());
    }


}
