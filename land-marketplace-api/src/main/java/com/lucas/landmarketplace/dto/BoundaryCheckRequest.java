package com.lucas.landmarketplace.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BoundaryCheckRequest(

        @NotNull
        @Size(min = 4, message = "Boundary must contain at least 4 points forming a closed polygon")
        List<@Valid CoordinateDTO> boundary) {
}
