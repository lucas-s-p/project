package com.lucas.landmarketplace.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lucas.landmarketplace.dto.BoundaryCheckRequest;
import com.lucas.landmarketplace.dto.CoordinateDTO;
import com.lucas.landmarketplace.dto.LandPlotCreateRequest;
import com.lucas.landmarketplace.dto.LandPlotResponse;
import com.lucas.landmarketplace.dto.OverlapCheckResponse;
import com.lucas.landmarketplace.exception.LandPlotNotFoundException;
import com.lucas.landmarketplace.mapper.GeoJsonMapper;
import com.lucas.landmarketplace.model.LandPlot;
import com.lucas.landmarketplace.repository.LandPlotRepository;

@ExtendWith(MockitoExtension.class)
class LandPlotServiceTest {

    @Mock
    private LandPlotRepository landPlotRepository;

    @InjectMocks
    private LandPlotService landPlotService;

    @Test
    void register_savesEntity_andReturnsMappedResponse() {
        LandPlotCreateRequest request = validRequest();
        when(landPlotRepository.save(any())).thenAnswer(invocation -> {
            LandPlot plot = invocation.getArgument(0);
            plot.setId(UUID.randomUUID());
            plot.setCreatedAt(Instant.now());
            return plot;
        });

        LandPlotResponse response = landPlotService.register(request);

        assertThat(response.id()).isNotNull();
        assertThat(response.price()).isEqualTo(request.price());
        assertThat(response.description()).isEqualTo(request.description());
        assertThat(response.boundary()).hasSize(5);
    }

    @Test
    void findById_throwsNotFound_whenMissing() {
        UUID id = UUID.randomUUID();
        when(landPlotRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> landPlotService.findById(id))
                .isInstanceOf(LandPlotNotFoundException.class);
    }

    @Test
    void findById_returnsMappedResponse_whenFound() {
        LandPlot plot = existingPlot();
        when(landPlotRepository.findById(plot.getId())).thenReturn(Optional.of(plot));

        LandPlotResponse response = landPlotService.findById(plot.getId());

        assertThat(response.id()).isEqualTo(plot.getId());
    }

    @Test
    void findAll_mapsEveryPlot() {
        when(landPlotRepository.findAll()).thenReturn(List.of(existingPlot()));

        List<LandPlotResponse> results = landPlotService.findAll();

        assertThat(results).hasSize(1);
    }

    @Test
    void searchWithinCircle_delegatesToRepository_withLngLatOrderSwapped() {
        when(landPlotRepository.findWithinCircle(anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(List.of(existingPlot()));

        List<LandPlotResponse> results = landPlotService.searchWithinCircle(1.0, 2.0, 500.0);

        assertThat(results).hasSize(1);
        verify(landPlotRepository).findWithinCircle(2.0, 1.0, 500.0);
    }

    @Test
    void checkOverlap_returnsTrue_whenRepositoryReportsOverlap() {
        when(landPlotRepository.existsOverlapping(any())).thenReturn(true);

        OverlapCheckResponse response = landPlotService.checkOverlap(new BoundaryCheckRequest(validRequest().boundary()));

        assertThat(response.overlaps()).isTrue();
    }

    @Test
    void checkOverlap_returnsFalse_whenRepositoryReportsNoOverlap() {
        when(landPlotRepository.existsOverlapping(any())).thenReturn(false);

        OverlapCheckResponse response = landPlotService.checkOverlap(new BoundaryCheckRequest(validRequest().boundary()));

        assertThat(response.overlaps()).isFalse();
    }

    @Test
    void delete_removesPlot_whenItExists() {
        UUID id = UUID.randomUUID();
        when(landPlotRepository.existsById(id)).thenReturn(true);

        landPlotService.delete(id);

        verify(landPlotRepository).deleteById(id);
    }

    @Test
    void delete_throwsNotFound_whenPlotDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(landPlotRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> landPlotService.delete(id))
                .isInstanceOf(LandPlotNotFoundException.class);
    }

    private LandPlot existingPlot() {
        return LandPlot.builder()
                .id(UUID.randomUUID())
                .boundary(GeoJsonMapper.toPolygon(validRequest().boundary()))
                .price(BigDecimal.valueOf(500))
                .description("desc")
                .contact("contact")
                .createdAt(Instant.now())
                .build();
    }

    private LandPlotCreateRequest validRequest() {
        return new LandPlotCreateRequest(
                List.of(
                        new CoordinateDTO(0.0, 0.0),
                        new CoordinateDTO(1.0, 0.0),
                        new CoordinateDTO(1.0, 1.0),
                        new CoordinateDTO(0.0, 1.0)),
                BigDecimal.valueOf(1000),
                "desc",
                "contact");
    }
}
