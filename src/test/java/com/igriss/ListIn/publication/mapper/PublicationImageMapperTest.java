package com.igriss.ListIn.publication.mapper;

import com.igriss.ListIn.publication.dto.ImageDTO;
import com.igriss.ListIn.publication.entity.Publication;
import com.igriss.ListIn.publication.entity.PublicationImage;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PublicationImageMapperTest {

    private final PublicationImageMapper mapper = new PublicationImageMapper();

    @Test
    void toProductImage_shouldMapCorrectly() {
        Publication publication = new Publication();
        publication.setId(UUID.randomUUID());

        String url = "https://cdn.example.com/images/pic1.jpg";

        PublicationImage image = mapper.toProductImage(url, publication);

        assertNotNull(image);
        assertEquals(url, image.getImageUrl());
        assertEquals("pic1.jpg", image.getImageName());
        assertEquals(publication, image.getPublication());
    }

    @Test
    void toImageDTOList_shouldMapListCorrectly() {
        PublicationImage img1 = PublicationImage.builder()
                .imageId(UUID.randomUUID())
                .imageUrl("url1")
                .isPrimaryImage(true)
                .build();

        PublicationImage img2 = PublicationImage.builder()
                .imageId(UUID.randomUUID())
                .imageUrl("url2")
                .isPrimaryImage(false)
                .build();

        List<ImageDTO> dtoList = mapper.toImageDTOList(List.of(img1, img2));

        assertEquals(2, dtoList.size());

        ImageDTO dto1 = dtoList.get(0);
        assertTrue(dto1.getIsPrimary());
        assertEquals("url1", dto1.getUrl());

        ImageDTO dto2 = dtoList.get(1);
        assertFalse(dto2.getIsPrimary());
        assertEquals("url2", dto2.getUrl());
    }

    @Test
    void toImageDTOList_shouldReturnEmptyList_whenEmptyInput() {
        List<ImageDTO> result = mapper.toImageDTOList(List.of());
        assertTrue(result.isEmpty());
    }
}
