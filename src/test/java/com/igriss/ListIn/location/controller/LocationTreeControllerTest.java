package com.igriss.ListIn.location.controller;

import com.igriss.ListIn.location.dto.node.CountryNode;
import com.igriss.ListIn.location.dto.node.CountyNode;
import com.igriss.ListIn.location.dto.node.LocationTreeNode;
import com.igriss.ListIn.location.dto.node.StateNode;
import com.igriss.ListIn.location.service.LocationService;
import org.instancio.Instancio;
import org.instancio.junit.InstancioExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(InstancioExtension.class)
class LocationTreeControllerTest {

    private LocationService locationService;
    private LocationTreeController locationTreeController;

    @BeforeEach
    void setUp() {
        locationService = mock(LocationService.class);
        locationTreeController = new LocationTreeController(locationService);
    }

    @Test
    void getCategoryTree_returnsLocationTreeNode() {
        CountyNode county = Instancio.create(CountyNode.class);

        StateNode state = Instancio.of(StateNode.class)
                .set(field(StateNode::getCounties), List.of(county))
                .create();

        CountryNode country = Instancio.of(CountryNode.class)
                .set(field(CountryNode::getStates), List.of(state))
                .create();

        LocationTreeNode treeNode = new LocationTreeNode();
        treeNode.setCountries(List.of(country));

        when(locationService.getLocationTree()).thenReturn(treeNode);

        ResponseEntity<LocationTreeNode> response = locationTreeController.getCategoryTree();

        assertThat(response).isNotNull();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCountries()).hasSize(1);
        assertThat(response.getBody().getCountries().get(0).getStates()).hasSize(1);
        assertThat(response.getBody().getCountries().get(0).getStates().get(0).getCounties()).hasSize(1);

        verify(locationService, times(1)).getLocationTree();
    }
}
