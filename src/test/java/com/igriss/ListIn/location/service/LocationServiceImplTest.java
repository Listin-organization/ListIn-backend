package com.igriss.ListIn.location.service;

import com.igriss.ListIn.exceptions.ResourceNotFoundException;
import com.igriss.ListIn.location.dto.LocationDTO;
import com.igriss.ListIn.location.dto.node.CountryNode;
import com.igriss.ListIn.location.dto.node.CountyNode;
import com.igriss.ListIn.location.dto.node.LocationTreeNode;
import com.igriss.ListIn.location.dto.node.StateNode;
import com.igriss.ListIn.location.entity.Country;
import com.igriss.ListIn.location.entity.County;
import com.igriss.ListIn.location.entity.State;
import com.igriss.ListIn.location.repository.CountryRepository;
import com.igriss.ListIn.location.repository.CountyRepository;
import com.igriss.ListIn.location.repository.StateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LocationServiceImplTest {

    @Mock
    private CountryRepository countryRepository;

    @Mock
    private StateRepository stateRepository;

    @Mock
    private CountyRepository countyRepository;

    @InjectMocks
    private LocationServiceImpl locationService;

    private Country country;
    private State state;
    private County county;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        country = Country.builder().id(UUID.randomUUID()).value("Uzbekistan").valueUz("Oʻzbekiston").valueRu("Узбекистан").build();
        state = State.builder().id(UUID.randomUUID()).value("Tashkent").valueUz("Toshkent").valueRu("Ташкент").country(country).build();
        county = County.builder().id(UUID.randomUUID()).value("Yunusabad").valueUz("Yunusobod").valueRu("Юнусабад").state(state).build();
    }

    @Test
    void getLocationTree_ShouldReturnTree() {
        when(countryRepository.findAll()).thenReturn(List.of(country));
        when(stateRepository.findAllByCountry_Id(country.getId())).thenReturn(List.of(state));
        when(countyRepository.findAllByState_Id(state.getId())).thenReturn(List.of(county));

        LocationTreeNode tree = locationService.getLocationTree();

        assertNotNull(tree);
        assertEquals(1, tree.getCountries().size());
        CountryNode countryNode = tree.getCountries().get(0);
        assertEquals(country.getId(), countryNode.getCountryId());
        assertEquals(1, countryNode.getStates().size());
        StateNode stateNode = countryNode.getStates().get(0);
        assertEquals(state.getId(), stateNode.getStateId());
        assertEquals(1, stateNode.getCounties().size());
        CountyNode countyNode = stateNode.getCounties().get(0);
        assertEquals(county.getId(), countyNode.getCountyId());
    }

    @Test
    void getLocation_ShouldReturnLocationDTO_ForDefaultLanguage() {
        when(countryRepository.findByValueIgnoreCase("Uzbekistan")).thenReturn(Optional.of(country));
        when(stateRepository.findByValueIgnoreCase("Tashkent")).thenReturn(Optional.of(state));
        when(countyRepository.findByValueIgnoreCase("Yunusabad")).thenReturn(Optional.of(county));

        LocationDTO location = locationService.getLocation("Uzbekistan", "Tashkent", "Yunusabad", "en");

        assertNotNull(location);
        assertEquals(country, location.getCountry());
        assertEquals(state, location.getState());
        assertEquals(county, location.getCounty());
    }

    @Test
    void getLocation_ShouldReturnLocationDTO_ForUzLanguage() {
        when(countryRepository.findByValueUzIgnoreCase("Oʻzbekiston")).thenReturn(Optional.of(country));
        when(stateRepository.findByValueUzIgnoreCase("Toshkent")).thenReturn(Optional.of(state));
        when(countyRepository.findByValueUzIgnoreCase("Yunusobod")).thenReturn(Optional.of(county));

        LocationDTO location = locationService.getLocation("Oʻzbekiston", "Toshkent", "Yunusobod", "uz");

        assertNotNull(location);
        assertEquals(country, location.getCountry());
        assertEquals(state, location.getState());
        assertEquals(county, location.getCounty());
    }

    @Test
    void getLocation_ShouldThrowResourceNotFoundException_WhenCountryNotFound() {
        when(countryRepository.findByValueIgnoreCase("UnknownCountry")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                locationService.getLocation("UnknownCountry", "Tashkent", "Yunusabad", "en")
        );
    }

    @Test
    void getLocation_ShouldReturnNull_WhenNameIsNull() {
        LocationDTO location = locationService.getLocation(null, null, null, "en");
        assertNotNull(location);
        assertNull(location.getCountry());
        assertNull(location.getState());
        assertNull(location.getCounty());
    }

    @Test
    void getLocation_ruLanguage_returnsEntities() {
        Country country = Country.builder().id(UUID.randomUUID()).valueRu("Россия").build();
        State state = State.builder().id(UUID.randomUUID()).valueRu("Москва").build();
        County county = County.builder().id(UUID.randomUUID()).valueRu("ЦАО").build();

        when(countryRepository.findByValueRuIgnoreCase("Россия")).thenReturn(Optional.of(country));
        when(stateRepository.findByValueRuIgnoreCase("Москва")).thenReturn(Optional.of(state));
        when(countyRepository.findByValueRuIgnoreCase("ЦАО")).thenReturn(Optional.of(county));

        LocationDTO dto = locationService.getLocation("Россия", "Москва", "ЦАО", "ru");

        assertThat(dto).isNotNull();
        assertThat(dto.getCountry()).isEqualTo(country);
        assertThat(dto.getState()).isEqualTo(state);
        assertThat(dto.getCounty()).isEqualTo(county);

        verify(countryRepository, times(1)).findByValueRuIgnoreCase("Россия");
        verify(stateRepository, times(1)).findByValueRuIgnoreCase("Москва");
        verify(countyRepository, times(1)).findByValueRuIgnoreCase("ЦАО");
    }

    @Test
    void getLocation_ruLanguage_entityNotFound_throwsResourceNotFoundException() {
        when(countryRepository.findByValueRuIgnoreCase("Неизвестно")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> locationService.getLocation("Неизвестно", null, null, "ru"));

        verify(countryRepository, times(1)).findByValueRuIgnoreCase("Неизвестно");
    }

    @Test
    void getLocation_languageOtherThanUzOrRu_usesDefaultFinder() {
        Country country = Country.builder().id(UUID.randomUUID()).value("USA").build();
        when(countryRepository.findByValueIgnoreCase("USA")).thenReturn(Optional.of(country));

        LocationDTO dto = locationService.getLocation("USA", null, null, "en");

        assertThat(dto.getCountry()).isEqualTo(country);
        verify(countryRepository, times(1)).findByValueIgnoreCase("USA");
    }

    @Test
    void getLocation_nullOrBlankName_returnsNull() {
        LocationDTO dto = locationService.getLocation(null, "", "  ", "ru");
        assertThat(dto.getCountry()).isNull();
        assertThat(dto.getState()).isNull();
        assertThat(dto.getCounty()).isNull();
    }

    @Test
    void getLocationTree_returnsLocationTreeNode() {
        when(countryRepository.findAll()).thenReturn(java.util.List.of());
        when(stateRepository.findAllByCountry_Id(any())).thenReturn(java.util.List.of());
        when(countyRepository.findAllByState_Id(any())).thenReturn(java.util.List.of());

        LocationTreeNode treeNode = locationService.getLocationTree();
        assertThat(treeNode).isNotNull();
        assertThat(treeNode.getCountries()).isEmpty();
    }
}
