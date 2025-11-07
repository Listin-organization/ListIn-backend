package com.igriss.ListIn.publication.service;

import com.igriss.ListIn.publication.dto.BrandModelDTO;
import com.igriss.ListIn.publication.entity.AttributeKey;
import com.igriss.ListIn.publication.entity.AttributeValue;
import com.igriss.ListIn.publication.entity.brand_models.ConsoleBrandModel;
import com.igriss.ListIn.publication.entity.brand_models.LaptopBrandModel;
import com.igriss.ListIn.publication.entity.brand_models.LaptopGPUModel;
import com.igriss.ListIn.publication.entity.brand_models.LaptopProcessorModel;
import com.igriss.ListIn.publication.entity.brand_models.PCBrandModel;
import com.igriss.ListIn.publication.entity.brand_models.PCGPUModel;
import com.igriss.ListIn.publication.entity.brand_models.PCProcessorModel;
import com.igriss.ListIn.publication.entity.brand_models.SmartWatchBrandModel;
import com.igriss.ListIn.publication.entity.brand_models.SmartphoneBrandModel;
import com.igriss.ListIn.publication.entity.brand_models.TabletBrandModel;
import com.igriss.ListIn.publication.repository.AttributeValueRepository;
import com.igriss.ListIn.publication.repository.brand_models_repository.ConsoleBrandModelRepository;
import com.igriss.ListIn.publication.repository.brand_models_repository.LaptopBrandModelRepository;
import com.igriss.ListIn.publication.repository.brand_models_repository.LaptopGPUModelRepository;
import com.igriss.ListIn.publication.repository.brand_models_repository.LaptopProcessorModelRepository;
import com.igriss.ListIn.publication.repository.brand_models_repository.PCBrandModelRepository;
import com.igriss.ListIn.publication.repository.brand_models_repository.PCGPUModelRepository;
import com.igriss.ListIn.publication.repository.brand_models_repository.PCProcessorModelRepository;
import com.igriss.ListIn.publication.repository.brand_models_repository.SmartWatchBrandModelRepository;
import com.igriss.ListIn.publication.repository.brand_models_repository.SmartphoneBrandModelRepository;
import com.igriss.ListIn.publication.repository.brand_models_repository.TabletBrandModelRepository;
import com.igriss.ListIn.publication.service_impl.BrandModelServiceImpl;
import org.instancio.Instancio;
import org.instancio.junit.InstancioExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(InstancioExtension.class)
class BrandModelServiceTest {

    @InjectMocks
    private BrandModelServiceImpl service;

    @Mock
    private ConsoleBrandModelRepository consoleRepo;
    @Mock
    private LaptopBrandModelRepository laptopRepo;
    @Mock
    private PCBrandModelRepository pcRepo;
    @Mock
    private SmartphoneBrandModelRepository smartphoneRepo;
    @Mock
    private SmartWatchBrandModelRepository watchRepo;
    @Mock
    private TabletBrandModelRepository tabletRepo;
    @Mock
    private LaptopProcessorModelRepository laptopProcRepo;
    @Mock
    private PCProcessorModelRepository pcProcRepo;
    @Mock
    private LaptopGPUModelRepository laptopGpuRepo;
    @Mock
    private PCGPUModelRepository pcGpuRepo;
    @Mock
    private AttributeValueRepository attrValueRepo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetModels_whenIdIsNull_returnsEmptyDTO() {
        AttributeValue val = new AttributeValue();
        val.setId(null);

        List<BrandModelDTO> result = service.getModels(val);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getModelId()).isNull();
        verifyNoInteractions(attrValueRepo);
    }

    @Test
    void testGetModels_whenHasChildren_returnsMappedDTOs() {
        AttributeValue parent = Instancio.create(AttributeValue.class);
        AttributeValue child = Instancio.create(AttributeValue.class);
        when(attrValueRepo.findByParentValue(parent)).thenReturn(List.of(child));

        List<BrandModelDTO> result = service.getModels(parent);

        assertThat(result).hasSize(1);
        BrandModelDTO dto = result.get(0);
        assertThat(dto.getModelId()).isEqualTo(child.getId());
        assertThat(dto.getName()).isEqualTo(child.getValue());
        assertThat(dto.getAttributeId()).isEqualTo(child.getAttributeKey().getId().toString());
        verify(attrValueRepo).findByParentValue(parent);
    }

    @Test
    void testSmartphoneBrandBranch() {
        AttributeKey key = new AttributeKey();
        key.setName("Smartphone Brand");
        AttributeValue val = Instancio.create(AttributeValue.class);

        SmartphoneBrandModel model = Instancio.create(SmartphoneBrandModel.class);
        when(smartphoneRepo.findByAttributeValue(val)).thenReturn(List.of(model));

        var result = service.getCorrespondingModels(key, val);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getModelId()).isEqualTo(model.getId());
        verify(smartphoneRepo).findByAttributeValue(val);
    }

    @Test
    void testLaptopBrandBranch() {
        AttributeKey key = new AttributeKey();
        key.setName("Laptop Brand");
        AttributeValue val = Instancio.create(AttributeValue.class);

        LaptopBrandModel model = Instancio.create(LaptopBrandModel.class);
        when(laptopRepo.findByAttributeValue(val)).thenReturn(List.of(model));

        var result = service.getCorrespondingModels(key, val);
        assertThat(result.get(0).getModelId()).isEqualTo(model.getId());
        verify(laptopRepo).findByAttributeValue(val);
    }

    @Test
    void testLaptopProcessorBrandBranch() {
        AttributeKey key = new AttributeKey();
        key.setName("Laptop Processor Brands");
        AttributeValue val = Instancio.create(AttributeValue.class);

        LaptopProcessorModel model = Instancio.create(LaptopProcessorModel.class);
        when(laptopProcRepo.findByAttributeValue(val)).thenReturn(List.of(model));

        var result = service.getCorrespondingModels(key, val);
        assertThat(result).hasSize(1);
        verify(laptopProcRepo).findByAttributeValue(val);
    }

    @Test
    void testLaptopGpuBrandBranch() {
        AttributeKey key = new AttributeKey();
        key.setName("Laptop GPU Brands");
        AttributeValue val = Instancio.create(AttributeValue.class);

        LaptopGPUModel model = Instancio.create(LaptopGPUModel.class);
        when(laptopGpuRepo.findByAttributeValue(val)).thenReturn(List.of(model));

        var result = service.getCorrespondingModels(key, val);
        assertThat(result).hasSize(1);
        verify(laptopGpuRepo).findByAttributeValue(val);
    }

    @Test
    void testSmartwatchBrandBranch() {
        AttributeKey key = new AttributeKey();
        key.setName("Smartwatch Brand");
        AttributeValue val = Instancio.create(AttributeValue.class);

        SmartWatchBrandModel model = Instancio.create(SmartWatchBrandModel.class);
        when(watchRepo.findByAttributeValue(val)).thenReturn(List.of(model));

        var result = service.getCorrespondingModels(key, val);
        assertThat(result).hasSize(1);
        verify(watchRepo).findByAttributeValue(val);
    }

    @Test
    void testTabletBrandBranch() {
        AttributeKey key = new AttributeKey();
        key.setName("Tablet Brand");
        AttributeValue val = Instancio.create(AttributeValue.class);

        TabletBrandModel model = Instancio.create(TabletBrandModel.class);
        when(tabletRepo.findByAttributeValue(val)).thenReturn(List.of(model));

        var result = service.getCorrespondingModels(key, val);
        assertThat(result).hasSize(1);
        verify(tabletRepo).findByAttributeValue(val);
    }

    @Test
    void testPcBrandBranch() {
        AttributeKey key = new AttributeKey();
        key.setName("PC Brand");
        AttributeValue val = Instancio.create(AttributeValue.class);

        PCBrandModel model = Instancio.create(PCBrandModel.class);
        when(pcRepo.findByAttributeValue(val)).thenReturn(List.of(model));

        var result = service.getCorrespondingModels(key, val);
        assertThat(result).hasSize(1);
        verify(pcRepo).findByAttributeValue(val);
    }

    @Test
    void testPcProcessorBrandBranch() {
        AttributeKey key = new AttributeKey();
        key.setName("PC Processor Brand");
        AttributeValue val = Instancio.create(AttributeValue.class);

        PCProcessorModel model = Instancio.create(PCProcessorModel.class);
        when(pcProcRepo.findByAttributeValue(val)).thenReturn(List.of(model));

        var result = service.getCorrespondingModels(key, val);
        assertThat(result).hasSize(1);
        verify(pcProcRepo).findByAttributeValue(val);
    }

    @Test
    void testPcGpuBrandBranch() {
        AttributeKey key = new AttributeKey();
        key.setName("PC GPU Brands");
        AttributeValue val = Instancio.create(AttributeValue.class);

        PCGPUModel model = Instancio.create(PCGPUModel.class);
        when(pcGpuRepo.findByAttributeValue(val)).thenReturn(List.of(model));

        var result = service.getCorrespondingModels(key, val);
        assertThat(result).hasSize(1);
        verify(pcGpuRepo).findByAttributeValue(val);
    }

    @Test
    void testConsoleBrandBranch() {
        AttributeKey key = new AttributeKey();
        key.setName("Console Brand");
        AttributeValue val = Instancio.create(AttributeValue.class);

        ConsoleBrandModel model = Instancio.create(ConsoleBrandModel.class);
        when(consoleRepo.findByAttributeValue(val)).thenReturn(List.of(model));

        var result = service.getCorrespondingModels(key, val);
        assertThat(result).hasSize(1);
        verify(consoleRepo).findByAttributeValue(val);
    }

    @Test
    void testDefaultBranch_returnsEmptyDTO() {
        AttributeKey key = new AttributeKey();
        key.setName("Unknown Type");
        AttributeValue val = Instancio.create(AttributeValue.class);

        var result = service.getCorrespondingModels(key, val);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getModelId()).isNull();
    }
}
