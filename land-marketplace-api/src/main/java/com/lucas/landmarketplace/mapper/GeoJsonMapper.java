package com.lucas.landmarketplace.mapper;

import java.util.Arrays;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;

import com.lucas.landmarketplace.dto.CoordinateDTO;

/**
 * Converts between the plain lng/lat coordinate lists used at the API boundary and the JTS
 * geometries used internally for spatial persistence and PostGIS queries. All geometries are
 * created using SRID 4326 (WGS 84), matching the coordinates produced by the frontend map.
 */
public final class GeoJsonMapper {

    private static final int SRID = 4326;
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), SRID);

    private GeoJsonMapper() {
    }

    public static Polygon toPolygon(List<CoordinateDTO> points) {
        Coordinate[] coordinates = points.stream()
                .map(point -> new Coordinate(point.lng(), point.lat()))
                .toArray(Coordinate[]::new);

        LinearRing ring = GEOMETRY_FACTORY.createLinearRing(ensureClosedRing(coordinates));
        return GEOMETRY_FACTORY.createPolygon(ring);
    }

    public static List<CoordinateDTO> toCoordinates(Polygon polygon) {
        return Arrays.stream(polygon.getExteriorRing().getCoordinates())
                .map(coordinate -> new CoordinateDTO(coordinate.x, coordinate.y))
                .toList();
    }

    public static Point toPoint(double lng, double lat) {
        return GEOMETRY_FACTORY.createPoint(new Coordinate(lng, lat));
    }

    private static Coordinate[] ensureClosedRing(Coordinate[] coordinates) {
        if (coordinates.length > 0 && !coordinates[0].equals2D(coordinates[coordinates.length - 1])) {
            Coordinate[] closed = Arrays.copyOf(coordinates, coordinates.length + 1);
            closed[closed.length - 1] = coordinates[0];
            return closed;
        }
        return coordinates;
    }
}
