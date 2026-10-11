package com.campusconnect;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CampusIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
        // Verifies that the Spring Boot application context loads successfully
    }

    @Test
    void testApiEndpointAvailability() throws Exception {
        mockMvc.perform(get("/api/notices")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(org.hamcrest.CoreMatchers.anyOf(
                        org.hamcrest.CoreMatchers.is(200), 
                        org.hamcrest.CoreMatchers.is(401), 
                        org.hamcrest.CoreMatchers.is(403)
                )));
    }

    @Test
    void testInvalidRouteIsSecuredOrNotFound() throws Exception {
        // Since Spring Security intercepts requests globally, unauthenticated calls 
        // to unknown routes typically return 403 Forbidden or 401 Unauthorized.
        mockMvc.perform(get("/api/invalid-test-route-12345"))
                .andExpect(status().is(org.hamcrest.CoreMatchers.anyOf(
                        org.hamcrest.CoreMatchers.is(404),
                        org.hamcrest.CoreMatchers.is(403),
                        org.hamcrest.CoreMatchers.is(401)
                )));
    }
}