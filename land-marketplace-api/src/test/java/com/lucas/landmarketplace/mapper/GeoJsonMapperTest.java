package com.lucas.landmarketplace.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Polygon;

import com.lucas.landmarketplace.dto.CoordinateDTO;

class GeoJsonMapperTest {

    @Test
    void toPolygon_closesOpenRing() {
        List<CoordinateDTO> openRing = List.of(
                new CoordinateDTO(0.0, 0.0),
                new CoordinateDTO(1.0, 0.0),
                new CoordinateDTO(1.0, 1.0),
                new CoordinateDTO(0.0, 1.0));

        Polygon polygon = GeoJsonMapper.toPolygon(openRing);

        assertThat(polygon.getExteriorRing().getCoordinates()).hasSize(5);
        assertThat(polygon.isValid()).isTrue();
    }

    @Test
    void toPolygon_keepsAlreadyClosedRing() {
        List<CoordinateDTO> closedRing = List.of(
                new CoordinateDTO(0.0, 0.0),
                new CoordinateDTO(1.0, 0.0),
                new CoordinateDTO(1.0, 1.0),
                new CoordinateDTO(0.0, 1.0),
                new CoordinateDTO(0.0, 0.0));

        Polygon polygon = GeoJsonMapper.toPolygon(closedRing);

        assertThat(polygon.getExteriorRing().getCoordinates()).hasSize(5);
    }

    @Test
    void toCoordinates_roundTripsThroughPolygon() {
        List<CoordinateDTO> closedRing = List.of(
                new CoordinateDTO(0.0, 0.0),
                new CoordinateDTO(1.0, 0.0),
                new CoordinateDTO(1.0, 1.0),
                new CoordinateDTO(0.0, 1.0),
                new CoordinateDTO(0.0, 0.0));

        List<CoordinateDTO> result = GeoJsonMapper.toCoordinates(GeoJsonMapper.toPolygon(closedRing));

        assertThat(result).hasSize(5);
        assertThat(result.get(0)).isEqualTo(new CoordinateDTO(0.0, 0.0));
        assertThat(result.get(2)).isEqualTo(new CoordinateDTO(1.0, 1.0));
    }
}
