package com.igriss.ListIn.search.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FoundPublicationsDTO {

    private Long foundPublications;

    private Float priceFrom;

    private Float priceTo;
}
