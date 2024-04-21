package org.thomasfraser.starlingroundup.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thomasfraser.starlingroundup.service.RoundUpService;

import java.math.BigDecimal;

/**
 * Controller class for handling API requests related to round up operations.
 */
@RestController
@RequestMapping("/api")
public class RoundUpController {

    private static final Logger LOGGER = LogManager.getLogger(RoundUpController.class);
    private final RoundUpService roundUpService;

    @Autowired
    public RoundUpController(RoundUpService roundUpService) {
        this.roundUpService = roundUpService;
    }

    @RequestMapping("/roundup")
    public ResponseEntity<String> roundup() {
        LOGGER.info("Round up request received");

        try {
            BigDecimal roundUpAmount = roundUpService.calculateAndTransferRoundUp();
            return ResponseEntity.ok("Round up completed successfully. Total rounded up: £" + roundUpAmount);
        } catch (Exception e){
            return ResponseEntity.badRequest().body("Failed to complete round up: " + e.getMessage());
        }
    }
}

// TODO
// 1. Transfer savings to a savings account
// 2. Add tests
// 3. Add more logging?
// 4. Move URLS and tokens to properties file
// 5. Handle amount vs source amount
// 6. Handle dates?
// 7. Settled time, edge case?
// 8. Clean up http header code and use appconfig.java to set up beans and request headers.
// 9. Add enums from api docs
// 10. Handle currency
