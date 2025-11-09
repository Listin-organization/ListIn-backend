package com.igriss.ListIn.publication.controller;

import com.igriss.ListIn.publication.dto.category_tree.ChildNode;
import com.igriss.ListIn.publication.dto.category_tree.ParentNode;
import com.igriss.ListIn.publication.service_impl.CategoryTreeService;
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

class CategoryTreeControllerTest {

    @Mock
    private CategoryTreeService categoryTreeService;

    @InjectMocks
    private CategoryTreeController controller;

    private UUID parentId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        parentId = UUID.randomUUID();
    }

    @Test
    void testGetCategoryTree_ReturnsParentNodes() {
        ChildNode child = ChildNode.builder().id(UUID.randomUUID()).name("Child1").build();
        ParentNode parent = ParentNode.builder()
                .id(parentId)
                .name("Parent1")
                .childCategories(List.of(child))
                .build();

        when(categoryTreeService.getCategoryTree()).thenReturn(List.of(parent));

        ResponseEntity<List<ParentNode>> response = controller.getCategoryTree();

        assertEquals(200, response.getStatusCode().value());
        Assertions.assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Parent1", response.getBody().get(0).getName());
        assertEquals(1, response.getBody().get(0).getChildCategories().size());
        assertEquals("Child1", response.getBody().get(0).getChildCategories().get(0).getName());

        verify(categoryTreeService, times(1)).getCategoryTree();
        verifyNoMoreInteractions(categoryTreeService);
    }

    @Test
    void testGetCategoryTree_ReturnsEmptyList() {
        when(categoryTreeService.getCategoryTree()).thenReturn(List.of());

        ResponseEntity<List<ParentNode>> response = controller.getCategoryTree();

        assertEquals(200, response.getStatusCode().value());
        Assertions.assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());

        verify(categoryTreeService, times(1)).getCategoryTree();
        verifyNoMoreInteractions(categoryTreeService);
    }
}
