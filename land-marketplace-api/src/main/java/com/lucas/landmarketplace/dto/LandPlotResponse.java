package com.lucas.landmarketplace.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record LandPlotResponse(
        UUID id,
        List<CoordinateDTO> boundary,
        BigDecimal price,
        String description,
        String contact,
        Instant createdAt) {
}
