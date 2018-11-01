package org.cs4239.team1.protectPMLeefrontendserver.controller;

import org.cs4239.team1.protectPMLeefrontendserver.payload.UploadFileResponse;
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
import java.io.IOException;

@RestController
@RequestMapping("/api/external")
public class ExternalPartnerController {
    private static final Logger logger = LoggerFactory.getLogger(ExternalPartnerController.class);

    @Autowired
    private ExternalPartnerService externalPartnerService;

    @PostMapping("/upload/{type}")
    public UploadFileResponse externalPartnerFile(@PathVariable String type, @RequestParam("file") MultipartFile file) throws IOException {
        String fileName = externalPartnerService.externalPartnerFile(type, file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/upload/")
                .path(fileName)
                .toUriString();

        return new UploadFileResponse(fileName, fileDownloadUri, file.getContentType(), file.getSize());
    }


}
