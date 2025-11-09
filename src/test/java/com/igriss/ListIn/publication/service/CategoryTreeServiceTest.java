package com.igriss.ListIn.publication.service;


import com.igriss.ListIn.publication.dto.GroupedAttributeDTO;
import com.igriss.ListIn.publication.dto.NumericFieldDTO;
import com.igriss.ListIn.publication.dto.category_tree.ChildNode;
import com.igriss.ListIn.publication.dto.category_tree.ParentNode;
import com.igriss.ListIn.publication.entity.NumericField;
import com.igriss.ListIn.publication.entity.static_entity.Category;
import com.igriss.ListIn.publication.repository.CategoryRepository;
import com.igriss.ListIn.publication.repository.NumericFieldRepository;
import com.igriss.ListIn.publication.service_impl.CategoryAttributeService;
import com.igriss.ListIn.publication.service_impl.CategoryTreeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryTreeServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryAttributeService categoryAttributeService;

    @Mock
    private NumericFieldRepository numericFieldRepository;

    @InjectMocks
    private CategoryTreeService categoryTreeService;

    private Category parentCategory;
    private Category childCategory;
    private NumericField numericField;

    @BeforeEach
    void setUp() {
        parentCategory = Category.builder()
                .id(UUID.randomUUID())
                .name("Electronics")
                .nameUz("Elektronika")
                .nameRu("Электроника")
                .description("Parent category")
                .imageUrl("parent.png")
                .build();

        childCategory = Category.builder()
                .id(UUID.randomUUID())
                .name("Smartphones")
                .nameUz("Smartfonlar")
                .nameRu("Смартфоны")
                .description("Child category")
                .imageUrl("child.png")
                .parentCategory(parentCategory)
                .build();

        numericField = NumericField.builder()
                .id(UUID.randomUUID())
                .fieldName("Battery Capacity")
                .fieldNameUz("Batareya sig'imi")
                .fieldNameRu("Ёмкость батареи")
                .description("Measured in mAh")
                .category(childCategory)
                .build();
    }

    @Test
    void shouldReturnCategoryTreeSuccessfully() {
        List<Category> parentCategories = List.of(parentCategory);
        List<Category> childCategories = List.of(childCategory);

        GroupedAttributeDTO attributeDTO = GroupedAttributeDTO.builder()
                .attributeKey("Color")
                .attributeKeyUz("Rang")
                .attributeKeyRu("Цвет")
                .values(List.of())
                .build();

        NumericFieldDTO numericFieldDTO = NumericFieldDTO.builder()
                .id(numericField.getId())
                .fieldName(numericField.getFieldName())
                .description(numericField.getDescription())
                .build();

        when(categoryRepository.findAllParentCategories()).thenReturn(parentCategories);
        when(categoryRepository.findAllByParentCategory_Id(parentCategory.getId())).thenReturn(childCategories);
        when(categoryAttributeService.getGroupedAttributesByCategoryId(childCategory.getId()))
                .thenReturn(List.of(attributeDTO));
        when(numericFieldRepository.findAllByCategory_Id(childCategory.getId()))
                .thenReturn(List.of(numericField));

        List<ParentNode> result = categoryTreeService.getCategoryTree();

        assertThat(result).hasSize(1);
        ParentNode parentNode = result.get(0);
        assertThat(parentNode.getName()).isEqualTo("Electronics");
        assertThat(parentNode.getChildCategories()).hasSize(1);

        ChildNode childNode = parentNode.getChildCategories().get(0);
        assertThat(childNode.getName()).isEqualTo("Smartphones");
        assertThat(childNode.getAttributes()).hasSize(1);
        assertThat(childNode.getNumericFields()).hasSize(1);
        assertThat(childNode.getNumericFields().get(0).getFieldName()).isEqualTo("Battery Capacity");

        verify(categoryRepository, times(1)).findAllParentCategories();
        verify(categoryRepository, times(1)).findAllByParentCategory_Id(parentCategory.getId());
        verify(categoryAttributeService, times(1)).getGroupedAttributesByCategoryId(childCategory.getId());
        verify(numericFieldRepository, times(1)).findAllByCategory_Id(childCategory.getId());
    }

    @Test
    void shouldReturnEmptyListWhenNoParentCategoriesFound() {
        when(categoryRepository.findAllParentCategories()).thenReturn(Collections.emptyList());

        List<ParentNode> result = categoryTreeService.getCategoryTree();

        assertThat(result).isEmpty();
        verify(categoryRepository, times(1)).findAllParentCategories();
        verifyNoInteractions(categoryAttributeService, numericFieldRepository);
    }

    @Test
    void shouldHandleParentWithNoChildCategories() {
        when(categoryRepository.findAllParentCategories()).thenReturn(List.of(parentCategory));
        when(categoryRepository.findAllByParentCategory_Id(parentCategory.getId())).thenReturn(Collections.emptyList());

        List<ParentNode> result = categoryTreeService.getCategoryTree();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getChildCategories()).isEmpty();
    }
}
