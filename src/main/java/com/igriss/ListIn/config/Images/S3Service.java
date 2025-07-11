package com.igriss.ListIn.config.Images;

import com.igriss.ListIn.chunker_client.ChunkerClient;
import com.igriss.ListIn.chunker_client.ChunkerResponse;
import com.igriss.ListIn.chunker_client.FileData;
import com.igriss.ListIn.exceptions.FailedToUploadFileException;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final ChunkerClient chunkerClient;

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.s3.bucket.link}")
    private String bucketLink;

    @Value("${cloud.aws.s3.cache-control}")
    private String cached;

    private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()); // Adjust thread pool size as needed

    //todo -> handle the case where S3 is out of memory
    public List<String> uploadFile(List<MultipartFile> files) {
        log.info("Starting sequential stream");

        return isVideoFile(files.get(0)) ? uploadVideo(files.get(0)) : uploadImage(files);

    }

    private List<String> uploadImage(List<MultipartFile> files) {
        return files.stream()
                .map(file -> {
                    log.info("Generating URL of file {}", file.getOriginalFilename());
                    String fileId = UUID.randomUUID() + "_" + System.currentTimeMillis();
                    String ext = FilenameUtils.getExtension(file.getOriginalFilename());
                    String fileName = fileId + "." + ext;
                    String fileUrl = String.format("%s/%s", bucketLink, fileName);

                    try {
                        byte[] fileData = file.getBytes();
                        String contentType = file.getContentType();

                        FileData multipartFile = FileData.builder()
                                .name(fileName)
                                .originalFilename(file.getOriginalFilename())
                                .content(fileData)
                                .contentType(contentType)
                                .build();

                        submitAsyncTask(fileName, multipartFile);

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    return fileUrl;

                }).toList();
    }

    private List<String> uploadVideo(MultipartFile f) {
        log.info("Video file sending to chunker service");
        ChunkerResponse chunkerResponse = chunkerClient.sendFileToChunker(f);
        List<MultipartFile> files = chunkerResponse.getFiles().stream().map(fileData -> (MultipartFile) fileData).toList();

        List<String> response = new ArrayList<>();

        files.forEach(file -> {

            String filename = file.getOriginalFilename();
            String fileUrl = String.format("%s/%s", bucketLink, filename);

            try {
                byte[] fileData = file.getBytes();
                String contentType = file.getContentType();

                FileData multipartFile = FileData.builder()
                        .name(file.getName())
                        .originalFilename(filename)
                        .content(fileData)
                        .contentType(contentType)
                        .build();


                log.info("File URL created {}", fileUrl);

                // Submit the async task to the executor
                submitAsyncTask(filename, multipartFile);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (fileUrl.endsWith(".m3u8")) response.add(fileUrl);

        });

        return response;
    }

    // Make sure to shut down the executor service properly to avoid memory leaks
    @PreDestroy
    public void shutdownExecutor() {
        try {
            log.info("Shutting down executor service...");
            executorService.shutdown();
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Error shutting down executor: {}", e.getMessage());
            executorService.shutdownNow();
        }
    }

    public List<String> getFileUrl(String uuid) {

        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(uuid)
                .build();
        ListObjectsV2Response response = s3Client.listObjectsV2(request);

        return response
                .contents()
                .stream()
                .map(s3Object -> String.format("%s/%s", bucketLink, s3Object.key()))
                .collect(Collectors.toList());
    }

    @Async
    public void deleteFiles(List<String> fileNames) {
        fileNames.forEach(fileName -> {
            try {
                String cleanFileName = fileName.split("\\?")[0];

                cleanFileName = URLDecoder.decode(cleanFileName, StandardCharsets.UTF_8);

                DeleteObjectRequest request = DeleteObjectRequest
                        .builder()
                        .bucket(bucketName)
                        .key(cleanFileName)
                        .build();

                s3Client.deleteObject(request);
            } catch (Exception e) {
                log.error("Failed to delete file: {} from bucket: {}. Error: {}",
                        fileName, bucketName, e.getMessage());
            }
        });
    }

    private void submitAsyncTask(String fileName, MultipartFile file) {
        executorService.submit(() -> {
            try {
                log.info("Uploading file {}", file.getOriginalFilename());
                saveFiles(fileName, file);
                log.info("Uploaded file {}", file.getOriginalFilename());
            } catch (Exception e) {
                log.error("Error processing file {}", file.getOriginalFilename() + e);
            }
        });
    }


    private void saveFiles(String fileName, MultipartFile file) {
        try {
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(fileName)
                            .contentType(file.getContentType())
                            .cacheControl(cached)
                            .contentDisposition("inline")
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            var exception = new FailedToUploadFileException("Uploading failed " + file.getOriginalFilename());
            log.error("Error processing file {}: {}", file.getOriginalFilename(), e.getMessage(), exception);
            throw exception;
        }
    }


    private boolean isVideoFile(MultipartFile file) {
        return switch (Objects.requireNonNull(FilenameUtils.getExtension(file.getOriginalFilename())).toLowerCase()) {
            case "mkv", "mp4", "mov" -> true;
            default -> false;
        };
    }

}