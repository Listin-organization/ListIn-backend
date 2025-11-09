package com.igriss.ListIn.chunker_client;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileDataTest {

    private final byte[] sampleContent = "Hello World".getBytes();

    @Test
    void testGetters() throws IOException {
        FileData fileData = FileData.builder()
                .name("file")
                .originalFilename("test.txt")
                .contentType("text/plain")
                .content(sampleContent)
                .build();

        assertThat(fileData.getName()).isEqualTo("file");
        assertThat(fileData.getOriginalFilename()).isEqualTo("test.txt");
        assertThat(fileData.getContentType()).isEqualTo("text/plain");
        assertThat(fileData.isEmpty()).isFalse();
        assertThat(fileData.getSize()).isEqualTo(sampleContent.length);
        assertThat(fileData.getBytes()).isEqualTo(sampleContent);

        try (var is = fileData.getInputStream(); var os = new ByteArrayOutputStream()) {
            is.transferTo(os);
            assertThat(os.toByteArray()).isEqualTo(sampleContent);
        }
    }

    @Test
    void testIsEmptyTrue() {
        FileData emptyFile = FileData.builder()
                .name("file")
                .originalFilename("empty.txt")
                .contentType("text/plain")
                .content(new byte[0])
                .build();

        assertThat(emptyFile.isEmpty()).isTrue();
        assertThat(emptyFile.getSize()).isEqualTo(0);
    }

    @Test
    void testTransferToFile() throws IOException {
        File tempFile = File.createTempFile("fileDataTest", ".txt");
        tempFile.deleteOnExit();

        FileData fileData = FileData.builder()
                .name("file")
                .originalFilename("test.txt")
                .contentType("text/plain")
                .content(sampleContent)
                .build();

        fileData.transferTo(tempFile);

        try (FileInputStream fis = new FileInputStream(tempFile)) {
            byte[] fileBytes = fis.readAllBytes();
            assertThat(fileBytes).isEqualTo(sampleContent);
        }
    }

    @Test
    void testTransferToThrowsIOException() {
        FileData fileData = FileData.builder()
                .name("file")
                .originalFilename("test.txt")
                .contentType("text/plain")
                .content(sampleContent)
                .build();

        File dir = new File(System.getProperty("java.io.tmpdir"));
        assertThrows(IOException.class, () -> fileData.transferTo(dir));
    }
}
