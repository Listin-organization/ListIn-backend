package com.igriss.ListIn.publication.repository;

import com.igriss.ListIn.publication.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {

    @Query("""
    select distinct pv from ProductVariant pv
    left join fetch pv.images
    where pv.publication.id = :publicationId
    """)
    List<ProductVariant> findByPublication_Id(UUID publicationId);
}