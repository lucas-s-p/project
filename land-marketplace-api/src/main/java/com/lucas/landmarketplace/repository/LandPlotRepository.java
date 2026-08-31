package com.lucas.landmarketplace.repository;

import java.util.List;
import java.util.UUID;

import org.locationtech.jts.geom.Polygon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lucas.landmarketplace.model.LandPlot;

public interface LandPlotRepository extends JpaRepository<LandPlot, UUID> {

    @Query(value = "SELECT EXISTS (SELECT 1 FROM land_plots WHERE ST_Intersects(boundary, :polygon))",
            nativeQuery = true)
    boolean existsOverlapping(@Param("polygon") Polygon polygon);

    @Query(value = """
            SELECT * FROM land_plots
            WHERE ST_DWithin(
                boundary::geography,
                ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography,
                :radiusMeters
            )
            """, nativeQuery = true)
    List<LandPlot> findWithinCircle(@Param("lng") double lng, @Param("lat") double lat,
            @Param("radiusMeters") double radiusMeters);
}
