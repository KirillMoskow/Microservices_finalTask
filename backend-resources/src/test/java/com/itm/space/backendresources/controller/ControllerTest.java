package com.itm.space.backendresources.controller;

import com.itm.space.backendresources.api.response.UserResponse;
import com.itm.space.backendresources.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class ControllerTest {

    @MockBean
    UserService userService;

    @Container
    private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("test")
            .withUsername("user")
            .withPassword("password");

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "MODERATOR")
    void testCreateUser() throws Exception {

        UUID userId = UUID.randomUUID();
        String userJson = """
        {   "id": "%s",
            "username": "tester1111",
            "email": "tester1111@example.com",
            "password": "password123",
            "firstName": "tester1111",
            "lastName": "User"
        }
        """.formatted(userId);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isCreated());
        System.out.println(userId);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"MODERATOR"})
    void getUserById() throws Exception {
        UUID userId = UUID.fromString("d5a57647-eb87-4fcc-a1c1-ec55c4244f84"); //05e68748-1ec0-4fbb-8a64-96e266098e7d
        UserResponse userResponse = new UserResponse
                ("tester1111", "User",
                        "tester1111@example.com", List.of("MODERATOR"), List.of());

        when(userService.getUserById(userId)).thenReturn(userResponse);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("tester1111"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.email").value("tester1111@example.com"))
                .andExpect(jsonPath("$.roles[0]").value("MODERATOR"));
    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    void testHello() throws Exception {
        mockMvc.perform(get("/api/users/hello"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("user")); // Замените на ожидаемое имя
    }

}