package com.igriss.ListIn.publication.service;

import com.igriss.ListIn.publication.dto.BrandModelDTO;
import com.igriss.ListIn.publication.dto.GroupedAttributeDTO;
import com.igriss.ListIn.publication.entity.AttributeValue;
import com.igriss.ListIn.publication.repository.CategoryAttributeRepository;
import com.igriss.ListIn.publication.service_impl.CategoryAttributeService;
import org.instancio.Instancio;
import org.instancio.junit.InstancioExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(InstancioExtension.class)
class CategoryAttributeServiceTest {

    @Mock
    private CategoryAttributeRepository repository;

    @Mock
    private BrandModelService brandModelService;

    @InjectMocks
    private CategoryAttributeService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetGroupedAttributesByCategoryId_withMultipleRecordsAndValidValues() {
        UUID categoryId = UUID.randomUUID();
        UUID keyId = UUID.randomUUID();
        UUID valueId = UUID.randomUUID();

        Object[] row = new Object[25];
        row[0] = keyId;
        row[1] = "Processor Brand";
        row[2] = "Helper";
        row[3] = "SubHelper";
        row[4] = "widget";
        row[5] = "subWidget";
        row[6] = "filter";
        row[7] = "subFilter";
        row[8] = "filterWidget";
        row[9] = "subFilterWidget";
        row[10] = "string";
        row[11] = valueId;
        row[12] = "Intel";
        row[13] = "Intel UZ";
        row[14] = "Intel RU";
        row[15] = "Helper UZ";
        row[16] = "SubHelper UZ";
        row[17] = "Helper RU";
        row[18] = "SubHelper RU";
        row[19] = "Filter UZ";
        row[20] = "SubFilter UZ";
        row[21] = "Filter RU";
        row[22] = "SubFilter RU";
        row[23] = "Name UZ";
        row[24] = "Name RU";

        when(repository.findAttributeKeysAndValuesByCategoryId(categoryId))
                .thenReturn(Collections.singletonList(row));

        List<BrandModelDTO> mockBrandModels = List.of(Instancio.create(BrandModelDTO.class));
        when(brandModelService.getModels(any(AttributeValue.class))).thenReturn(mockBrandModels);

        List<GroupedAttributeDTO> result = service.getGroupedAttributesByCategoryId(categoryId);

        assertThat(result).hasSize(1);
        GroupedAttributeDTO dto = result.get(0);
        assertThat(dto.getAttributeKey()).isEqualTo("Processor Brand");
        assertThat(dto.getValues()).hasSize(1);

        GroupedAttributeDTO.AttributeValueDTO valDto = dto.getValues().get(0);
        assertThat(valDto.getAttributeValueId()).isEqualTo(valueId.toString());
        assertThat(valDto.getList()).containsExactlyElementsOf(mockBrandModels);

        verify(repository).findAttributeKeysAndValuesByCategoryId(categoryId);
        verify(brandModelService).getModels(any(AttributeValue.class));
    }

    @Test
    void testGetGroupedAttributesByCategoryId_withNullValueId_skipsBrandModelService() {
        UUID categoryId = UUID.randomUUID();
        UUID keyId = UUID.randomUUID();

        Object[] row = new Object[25];
        row[0] = keyId;
        row[1] = "RAM Size";
        row[2] = "Helper";
        row[3] = "SubHelper";
        row[4] = "widget";
        row[5] = "subWidget";
        row[6] = "filter";
        row[7] = "subFilter";
        row[8] = "filterWidget";
        row[9] = "subFilterWidget";
        row[10] = "integer";
        row[11] = null;
        row[12] = null;
        row[13] = null;
        row[14] = null;
        row[15] = "Helper UZ";
        row[16] = "SubHelper UZ";
        row[17] = "Helper RU";
        row[18] = "SubHelper RU";
        row[19] = "Filter UZ";
        row[20] = "SubFilter UZ";
        row[21] = "Filter RU";
        row[22] = "SubFilter RU";
        row[23] = "Name UZ";
        row[24] = "Name RU";

        when(repository.findAttributeKeysAndValuesByCategoryId(categoryId))
                .thenReturn(Collections.singletonList(row));

        List<GroupedAttributeDTO> result = service.getGroupedAttributesByCategoryId(categoryId);

        assertThat(result).hasSize(1);
        GroupedAttributeDTO dto = result.get(0);
        assertThat(dto.getValues()).isEmpty();

        verify(repository).findAttributeKeysAndValuesByCategoryId(categoryId);
        verifyNoInteractions(brandModelService);
    }

    @Test
    void testGetGroupedAttributesByCategoryId_withMultipleSameAttributeKeys_groupsProperly() {
        UUID categoryId = UUID.randomUUID();
        UUID keyId = UUID.randomUUID();

        Object[] row1 = new Object[25];
        Object[] row2 = new Object[25];

        for (Object[] row : List.of(row1, row2)) {
            row[0] = keyId;
            row[1] = "Storage";
            row[2] = "helper";
            row[3] = "sub";
            row[4] = "widget";
            row[5] = "subWidget";
            row[6] = "filter";
            row[7] = "subFilter";
            row[8] = "fw";
            row[9] = "sfw";
            row[10] = "string";
            row[15] = "HelperUZ";
            row[16] = "SubHelperUZ";
            row[17] = "HelperRU";
            row[18] = "SubHelperRU";
            row[19] = "FilterUZ";
            row[20] = "SubFilterUZ";
            row[21] = "FilterRU";
            row[22] = "SubFilterRU";
            row[23] = "NameUZ";
            row[24] = "NameRU";
        }

        row1[11] = UUID.randomUUID();
        row1[12] = "256GB";
        row1[13] = "256GB UZ";
        row1[14] = "256GB RU";

        row2[11] = UUID.randomUUID();
        row2[12] = "512GB";
        row2[13] = "512GB UZ";
        row2[14] = "512GB RU";

        when(repository.findAttributeKeysAndValuesByCategoryId(categoryId))
                .thenReturn(List.of(row1, row2));

        when(brandModelService.getModels(any(AttributeValue.class)))
                .thenReturn(Collections.emptyList());

        List<GroupedAttributeDTO> result = service.getGroupedAttributesByCategoryId(categoryId);

        assertThat(result).hasSize(1);
        GroupedAttributeDTO dto = result.get(0);
        assertThat(dto.getValues()).hasSize(2);
        assertThat(dto.getValues().get(0).getValue()).isEqualTo("256GB");
        assertThat(dto.getValues().get(1).getValue()).isEqualTo("512GB");

        verify(repository).findAttributeKeysAndValuesByCategoryId(categoryId);
        verify(brandModelService, times(2)).getModels(any(AttributeValue.class));
    }

    @Test
    void testGetGroupedAttributesByCategoryId_withEmptyResult_returnsEmptyList() {
        UUID categoryId = UUID.randomUUID();
        when(repository.findAttributeKeysAndValuesByCategoryId(categoryId)).thenReturn(Collections.emptyList());

        var result = service.getGroupedAttributesByCategoryId(categoryId);

        assertThat(result).isEmpty();
        verify(repository).findAttributeKeysAndValuesByCategoryId(categoryId);
        verifyNoInteractions(brandModelService);
    }
}
