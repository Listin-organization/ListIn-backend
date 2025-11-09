package com.igriss.ListIn.publication.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.igriss.ListIn.comment.entity.Comment;
import com.igriss.ListIn.location.entity.Country;
import com.igriss.ListIn.location.entity.County;
import com.igriss.ListIn.location.entity.State;
import com.igriss.ListIn.publication.entity.static_entity.Category;
import com.igriss.ListIn.publication.enums.ProductCondition;
import com.igriss.ListIn.publication.enums.PublicationStatus;
import com.igriss.ListIn.publication.enums.PublicationType;
import com.igriss.ListIn.user.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@Entity
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "publications")
@EntityListeners(AuditingEntityListener.class)
public class Publication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "publication_id")
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 5000)
    private String description;

    private Float price;

    @Column(nullable = false)
    private Boolean bargain;

    @Enumerated(EnumType.STRING)
    private PublicationType publicationType;

    @Enumerated(EnumType.STRING)
    private PublicationStatus publicationStatus;

    @Enumerated(EnumType.STRING)
    private ProductCondition productCondition;

    private Long likes;

    private Long views;

    @Column()
    private Double aspectRation = 1.0;

    @Column(nullable = true)
    private String videoPreview;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime datePosted;

    @LastModifiedDate
    private LocalDateTime dateUpdated;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User seller;

    @Column(nullable = false)
    private Boolean isGrantedForPreciseLocation;

    @Column(nullable = false)
    private String locationName;

    @ManyToOne
    @JoinColumn(name = "country_id")
    private Country country;

    @ManyToOne
    @JoinColumn(name = "state_id")
    private State state;

    @ManyToOne
    @JoinColumn(name = "county_id")
    private County county;

    @JsonIgnore
    @ToString.Exclude
    @OneToMany(mappedBy = "publication", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Comment> comments;

    @Column()
    private Integer commentsCount;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private Double latitude;


    @PrePersist
    @PreUpdate
    public void init() {
        if (this.price == null) {
            this.price = 0.0F;
        }
    }
}
