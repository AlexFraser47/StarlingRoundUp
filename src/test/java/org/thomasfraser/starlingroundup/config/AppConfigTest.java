package org.thomasfraser.starlingroundup.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppConfigTest {

    @InjectMocks
    private AppConfig appConfig;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(appConfig, "apiToken", "<api token>");
    }

    @Test
    void shouldReturnCorrectHeadersTest() {
        HttpHeaders expectedHeaders = new HttpHeaders();
        expectedHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        expectedHeaders.set("Authorization", "Bearer <api token>");

        HttpHeaders actualHeaders = appConfig.httpHeaders();

        assertEquals(expectedHeaders, actualHeaders);
    }
}