package com.igriss.ListIn.publication.controller;

import com.igriss.ListIn.publication.entity.static_entity.Category;
import com.igriss.ListIn.publication.repository.CategoryRepository;
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

class CategoryControllerTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryController controller;

    private UUID parentId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        parentId = UUID.randomUUID();
    }

    @Test
    void testGetParents_ReturnsParentCategories() {
        Category parent1 = Category.builder().id(UUID.randomUUID()).name("Parent1").build();
        Category parent2 = Category.builder().id(UUID.randomUUID()).name("Parent2").build();

        when(categoryRepository.findAllParentCategories()).thenReturn(List.of(parent1, parent2));

        ResponseEntity<List<Category>> response = controller.getParents();

        assertEquals(200, response.getStatusCode().value());
        Assertions.assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("Parent1", response.getBody().get(0).getName());
        verify(categoryRepository, times(1)).findAllParentCategories();
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    void testGetChildren_ReturnsChildCategories() {
        Category child1 = Category.builder().id(UUID.randomUUID()).name("Child1").build();
        Category child2 = Category.builder().id(UUID.randomUUID()).name("Child2").build();

        when(categoryRepository.findAllByParentCategory_Id(parentId)).thenReturn(List.of(child1, child2));

        ResponseEntity<List<Category>> response = controller.getChildren(parentId);

        assertEquals(200, response.getStatusCode().value());
        Assertions.assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("Child1", response.getBody().get(0).getName());
        verify(categoryRepository, times(1)).findAllByParentCategory_Id(parentId);
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    void testGetChildren_ReturnsEmptyList() {
        when(categoryRepository.findAllByParentCategory_Id(parentId)).thenReturn(List.of());

        ResponseEntity<List<Category>> response = controller.getChildren(parentId);

        assertEquals(200, response.getStatusCode().value());
        Assertions.assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());
        verify(categoryRepository, times(1)).findAllByParentCategory_Id(parentId);
        verifyNoMoreInteractions(categoryRepository);
    }
}
