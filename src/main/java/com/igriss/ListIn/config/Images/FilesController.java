
package com.igriss.ListIn.config.Images;

import com.igriss.ListIn.publication.service.ProductFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/files")
@CrossOrigin(maxAge = 3600)
@RequiredArgsConstructor
public class FilesController {

    private final S3Service s3Service;
    private final ProductFileService productFileService;

    @PostMapping(value = "/upload/images")
    public ResponseEntity<List<String>> uploadImage(@RequestParam("images") List<MultipartFile> files) {
        return ResponseEntity.ok(productFileService.uploadImageURLs(files));
    }

    @PostMapping(value = "/upload/video")
    public ResponseEntity<?> uploadVideo(@RequestParam(name = "video") MultipartFile video) {
        return ResponseEntity.ok(productFileService.uploadVideoURL(video));
    }

    @GetMapping("/download")
    public ResponseEntity<String> getFileUrl(@RequestParam("id") String id) {
        return ResponseEntity.ok(s3Service.getFileUrl(id).toString());
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteFile(@RequestParam("id") String id) {

        s3Service.deleteFiles(Collections.singletonList(id));

        return ResponseEntity.ok("File deleted successfully");
    }
}