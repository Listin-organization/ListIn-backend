package com.igriss.ListIn.publication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class ProductVariantRequestDTO {

    private String size;

    private String shoeSize;

    private String color;

    private List<String> imageUrls;

    private Double price;

    private Double discountPrice;

    private Integer stock;

    private String sku;
}
