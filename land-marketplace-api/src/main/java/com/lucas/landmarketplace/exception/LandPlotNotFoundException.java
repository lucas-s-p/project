package com.lucas.landmarketplace.exception;

import java.util.UUID;

public class LandPlotNotFoundException extends RuntimeException {

    public LandPlotNotFoundException(UUID id) {
        super("Land plot not found: " + id);
    }
}
