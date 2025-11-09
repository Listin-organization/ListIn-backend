package com.igriss.ListIn.publication.controller;

import com.igriss.ListIn.publication.dto.GroupedAttributeDTO;
import com.igriss.ListIn.publication.service_impl.CategoryAttributeService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class CategoryAttributeControllerTest {

    @Mock
    private CategoryAttributeService service;

    @InjectMocks
    private CategoryAttributeController controller;

    private UUID categoryId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        categoryId = UUID.randomUUID();
    }

    @Test
    void testGetGroupedAttributesByCategory_ReturnsOkResponse() {
        GroupedAttributeDTO.AttributeValueDTO valueDTO = new GroupedAttributeDTO.AttributeValueDTO(
                "valueId1", "keyId1", "Value", "ValueUz", "ValueRu", List.of()
        );
        GroupedAttributeDTO dto = GroupedAttributeDTO.builder()
                .attributeKey("Color")
                .values(List.of(valueDTO))
                .build();

        when(service.getGroupedAttributesByCategoryId(categoryId))
                .thenReturn(List.of(dto));

        ResponseEntity<List<GroupedAttributeDTO>> response = controller.getGroupedAttributesByCategory(categoryId);

        assertEquals(200, response.getStatusCode().value());
        Assertions.assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Color", response.getBody().get(0).getAttributeKey());

        verify(service, times(1)).getGroupedAttributesByCategoryId(categoryId);
        verifyNoMoreInteractions(service);
    }

    @Test
    void testGetGroupedAttributesByCategory_ReturnsEmptyList() {
        when(service.getGroupedAttributesByCategoryId(categoryId))
                .thenReturn(List.of());

        ResponseEntity<List<GroupedAttributeDTO>> response = controller.getGroupedAttributesByCategory(categoryId);

        assertEquals(200, response.getStatusCode().value());
        Assertions.assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());

        verify(service, times(1)).getGroupedAttributesByCategoryId(categoryId);
        verifyNoMoreInteractions(service);
    }
}
