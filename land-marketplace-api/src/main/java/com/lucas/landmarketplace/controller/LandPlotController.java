package com.lucas.landmarketplace.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lucas.landmarketplace.dto.BoundaryCheckRequest;
import com.lucas.landmarketplace.dto.LandPlotCreateRequest;
import com.lucas.landmarketplace.dto.LandPlotResponse;
import com.lucas.landmarketplace.dto.OverlapCheckResponse;
import com.lucas.landmarketplace.service.LandPlotService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/land-plots")
@RequiredArgsConstructor
public class LandPlotController {

    private final LandPlotService landPlotService;

    @PostMapping
    public ResponseEntity<LandPlotResponse> register(@Valid @RequestBody LandPlotCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(landPlotService.register(request));
    }

    @GetMapping
    public List<LandPlotResponse> findAll() {
        return landPlotService.findAll();
    }

    @GetMapping("/{id}")
    public LandPlotResponse findById(@PathVariable UUID id) {
        return landPlotService.findById(id);
    }

    @GetMapping("/search")
    public List<LandPlotResponse> search(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam double radiusMeters) {
        return landPlotService.searchWithinCircle(lat, lng, radiusMeters);
    }

    @PostMapping("/check-overlap")
    public OverlapCheckResponse checkOverlap(@Valid @RequestBody BoundaryCheckRequest request) {
        return landPlotService.checkOverlap(request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        landPlotService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
