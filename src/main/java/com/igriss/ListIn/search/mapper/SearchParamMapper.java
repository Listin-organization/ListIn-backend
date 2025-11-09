package com.igriss.ListIn.search.mapper;

import com.igriss.ListIn.search.service.supplier.SearchParams;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SearchParamMapper {

    public SearchParams toSearchParams(UUID pCategory, UUID category,
                                       String query, Boolean bargain,
                                       String productCondition, Float from,
                                       Float to, String locationName,
                                       Boolean isFree, Map<String, String> locations,
                                       String sellerType, Map<String, List<String>> filters,
                                       Map<String, String[]> numericFilter) {
        return SearchParams.builder()
                .parentCategory(pCategory)
                .category(category)
                .input(query)
                .bargain(bargain)
                .productCondition(productCondition)
                .priceFrom(from)
                .priceTo(to)
                .locationName(locationName)
                .isFree(isFree)
                .sellerType(sellerType)
                .locationIds(locations)
                .filters(filters)
                .numericFilter(numericFilter)
                .build();
    }
}
