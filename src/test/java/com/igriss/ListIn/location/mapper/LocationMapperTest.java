package com.igriss.ListIn.location.mapper;

import com.igriss.ListIn.location.dto.CountryDTO;
import com.igriss.ListIn.location.dto.CountyDTO;
import com.igriss.ListIn.location.dto.StateDTO;
import com.igriss.ListIn.location.entity.Country;
import com.igriss.ListIn.location.entity.County;
import com.igriss.ListIn.location.entity.State;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LocationMapperTest {

    private LocationMapper locationMapper;

    @BeforeEach
    void setUp() {
        locationMapper = new LocationMapper();
    }

    @Test
    void toCountryDTO_mapsCorrectly() {
        UUID id = UUID.randomUUID();
        Country country = Country.builder()
                .id(id)
                .value("Country Name")
                .valueUz("Davlat Nomi")
                .valueRu("Название страны")
                .build();

        CountryDTO dto = locationMapper.toCountryDTO(country);

        assertThat(dto).isNotNull();
        assertThat(dto.getCountryId()).isEqualTo(id);
        assertThat(dto.getValue()).isEqualTo("Country Name");
        assertThat(dto.getValueUz()).isEqualTo("Davlat Nomi");
        assertThat(dto.getValueRu()).isEqualTo("Название страны");
    }

    @Test
    void toStateDTO_mapsCorrectly() {
        UUID id = UUID.randomUUID();
        State state = State.builder()
                .id(id)
                .value("State Name")
                .valueUz("Viloyat Nomi")
                .valueRu("Название штата")
                .build();

        StateDTO dto = locationMapper.toStateDTO(state);

        assertThat(dto).isNotNull();
        assertThat(dto.getStateId()).isEqualTo(id);
        assertThat(dto.getValue()).isEqualTo("State Name");
        assertThat(dto.getValueUz()).isEqualTo("Viloyat Nomi");
        assertThat(dto.getValueRu()).isEqualTo("Название штата");
    }

    @Test
    void toCountyDTO_mapsCorrectly() {
        UUID id = UUID.randomUUID();
        County county = County.builder()
                .id(id)
                .value("County Name")
                .valueUz("Tuman Nomi")
                .valueRu("Название района")
                .build();

        CountyDTO dto = locationMapper.toCountyDTO(county);

        assertThat(dto).isNotNull();
        assertThat(dto.getCountyId()).isEqualTo(id);
        assertThat(dto.getValue()).isEqualTo("County Name");
        assertThat(dto.getValueUz()).isEqualTo("Tuman Nomi");
        assertThat(dto.getValueRu()).isEqualTo("Название района");
    }
}
