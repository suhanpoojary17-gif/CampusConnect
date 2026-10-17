package com.campusconnect;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void unauthenticatedUser_IsBlockedFromAdminRoute() throws Exception {
        // Hitting an admin controller endpoint without authentication should be rejected
        mockMvc.perform(get("/api/admin/dashboard") // Maps to AdminDashboardController
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(org.hamcrest.CoreMatchers.anyOf(
                        org.hamcrest.CoreMatchers.is(401),
                        org.hamcrest.CoreMatchers.is(403)
                )));
    }

    @Test
    @WithMockUser(username = "student_user", roles = {"STUDENT"})
    void studentUser_ForbiddenFromAdminBusPass() throws Exception {
        // Verifies a student cannot access admin-level bus pass management
        mockMvc.perform(get("/api/admin/bus-passes") // Maps to AdminBusPassController
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden()); // Expects 403 Forbidden
    }

    @Test
    @WithMockUser(username = "admin_user", roles = {"ADMIN"})
    void adminUser_CanAccessAdminRoutes() throws Exception {
        // Verifies that an admin user can successfully reach the admin route layer 
        // (Expects 200 OK or 404/400 if specific request parameters/path variables are missing, meaning security allowed it through)
        mockMvc.perform(get("/api/admin/dashboard")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(org.hamcrest.CoreMatchers.anyOf(
                        org.hamcrest.CoreMatchers.is(200),
                        org.hamcrest.CoreMatchers.is(404),
                        org.hamcrest.CoreMatchers.is(400)
                )));
    }
}