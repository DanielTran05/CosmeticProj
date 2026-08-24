package com.dtp.cosmemgt.core.controller;

import com.dtp.cosmemgt.core.dto.ApiResponse;
import com.dtp.cosmemgt.core.commonService.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/uploads")
@RequiredArgsConstructor
public class UploadController {
    private final CloudinaryService cloudinaryUtils;

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "empty") String folder,
            @RequestParam(value = "objectId", required = false) String objectId){
        
        String imageUrl = cloudinaryUtils.uploadImg(file, folder, objectId);
        return ApiResponse.<String>builder()
                .result(imageUrl)
                .build();
    }
}