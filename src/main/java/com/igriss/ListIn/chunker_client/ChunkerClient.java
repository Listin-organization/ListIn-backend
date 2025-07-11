package com.igriss.ListIn.chunker_client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
@Component
@RequiredArgsConstructor
public class ChunkerClient {

    private final RestTemplate restTemplate;

    @Value("${chunker-service.url}")
    private String targetUrl;

    public ChunkerResponse sendFileToChunker(MultipartFile file) {
        try {
            ByteArrayResource fileAsResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", fileAsResource);

            // Create headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // Create the request entity
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<byte[]> response = restTemplate.postForEntity(targetUrl, requestEntity, byte[].class);
            // Create the body

            return parseMultipartResponse(response);
        } catch (IOException e) {
            throw new RuntimeException("Exception on sendFileToChunker(...)", e);
        }
    }


    private ChunkerResponse parseMultipartResponse(ResponseEntity<byte[]> response) {
        String responseBody = new String(response.getBody());

        // Extract boundary from Content-Type header
        String contentType = response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
        String boundary = extractBoundary(contentType);

        if (boundary == null) {
            throw new IllegalArgumentException("No boundary found in Content-Type header");
        }

        log.info("Boundary: {}", boundary);

        // Split the response by boundary
        String[] parts = responseBody.split("--" + Pattern.quote(boundary));

        ChunkerResponse chunkerResponse = new ChunkerResponse();
        List<FileData> files = new ArrayList<>();

        for (String part : parts) {
            if (part.trim().isEmpty() || part.trim().equals("--")) {
                continue;
            }

            FileData fileData = parsePart(part);
            if (fileData != null) {
                files.add(fileData);
            }
        }

        chunkerResponse.setFiles(files);
        return chunkerResponse;
    }

    private String extractBoundary(String contentType) {
        if (contentType == null) return null;

        Pattern boundaryPattern = Pattern.compile("boundary=([^;]+)");
        Matcher matcher = boundaryPattern.matcher(contentType);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    private FileData parsePart(String part) {
        try {
            // Split headers and content
            String[] headerAndContent = part.split("\r\n\r\n", 2);
            if (headerAndContent.length != 2) {
                return null;
            }

            String headers = headerAndContent[0];
            String content = headerAndContent[1];

            // Extract filename from Content-Disposition header
            String filename = extractFilename(headers);
            String contentType = extractContentType(headers);

            if (filename != null) {
                var fileData = FileData.builder()
                        .originalFilename(filename)
                        .name(filename)
                        .contentType(contentType)
                        .content(content.getBytes())
                        .build();

                log.info("Parsed file: {} ({})", filename, contentType);
                return fileData;
            }
        } catch (Exception e) {
            log.warn("Failed to parse part: {}", e.getMessage());
        }
        return null;
    }

    private String extractFilename(String headers) {
        Pattern filenamePattern = Pattern.compile("filename=\"([^\"]+)\"");
        Matcher matcher = filenamePattern.matcher(headers);

        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String extractContentType(String headers) {
        Pattern contentTypePattern = Pattern.compile("Content-Type:\\s*([^\\r\\n]+)");
        Matcher matcher = contentTypePattern.matcher(headers);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "application/octet-stream";
    }

    // Method to save files to disk
    public void saveFiles(ChunkerResponse response, String outputDirectory) throws IOException {
        Path outputPath = Paths.get(outputDirectory);
        Files.createDirectories(outputPath);

        for (FileData file : response.getFiles()) {
            Path filePath = outputPath.resolve(file.getName());
            Files.write(filePath, file.getBytes());
            log.info("Saved file: {}", filePath);
        }
    }

}