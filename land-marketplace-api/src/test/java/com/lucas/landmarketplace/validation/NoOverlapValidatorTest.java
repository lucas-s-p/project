package com.lucas.landmarketplace.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lucas.landmarketplace.dto.CoordinateDTO;
import com.lucas.landmarketplace.dto.LandPlotCreateRequest;
import com.lucas.landmarketplace.repository.LandPlotRepository;

import jakarta.validation.ConstraintValidatorContext;

@ExtendWith(MockitoExtension.class)
class NoOverlapValidatorTest {

    @Mock
    private LandPlotRepository landPlotRepository;

    @InjectMocks
    private NoOverlapValidator validator;

    @Test
    void isValid_returnsFalse_whenRepositoryReportsOverlap() {
        when(landPlotRepository.existsOverlapping(any())).thenReturn(true);

        boolean result = validator.isValid(validRequest(), mock(ConstraintValidatorContext.class));

        assertThat(result).isFalse();
    }

    @Test
    void isValid_returnsTrue_whenNoOverlap() {
        when(landPlotRepository.existsOverlapping(any())).thenReturn(false);

        boolean result = validator.isValid(validRequest(), mock(ConstraintValidatorContext.class));

        assertThat(result).isTrue();
    }

    @Test
    void isValid_returnsTrue_andSkipsQuery_whenBoundaryIncomplete() {
        LandPlotCreateRequest request = new LandPlotCreateRequest(
                List.of(new CoordinateDTO(0.0, 0.0)), BigDecimal.TEN, "desc", "contact");

        boolean result = validator.isValid(request, mock(ConstraintValidatorContext.class));

        assertThat(result).isTrue();
        verifyNoInteractions(landPlotRepository);
    }

    private LandPlotCreateRequest validRequest() {
        return new LandPlotCreateRequest(
                List.of(
                        new CoordinateDTO(0.0, 0.0),
                        new CoordinateDTO(1.0, 0.0),
                        new CoordinateDTO(1.0, 1.0),
                        new CoordinateDTO(0.0, 1.0)),
                BigDecimal.valueOf(1000),
                "desc",
                "contact");
    }
}
