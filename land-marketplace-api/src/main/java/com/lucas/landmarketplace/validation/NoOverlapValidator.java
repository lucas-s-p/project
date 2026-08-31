package com.lucas.landmarketplace.validation;

import org.locationtech.jts.geom.Polygon;

import com.lucas.landmarketplace.dto.LandPlotCreateRequest;
import com.lucas.landmarketplace.mapper.GeoJsonMapper;
import com.lucas.landmarketplace.repository.LandPlotRepository;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NoOverlapValidator implements ConstraintValidator<NoOverlap, LandPlotCreateRequest> {

    private final LandPlotRepository landPlotRepository;

    @Override
    public boolean isValid(LandPlotCreateRequest request, ConstraintValidatorContext context) {
        if (request == null || request.boundary() == null || request.boundary().size() < 4) {
            return true;
        }
        Polygon polygon = GeoJsonMapper.toPolygon(request.boundary());
        return !landPlotRepository.existsOverlapping(polygon);
    }
}
