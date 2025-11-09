package com.igriss.ListIn.publication.mapper;

import com.igriss.ListIn.publication.dto.CategoryDTO;
import com.igriss.ListIn.publication.entity.static_entity.Category;
import com.igriss.ListIn.publication.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CategoryMapperTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryMapper categoryMapper;

    private UUID categoryId;
    private Category parent;
    private Category category;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        categoryId = UUID.randomUUID();

        parent = new Category();
        parent.setId(UUID.randomUUID());
        parent.setName("Parent");

        category = new Category();
        category.setId(categoryId);
        category.setName("Child");
        category.setParentCategory(parent);
    }

    @Test
    void toCategory_shouldReturnCategory() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        Category result = categoryMapper.toCategory(categoryId);

        assertNotNull(result);
        assertEquals(categoryId, result.getId());
        verify(categoryRepository).findById(categoryId);
    }

    @Test
    void toCategory_shouldThrowException_whenNotFound() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> categoryMapper.toCategory(categoryId));
    }

    @Test
    void toCategoryResponseDTO_shouldMapCorrectly() {
        CategoryDTO dto = categoryMapper.toCategoryResponseDTO(category);

        assertEquals(category.getId(), dto.getId());
        assertEquals(category.getName(), dto.getName());
        assertEquals(category.getParentCategory().getId(), dto.getParentCategoryId());
    }
}
