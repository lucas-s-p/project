package com.lucas.landmarketplace.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.lucas.landmarketplace.model.LandPlot;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class LandPlotRepositoryTest {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgis/postgis:16-3.4").asCompatibleSubstituteFor("postgres"));

    @Autowired
    private LandPlotRepository landPlotRepository;

    @Test
    void existsOverlapping_returnsTrue_whenPolygonsIntersect() {
        landPlotRepository.save(plotAt(square(0, 0, 1)));

        boolean overlaps = landPlotRepository.existsOverlapping(square(0.5, 0.5, 1));

        assertThat(overlaps).isTrue();
    }

    @Test
    void existsOverlapping_returnsFalse_whenPolygonsDoNotIntersect() {
        landPlotRepository.save(plotAt(square(0, 0, 1)));

        boolean overlaps = landPlotRepository.existsOverlapping(square(10, 10, 1));

        assertThat(overlaps).isFalse();
    }

    @Test
    void findWithinCircle_returnsOnlyPlotsIntersectingTheRadius() {
        LandPlot near = landPlotRepository.save(plotAt(square(0, 0, 0.001)));
        landPlotRepository.save(plotAt(square(10, 10, 0.001)));

        List<LandPlot> results = landPlotRepository.findWithinCircle(0.0005, 0.0005, 5000);

        assertThat(results).extracting(LandPlot::getId).containsExactly(near.getId());
    }

    private LandPlot plotAt(Polygon polygon) {
        return LandPlot.builder()
                .boundary(polygon)
                .price(BigDecimal.valueOf(1000))
                .description("Test plot")
                .contact("test@example.com")
                .build();
    }

    private Polygon square(double originX, double originY, double size) {
        Coordinate[] coordinates = new Coordinate[] {
                new Coordinate(originX, originY),
                new Coordinate(originX + size, originY),
                new Coordinate(originX + size, originY + size),
                new Coordinate(originX, originY + size),
                new Coordinate(originX, originY)
        };
        return GEOMETRY_FACTORY.createPolygon(coordinates);
    }
}
