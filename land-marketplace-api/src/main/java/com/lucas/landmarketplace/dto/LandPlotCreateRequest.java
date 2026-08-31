package com.lucas.landmarketplace.dto;

import java.math.BigDecimal;
import java.util.List;

import com.lucas.landmarketplace.validation.NoOverlap;
import com.lucas.landmarketplace.validation.ValidContact;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@NoOverlap
public record LandPlotCreateRequest(

        @NotNull
        @Size(min = 4, message = "Boundary must contain at least 4 points forming a closed polygon")
        List<@Valid CoordinateDTO> boundary,

        @NotNull
        @Positive(message = "Price must be greater than zero")
        BigDecimal price,

        @NotBlank(message = "Description is required")
        @Size(min = 5, max = 2000, message = "Description must be at least 5 characters long")
        String description,

        @NotBlank(message = "Contact is required")
        @Size(max = 200)
        @ValidContact
        String contact) {
}
