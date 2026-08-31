package com.lucas.landmarketplace.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lucas.landmarketplace.dto.BoundaryCheckRequest;
import com.lucas.landmarketplace.dto.LandPlotCreateRequest;
import com.lucas.landmarketplace.dto.LandPlotResponse;
import com.lucas.landmarketplace.dto.OverlapCheckResponse;
import com.lucas.landmarketplace.exception.LandPlotNotFoundException;
import com.lucas.landmarketplace.mapper.GeoJsonMapper;
import com.lucas.landmarketplace.model.LandPlot;
import com.lucas.landmarketplace.repository.LandPlotRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LandPlotService {

    private final LandPlotRepository landPlotRepository;

    @Transactional
    public LandPlotResponse register(LandPlotCreateRequest request) {
        LandPlot landPlot = LandPlot.builder()
                .boundary(GeoJsonMapper.toPolygon(request.boundary()))
                .price(request.price())
                .description(request.description())
                .contact(request.contact())
                .build();

        return toResponse(landPlotRepository.save(landPlot));
    }

    @Transactional(readOnly = true)
    public List<LandPlotResponse> findAll() {
        return landPlotRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public LandPlotResponse findById(UUID id) {
        LandPlot landPlot = landPlotRepository.findById(id)
                .orElseThrow(() -> new LandPlotNotFoundException(id));
        return toResponse(landPlot);
    }

    @Transactional(readOnly = true)
    public List<LandPlotResponse> searchWithinCircle(double lat, double lng, double radiusMeters) {
        return landPlotRepository.findWithinCircle(lng, lat, radiusMeters).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OverlapCheckResponse checkOverlap(BoundaryCheckRequest request) {
        boolean overlaps = landPlotRepository.existsOverlapping(GeoJsonMapper.toPolygon(request.boundary()));
        return new OverlapCheckResponse(overlaps);
    }

    @Transactional
    public void delete(UUID id) {
        if (!landPlotRepository.existsById(id)) {
            throw new LandPlotNotFoundException(id);
        }
        landPlotRepository.deleteById(id);
    }

    private LandPlotResponse toResponse(LandPlot landPlot) {
        return new LandPlotResponse(
                landPlot.getId(),
                GeoJsonMapper.toCoordinates(landPlot.getBoundary()),
                landPlot.getPrice(),
                landPlot.getDescription(),
                landPlot.getContact(),
                landPlot.getCreatedAt());
    }
}
