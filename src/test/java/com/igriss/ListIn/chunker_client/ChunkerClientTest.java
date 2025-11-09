package com.igriss.ListIn.chunker_client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ChunkerClientTest {

    private ChunkerClient chunkerClient;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        chunkerClient = new ChunkerClient(restTemplate);
    }


    @Test
    void sendFileToChunker_ioException_throwsRuntimeException() throws IOException {
        MockMultipartFile file = mock(MockMultipartFile.class);
        when(file.getBytes()).thenThrow(new IOException("mock exception"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> chunkerClient.sendFileToChunker(file));
        assertThat(ex.getMessage()).contains("Exception on sendFileToChunker");
        assertThat(ex.getCause()).isInstanceOf(IOException.class);
    }
}
