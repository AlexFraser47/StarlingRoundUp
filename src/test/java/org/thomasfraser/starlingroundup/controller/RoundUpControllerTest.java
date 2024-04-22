package org.thomasfraser.starlingroundup.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.thomasfraser.starlingroundup.service.RoundUpService;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class RoundUpControllerTest {

    @Mock
    private RoundUpService roundUpService;

    @InjectMocks
    private RoundUpController roundUpController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnSuccessMessageWhenRoundUpIsSuccessfulTest() throws Exception {
        BigDecimal roundUpAmount = new BigDecimal("10.00");
        when(roundUpService.calculateAndTransferRoundUp()).thenReturn(roundUpAmount);

        ResponseEntity<String> response = roundUpController.roundup();

        assertEquals(ResponseEntity.ok("Round up completed successfully. Total rounded up: " + roundUpAmount), response);
    }

    @Test
    void shouldReturnErrorMessageWhenRoundUpFailsTest() throws Exception {
        String errorMessage = "Failed to complete round up";
        when(roundUpService.calculateAndTransferRoundUp()).thenThrow(new Exception("Error message"));

        ResponseEntity<String> response = roundUpController.roundup();

        assertEquals(ResponseEntity.badRequest().body(errorMessage), response);
    }
}