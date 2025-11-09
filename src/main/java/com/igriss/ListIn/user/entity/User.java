package com.igriss.ListIn.user.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.igriss.ListIn.location.entity.Country;
import com.igriss.ListIn.location.entity.County;
import com.igriss.ListIn.location.entity.State;
import com.igriss.ListIn.security.roles.Role;
import com.igriss.ListIn.user.enums.Status;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "users")
@ToString
@EntityListeners(AuditingEntityListener.class)
public class User implements UserDetails { // Agar UserDetails dan implement qilsak Spring shu classni taniydi

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID userId;

    @Column(nullable = false)
    private String nickName;

    private Boolean enableCalling;

    @Column(nullable = false)
    private String phoneNumber;

    @JsonFormat(pattern = "HH:mm")
    @Builder.Default
    @Column(columnDefinition = "TIME")
    private LocalTime fromTime = LocalTime.of(0, 0);

    @JsonFormat(pattern = "HH:mm")
    @Builder.Default
    @Column(columnDefinition = "TIME")
    private LocalTime toTime = LocalTime.of(23, 59);

    @Column(unique = true, nullable = false)
    private String email;

    private String biography;

    @Column(nullable = false)
    private String password;

    private String profileImagePath;

    private String backgroundImagePath;

    @Builder.Default
    private Float rating = 5.0F;

    @Builder.Default
    private Long followers = 0L;

    @Builder.Default
    private Long following = 0L;

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

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private Double latitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    private Status status;

    @CreatedDate
    private LocalDateTime dateCreated;
    @LastModifiedDate
    private LocalDateTime dateUpdated;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.role.getAuthorities();
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email; // Har bir Userda unikal field ishlatilishi kerak
    }
}
