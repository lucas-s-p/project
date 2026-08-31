package com.lucas.landmarketplace.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import tools.jackson.databind.ObjectMapper;

import com.lucas.landmarketplace.dto.BoundaryCheckRequest;
import com.lucas.landmarketplace.dto.CoordinateDTO;
import com.lucas.landmarketplace.dto.LandPlotCreateRequest;
import com.lucas.landmarketplace.repository.LandPlotRepository;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
@WithMockUser
@DisplayName("Land plot end-to-end tests")
class LandPlotIntegrationTest {

    private static final String URI_LAND_PLOTS = "/api/land-plots";

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgis/postgis:16-3.4").asCompatibleSubstituteFor("postgres"));

    @Autowired
    MockMvc mockMvc;

    @Autowired
    LandPlotRepository landPlotRepository;

    ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        landPlotRepository.deleteAll();
    }

    @Test
    @DisplayName("Registers a land plot and persists it")
    void testWhenWeRegisterALandPlot() throws Exception {
        LandPlotCreateRequest request = squareRequest(0, 0, 1, "Nice flat plot", "owner@example.com");

        mockMvc.perform(post(URI_LAND_PLOTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(print())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.price").value(1000));

        assertThat(landPlotRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("Rejects a land plot whose boundary overlaps an existing one")
    void testWhenBoundaryOverlapsAnExistingPlot() throws Exception {
        mockMvc.perform(post(URI_LAND_PLOTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                squareRequest(0, 0, 1, "First plot", "first@example.com"))))
                .andExpect(status().isCreated());

        mockMvc.perform(post(URI_LAND_PLOTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                squareRequest(0.5, 0.5, 1, "Overlapping plot", "second@example.com"))))
                .andExpect(status().isConflict());

        assertThat(landPlotRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("Rejects a land plot with an invalid price, description, or contact")
    void testWhenPayloadIsInvalid() throws Exception {
        LandPlotCreateRequest invalid = squareRequest(0, 0, 1, "abc", "not-a-contact");

        mockMvc.perform(post(URI_LAND_PLOTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        assertThat(landPlotRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("Fetches every registered land plot")
    void testWhenWeFetchAllLandPlots() throws Exception {
        mockMvc.perform(post(URI_LAND_PLOTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                squareRequest(0, 0, 1, "Plot one", "one@example.com"))))
                .andExpect(status().isCreated());
        mockMvc.perform(post(URI_LAND_PLOTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                squareRequest(10, 10, 1, "Plot two", "two@example.com"))))
                .andExpect(status().isCreated());

        String responseJson = mockMvc.perform(get(URI_LAND_PLOTS))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        List<?> results = objectMapper.readValue(responseJson, List.class);
        assertThat(results).hasSize(2);
    }

    @Test
    @DisplayName("Searches only the land plots intersecting a circle")
    void testWhenWeSearchWithinACircle() throws Exception {
        mockMvc.perform(post(URI_LAND_PLOTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                squareRequest(0, 0, 0.001, "Near plot", "near@example.com"))))
                .andExpect(status().isCreated());
        mockMvc.perform(post(URI_LAND_PLOTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                squareRequest(10, 10, 0.001, "Far plot", "far@example.com"))))
                .andExpect(status().isCreated());

        mockMvc.perform(get(URI_LAND_PLOTS + "/search")
                        .param("lat", "0.0005")
                        .param("lng", "0.0005")
                        .param("radiusMeters", "5000"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].description").value("Near plot"));
    }

    @Test
    @DisplayName("Reports overlap for a boundary intersecting an existing plot, without persisting anything")
    void testWhenWeCheckOverlap() throws Exception {
        mockMvc.perform(post(URI_LAND_PLOTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                squareRequest(0, 0, 1, "Existing plot", "existing@example.com"))))
                .andExpect(status().isCreated());

        mockMvc.perform(post(URI_LAND_PLOTS + "/check-overlap")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new BoundaryCheckRequest(squareRequest(0.5, 0.5, 1, "x", "x@example.com").boundary()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overlaps").value(true));

        mockMvc.perform(post(URI_LAND_PLOTS + "/check-overlap")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new BoundaryCheckRequest(squareRequest(10, 10, 1, "x", "x@example.com").boundary()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overlaps").value(false));

        assertThat(landPlotRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("Deletes a registered land plot")
    void testWhenWeDeleteALandPlot() throws Exception {
        mockMvc.perform(post(URI_LAND_PLOTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                squareRequest(0, 0, 1, "To be deleted", "owner@example.com"))))
                .andExpect(status().isCreated());
        UUID id = landPlotRepository.findAll().get(0).getId();

        mockMvc.perform(delete(URI_LAND_PLOTS + "/{id}", id))
                .andExpect(status().isNoContent());

        assertThat(landPlotRepository.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("Returns 404 when deleting a land plot that does not exist")
    void testWhenWeDeleteANonExistentLandPlot() throws Exception {
        mockMvc.perform(delete(URI_LAND_PLOTS + "/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    private LandPlotCreateRequest squareRequest(double originX, double originY, double size,
            String description, String contact) {
        List<CoordinateDTO> boundary = List.of(
                new CoordinateDTO(originX, originY),
                new CoordinateDTO(originX + size, originY),
                new CoordinateDTO(originX + size, originY + size),
                new CoordinateDTO(originX, originY + size));
        return new LandPlotCreateRequest(boundary, java.math.BigDecimal.valueOf(1000), description, contact);
    }
}
