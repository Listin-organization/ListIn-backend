package com.igriss.ListIn.publication.repository;

import com.igriss.ListIn.publication.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {

    List<ProductVariant> findByPublication_Id(UUID publicationId);
}