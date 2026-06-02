package com.cts.userservice.tests;


import com.cts.userservice.controller.UserController;
import com.cts.userservice.dto.request.LoginRequest;
import com.cts.userservice.dto.request.RegisterRequest;
import com.cts.userservice.dto.request.UpdateProfileRequest;
import com.cts.userservice.dto.response.LoginResponse;
import com.cts.userservice.dto.response.UserResponse;
import com.cts.userservice.dto.response.ValidationResponse;
import com.cts.userservice.exception.GlobalExceptionHandler;
import com.cts.userservice.exception.customexception.UserNotFoundException;
import com.cts.userservice.model.RoleName;
import com.cts.userservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer tests for {@link UserController}.
 *
 * <p>Uses a standalone {@link MockMvc} setup (no full Spring context or
 * Security filter chain) wired with the real {@link GlobalExceptionHandler}, so
 * routing, request binding, bean validation, and error mapping are all
 * exercised while the service is mocked.</p>
 */
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock private UserService userService;
    @InjectMocks private UserController userController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Builds the standalone {@link MockMvc} instance, registering the real
     * {@link GlobalExceptionHandler} so exception-to-response mapping is tested.
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    /** Verifies a valid registration returns 201 with the created profile. */
    @Test
    @DisplayName("POST /register returns 201 with the created profile")
    void register_created() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .name("Alice").email("alice@example.com").password("password123").build();
        UserResponse response = UserResponse.builder()
                .userId(10L).name("Alice").email("alice@example.com").roleName(RoleName.CUSTOMER).build();
        when(userService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(10))
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    /**
     * Verifies an invalid registration body is rejected with 400 and the
     * {@code VALIDATION_FAILED} error code from the global handler.
     */
    @Test
    @DisplayName("POST /register returns 400 when the body fails validation")
    void register_validationFailure() throws Exception {
        RegisterRequest invalid = RegisterRequest.builder()
                .name("").email("not-an-email").password("short").build();

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));
    }

    /** Verifies a valid login returns 200 with the issued token and role. */
    @Test
    @DisplayName("POST /login returns 200 with a token")
    void login_ok() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("alice@example.com").password("password123").build();
        LoginResponse response = LoginResponse.builder()
                .token("jwt-token").userId(10L).roleName(RoleName.CUSTOMER).expiresIn(3600000L).build();
        when(userService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.roleName").value("CUSTOMER"));
    }

    /**
     * Verifies the validate endpoint forwards the {@code Authorization} header
     * to the service and returns the resolved user id.
     */
    @Test
    @DisplayName("POST /auth/validate passes the Authorization header through")
    void validate_ok() throws Exception {
        when(userService.validate(eq("Bearer jwt-token")))
                .thenReturn(ValidationResponse.builder().userId(42L).build());

        mockMvc.perform(post("/api/users/auth/validate")
                        .header("Authorization", "Bearer jwt-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(42));
    }

    /** Verifies the list endpoint returns all users as a JSON array. */
    @Test
    @DisplayName("GET /api/users returns the full list")
    void listAll_ok() throws Exception {
        when(userService.listAllUsers()).thenReturn(List.of(
                UserResponse.builder().userId(1L).name("Alice").email("alice@example.com").roleName(RoleName.CUSTOMER).build(),
                UserResponse.builder().userId(2L).name("Bob").email("bob@example.com").roleName(RoleName.ADMIN).build()
        ));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].roleName").value("ADMIN"));
    }

    /**
     * Verifies a {@link UserNotFoundException} from the service is mapped to a
     * 404 response carrying the {@code USER_NOT_FOUND} code and request path.
     */
    @Test
    @DisplayName("GET /api/users/{id} returns 404 when the service reports not found")
    void getById_notFound() throws Exception {
        when(userService.getUserById(99L)).thenThrow(new UserNotFoundException("No user found with id 99"));

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.path").value("/api/users/99"));
    }

    /** Verifies a valid profile update returns 200 with the updated profile. */
    @Test
    @DisplayName("PUT /api/users/{id} returns 200 with the updated profile")
    void updateProfile_ok() throws Exception {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .name("Alice B").email("aliceb@example.com").build();
        when(userService.updateProfile(eq(10L), any(UpdateProfileRequest.class)))
                .thenReturn(UserResponse.builder()
                        .userId(10L).name("Alice B").email("aliceb@example.com").roleName(RoleName.CUSTOMER).build());

        mockMvc.perform(put("/api/users/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice B"));
    }
}