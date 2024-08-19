package com.itm.space.backendresources.service;

import com.itm.space.backendresources.BaseIntegrationTest;
import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.api.response.UserResponse;
import com.itm.space.backendresources.exception.BackendResourcesException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.MappingsRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ServiceTest extends BaseIntegrationTest {
    @Autowired
    private UserServiceImpl userService;

    @MockBean
    private Keycloak keycloakClient;

    @Mock
    private RealmResource realmResource;

    @Mock
    private UsersResource usersResource;

    @BeforeEach
    void setUp() {
        when(keycloakClient.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
    }

    @Test
    void createUser_success() {
        // Arrange
        UserRequest userRequest = new UserRequest("testUser", "test@test.com", "password", "Test", "User");

        Response response = Response.status(HttpStatus.CREATED.value()).entity("{\"id\":\"test-user-id\"}").build();
        when(keycloakClient.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(Mockito.any(UserRepresentation.class))).thenReturn(response);

        // Act
        userService.createUser(userRequest);

        // Assert
        Mockito.verify(keycloakClient).realm("ITM");
        Mockito.verify(realmResource).users();
        Mockito.verify(usersResource).create(Mockito.any(UserRepresentation.class));
    }

    @Test
    void GetUserById() {
        UUID userId = UUID.randomUUID();
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setId(userId.toString());
        userRepresentation.setFirstName("John");
        userRepresentation.setLastName("Doe");
        userRepresentation.setEmail("john.doe@example.com");

        RealmResource mockRealmResource = mock(RealmResource.class);
        UsersResource mockUsersResource = mock(UsersResource.class);
        UserResource mockUserResource = mock(UserResource.class);
        RoleMappingResource mockRoleMappingResource = mock(RoleMappingResource.class);
        RoleScopeResource mockRoleScopeResource = mock(RoleScopeResource.class);

        when(keycloakClient.realm(anyString())).thenReturn(mockRealmResource);
        when(mockRealmResource.users()).thenReturn(mockUsersResource);
        when(mockUsersResource.get(userId.toString())).thenReturn(mockUserResource);
        when(mockUserResource.toRepresentation()).thenReturn(userRepresentation);
        when(mockUserResource.roles()).thenReturn(mockRoleMappingResource);
        MappingsRepresentation mockMappingsRepresentation = mock(MappingsRepresentation.class);
        when(mockRoleMappingResource.getAll()).thenReturn(mockMappingsRepresentation);
        when(mockMappingsRepresentation.getRealmMappings()).thenReturn(Collections.emptyList());
        when(mockUserResource.groups()).thenReturn(Collections.emptyList());
        UserResponse userResponse = userService.getUserById(userId);

        assertNotNull(userResponse);
        assertEquals("John", userResponse.getFirstName());
        assertEquals("Doe", userResponse.getLastName());
        assertEquals("john.doe@example.com", userResponse.getEmail());
    }
    @Test
    void ExceptionIfUserNotFound() {
        UUID userId = UUID.randomUUID();
        RealmResource mockRealmResource = mock(RealmResource.class);
        UsersResource mockUsersResource = mock(UsersResource.class);

        when(keycloakClient.realm(anyString())).thenReturn(mockRealmResource);
        when(mockRealmResource.users()).thenReturn(mockUsersResource);
        when(mockUsersResource.get(userId.toString())).thenThrow(new NotFoundException("User not found"));

        assertThrows(BackendResourcesException.class, () -> userService.getUserById(userId));
    }

}
