package com.igriss.ListIn.publication.repository;

import com.igriss.ListIn.publication.entity.Publication;
import com.igriss.ListIn.publication.entity.static_entity.Category;
import com.igriss.ListIn.publication.enums.PublicationStatus;
import com.igriss.ListIn.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;



import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PublicationRepository extends JpaRepository<Publication, UUID> {
    Optional<Publication> findByIdOrderByDateUpdatedDesc(UUID id);

    Page<Publication> findAllBySeller(Pageable pageable, User seller);

    Page<Publication> findAllByOrderByDatePostedDesc(Pageable pageable);


    @Modifying(clearAutomatically = true)
    @Query(value = """
            UPDATE publications
            SET  title = CASE
                    WHEN CAST(:title AS varchar) IS NOT NULL THEN CAST(:title AS varchar)
                    ELSE title
                END,
                  description = CASE
                    WHEN CAST(:description AS varchar) IS NOT NULL THEN CAST(:description AS varchar)
                    ELSE description
                END,
                price = CASE
                    WHEN CAST(:price AS float) IS NOT NULL THEN CAST(:price AS float)
                    ELSE price
                END,
                bargain = CASE
                    WHEN CAST(:bargain AS boolean) IS NOT NULL THEN CAST(:bargain AS boolean)
                    ELSE bargain
                END,
                product_condition = CASE
                    WHEN CAST(:productCondition AS varchar) IS NOT NULL THEN CAST(:productCondition AS varchar)
                    ELSE product_condition
                END,
                aspect_ration = CASE
                    WHEN CAST(:aspectRation AS double precision) IS NOT NULL THEN CAST(:aspectRation AS double precision)
                    ELSE aspect_ration
                END
            WHERE publication_id = :publicationId
            """, nativeQuery = true)
    Integer updatePublicationById(UUID publicationId, String title, String description, Float price, Boolean bargain, String productCondition, Double aspectRation);

    Page<Publication> findAllByCategory_ParentCategory_Id(UUID parentCategoryId, Pageable pageable);

    Page<Publication> findAllBySeller_UserId(UUID sellerUserId, Pageable pageable);

    @Modifying
    @Query(value = """
            UPDATE publications
            SET likes = likes + 1
            WHERE publication_id = :publicationId
            """, nativeQuery = true)
    Integer incrementLike(UUID publicationId);

    @Modifying
    @Query(value = """
            UPDATE publications
            SET likes = likes - 1
            WHERE publication_id = :publicationId
            """, nativeQuery = true)
    Integer decrementLike(UUID publicationId);

    List<Publication> findAllByIdInOrderByDatePosted(List<UUID> publicationIds);

    
    List<Publication> findByPriceBetweenAndPublicationStatus(Float minPrice, Float maxPrice, PublicationStatus publicationStatus, PageRequest of);

    @Query(value = """
        SELECT p FROM Publication p
        WHERE p.category IN :categories
        AND p.publicationStatus = :status
        """)
    Page<Publication> findByCategoryInAndPublicationStatus(
            List<Category> categories,
            PublicationStatus status,
            Pageable pageable
    );

    Page<Publication> findBySeller_UserIdInOrderByDatePostedDesc(List<UUID> userIds, Pageable pageable);

}