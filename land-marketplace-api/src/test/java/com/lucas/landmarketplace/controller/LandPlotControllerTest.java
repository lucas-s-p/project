package com.lucas.landmarketplace.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;
import com.lucas.landmarketplace.config.JwtAuthenticationFilter;
import com.lucas.landmarketplace.config.JwtService;
import com.lucas.landmarketplace.config.SecurityConfig;
import com.lucas.landmarketplace.dto.BoundaryCheckRequest;
import com.lucas.landmarketplace.dto.CoordinateDTO;
import com.lucas.landmarketplace.dto.LandPlotCreateRequest;
import com.lucas.landmarketplace.dto.LandPlotResponse;
import com.lucas.landmarketplace.dto.OverlapCheckResponse;
import com.lucas.landmarketplace.exception.LandPlotNotFoundException;
import com.lucas.landmarketplace.repository.LandPlotRepository;
import com.lucas.landmarketplace.service.LandPlotService;

@WebMvcTest(LandPlotController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtService.class})
@WithMockUser
class LandPlotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LandPlotService landPlotService;

    // Needed because the NoOverlap class-level validator is a Spring-managed
    // ConstraintValidator that autowires the repository even in this web slice.
    @MockitoBean
    private LandPlotRepository landPlotRepository;

    @Test
    void register_returns201_withCreatedPlot() throws Exception {
        LandPlotCreateRequest request = validRequest();
        LandPlotResponse response = new LandPlotResponse(
                UUID.randomUUID(), request.boundary(), request.price(), request.description(),
                request.contact(), Instant.now());
        when(landPlotService.register(any())).thenReturn(response);

        mockMvc.perform(post("/api/land-plots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.price").value(1000));
    }

    @Test
    void register_returns409_whenBoundaryOverlapsExistingPlot() throws Exception {
        when(landPlotRepository.existsOverlapping(any())).thenReturn(true);

        mockMvc.perform(post("/api/land-plots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isConflict());

        verifyNoInteractions(landPlotService);
    }

    @Test
    void register_returns400_whenBoundaryHasFewerThanFourPoints() throws Exception {
        LandPlotCreateRequest invalid = new LandPlotCreateRequest(
                List.of(new CoordinateDTO(0.0, 0.0)), BigDecimal.TEN, "d", "c");

        mockMvc.perform(post("/api/land-plots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findById_returns404_whenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(landPlotService.findById(id)).thenThrow(new LandPlotNotFoundException(id));

        mockMvc.perform(get("/api/land-plots/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void search_returnsMatchingPlots() throws Exception {
        when(landPlotService.searchWithinCircle(anyDouble(), anyDouble(), anyDouble())).thenReturn(List.of());

        mockMvc.perform(get("/api/land-plots/search")
                        .param("lat", "1")
                        .param("lng", "2")
                        .param("radiusMeters", "500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void checkOverlap_returnsTrue_whenServiceReportsOverlap() throws Exception {
        when(landPlotService.checkOverlap(any())).thenReturn(new OverlapCheckResponse(true));

        mockMvc.perform(post("/api/land-plots/check-overlap")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new BoundaryCheckRequest(validRequest().boundary()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overlaps").value(true));
    }

    @Test
    void delete_returns204_whenPlotExists() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(landPlotService).delete(id);

        mockMvc.perform(delete("/api/land-plots/{id}", id))
                .andExpect(status().isNoContent());

        verify(landPlotService).delete(id);
    }

    @Test
    void delete_returns404_whenPlotDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new LandPlotNotFoundException(id)).when(landPlotService).delete(id);

        mockMvc.perform(delete("/api/land-plots/{id}", id))
                .andExpect(status().isNotFound());
    }

    private LandPlotCreateRequest validRequest() {
        return new LandPlotCreateRequest(
                List.of(
                        new CoordinateDTO(0.0, 0.0),
                        new CoordinateDTO(1.0, 0.0),
                        new CoordinateDTO(1.0, 1.0),
                        new CoordinateDTO(0.0, 1.0)),
                BigDecimal.valueOf(1000),
                "A nice flat plot",
                "owner@example.com");
    }
}
