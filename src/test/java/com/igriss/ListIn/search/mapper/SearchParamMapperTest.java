package com.igriss.ListIn.search.mapper;

import com.igriss.ListIn.search.service.supplier.SearchParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class SearchParamMapperTest {

    private SearchParamMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new SearchParamMapper();
    }

    @Test
    void toSearchParams_mapsAllFieldsCorrectly() {
        UUID parentCategoryId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        String query = "testQuery";
        Boolean bargain = true;
        String productCondition = "NEW_PRODUCT";
        Float from = 10.0F;
        Float to = 100.0F;
        String locationName = "Test City";
        Boolean isFree = false;
        String sellerType = "BUSINESS";
        Map<String, String> locationIds = Map.of("country", "id1", "state", "id2");
        Map<String, List<String>> filters = Map.of("color", List.of("red", "blue"));
        Map<String, String[]> numericFilter = Map.of("weight", new String[]{"1", "5"});

        SearchParams params = mapper.toSearchParams(
                parentCategoryId, categoryId, query, bargain,
                productCondition, from, to, locationName,
                isFree, locationIds, sellerType, filters, numericFilter
        );

        assertThat(params.getParentCategory()).isEqualTo(parentCategoryId);
        assertThat(params.getCategory()).isEqualTo(categoryId);
        assertThat(params.getInput()).isEqualTo(query);
        assertThat(params.getBargain()).isEqualTo(bargain);
        assertThat(params.getProductCondition()).isEqualTo(productCondition);
        assertThat(params.getPriceFrom()).isEqualTo(from);
        assertThat(params.getPriceTo()).isEqualTo(to);
        assertThat(params.getLocationName()).isEqualTo(locationName);
        assertThat(params.getIsFree()).isEqualTo(isFree);
        assertThat(params.getSellerType()).isEqualTo(sellerType);
        assertThat(params.getLocationIds()).isEqualTo(locationIds);
        assertThat(params.getFilters()).isEqualTo(filters);
        assertThat(params.getNumericFilter()).isEqualTo(numericFilter);
    }
}
