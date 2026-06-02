package com.cts.userservice.tests;

import com.cts.userservice.dto.request.LoginRequest;
import com.cts.userservice.dto.request.RegisterRequest;
import com.cts.userservice.dto.request.UpdateProfileRequest;
import com.cts.userservice.dto.response.LoginResponse;
import com.cts.userservice.dto.response.UserResponse;
import com.cts.userservice.dto.response.ValidationResponse;
import com.cts.userservice.exception.customexception.InvalidPasswordException;
import com.cts.userservice.exception.customexception.PasswordHashingException;
import com.cts.userservice.exception.customexception.RoleNotFoundException;
import com.cts.userservice.exception.customexception.UserAlreadyExistsException;
import com.cts.userservice.exception.customexception.UserNotFoundException;
import com.cts.userservice.model.Authentication;
import com.cts.userservice.model.Role;
import com.cts.userservice.model.RoleName;
import com.cts.userservice.model.User;
import com.cts.userservice.repository.AuthenticationRepository;
import com.cts.userservice.repository.RoleRepository;
import com.cts.userservice.repository.UserRepository;
import com.cts.userservice.security.JwtUtil;
import com.cts.userservice.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link UserServiceImpl}, exercising the business logic in
 * isolation with all collaborators mocked.
 *
 * <p>Each public service method has its own {@link Nested} group covering the
 * happy path and the relevant failure branches.</p>
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private FilterChainProxy springSecurityFilterChain;
    @Mock private AuthenticationRepository authenticationRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks private UserServiceImpl userService;

    private static final long JWT_EXPIRATION_MS = 3_600_000L;

    private Role customerRole;

    /**
     * Sets the {@code @Value}-injected expiration field (not populated under
     * plain Mockito) and prepares a reusable {@code CUSTOMER} role.
     */
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "jwtExpirationMs", JWT_EXPIRATION_MS);
        customerRole = Role.builder().roleId(1).roleName(RoleName.CUSTOMER).build();
    }

    /**
     * Builds a {@link User} with the given identity and role for use as stubbed
     * repository output.
     *
     * @param id    the user id
     * @param name  the display name
     * @param email the email address
     * @param role  the assigned role
     * @return a populated {@link User} instance
     */
    private User userWithId(Long id, String name, String email, Role role) {
        return User.builder().userId(id).name(name).email(email).role(role).build();
    }

    /** Tests for {@link UserServiceImpl#register}. */
    @Nested
    @DisplayName("register")
    class Register {

        private RegisterRequest request;

        /** Prepares a valid registration request before each test. */
        @BeforeEach
        void init() {
            request = RegisterRequest.builder()
                    .name("Alice")
                    .email("alice@example.com")
                    .password("password123")
                    .build();
        }

        /**
         * Verifies a successful registration persists the user and credentials
         * (with the encoded hash, linked to the saved user) and returns the
         * mapped profile.
         */
        @Test
        @DisplayName("creates user and credentials, returns profile")
        void register_success() {
            when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
            when(roleRepository.findByRoleName(RoleName.CUSTOMER)).thenReturn(Optional.of(customerRole));
            when(userRepository.save(any(User.class)))
                    .thenReturn(userWithId(10L, "Alice", "alice@example.com", customerRole));
            when(passwordEncoder.encode("password123")).thenReturn("hashed-pw");

            UserResponse response = userService.register(request);

            assertThat(response.getUserId()).isEqualTo(10L);
            assertThat(response.getName()).isEqualTo("Alice");
            assertThat(response.getEmail()).isEqualTo("alice@example.com");
            assertThat(response.getRoleName()).isEqualTo(RoleName.CUSTOMER);

            ArgumentCaptor<Authentication> authCaptor = ArgumentCaptor.forClass(Authentication.class);
            verify(authenticationRepository).save(authCaptor.capture());
            assertThat(authCaptor.getValue().getPasswordHash()).isEqualTo("hashed-pw");
            assertThat(authCaptor.getValue().getUser().getUserId()).isEqualTo(10L);
        }

        /**
         * Verifies registration fails fast when the email is already taken and
         * persists nothing.
         */
        @Test
        @DisplayName("throws when email already exists and does not persist anything")
        void register_emailExists() {
            when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

            assertThatThrownBy(() -> userService.register(request))
                    .isInstanceOf(UserAlreadyExistsException.class)
                    .hasMessageContaining("alice@example.com");

            verify(userRepository, never()).save(any());
            verifyNoInteractions(authenticationRepository, passwordEncoder);
        }

        /**
         * Verifies registration fails when the default {@code CUSTOMER} role is
         * not configured.
         */
        @Test
        @DisplayName("throws when default CUSTOMER role is missing")
        void register_roleNotFound() {
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(roleRepository.findByRoleName(RoleName.CUSTOMER)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.register(request))
                    .isInstanceOf(RoleNotFoundException.class);

            verify(userRepository, never()).save(any());
        }

        /**
         * Verifies a runtime failure during password hashing is wrapped in
         * {@link PasswordHashingException} and credentials are not saved.
         */
        @Test
        @DisplayName("wraps hashing failures in PasswordHashingException")
        void register_hashingFails() {
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(roleRepository.findByRoleName(RoleName.CUSTOMER)).thenReturn(Optional.of(customerRole));
            when(userRepository.save(any(User.class)))
                    .thenReturn(userWithId(10L, "Alice", "alice@example.com", customerRole));
            when(passwordEncoder.encode(anyString())).thenThrow(new RuntimeException("boom"));

            assertThatThrownBy(() -> userService.register(request))
                    .isInstanceOf(PasswordHashingException.class)
                    .hasCauseInstanceOf(RuntimeException.class);

            verify(authenticationRepository, never()).save(any());
        }
    }

    /** Tests for {@link UserServiceImpl#login}. */
    @Nested
    @DisplayName("login")
    class Login {

        private LoginRequest request;
        private User user;
        private Authentication auth;

        /** Prepares a user, credentials, and a valid login request. */
        @BeforeEach
        void init() {
            request = LoginRequest.builder().email("alice@example.com").password("password123").build();
            user = userWithId(10L, "Alice", "alice@example.com", customerRole);
            auth = Authentication.builder().authId(1).user(user).passwordHash("hashed-pw").build();
        }

        /**
         * Verifies valid credentials yield a token, the expected identity
         * context, and an updated last-login timestamp.
         */
        @Test
        @DisplayName("returns token and updates last login on valid credentials")
        void login_success() {
            when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
            when(authenticationRepository.findByUser_UserId(10L)).thenReturn(Optional.of(auth));
            when(passwordEncoder.matches("password123", "hashed-pw")).thenReturn(true);
            when(jwtUtil.generateToken(10L, RoleName.CUSTOMER)).thenReturn("jwt-token");

            LoginResponse response = userService.login(request);

            assertThat(response.getToken()).isEqualTo("jwt-token");
            assertThat(response.getUserId()).isEqualTo(10L);
            assertThat(response.getRoleName()).isEqualTo(RoleName.CUSTOMER);

            ArgumentCaptor<Authentication> captor = ArgumentCaptor.forClass(Authentication.class);
            verify(authenticationRepository).save(captor.capture());
            assertThat(captor.getValue().getLastLogin()).isNotNull();
        }

        /** Verifies login fails when no user matches the supplied email. */
        @Test
        @DisplayName("throws when no user matches the email")
        void login_userNotFound() {
            when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(UserNotFoundException.class);

            verifyNoInteractions(jwtUtil);
        }

        /** Verifies login fails when the user has no credentials record. */
        @Test
        @DisplayName("throws when credentials record is missing")
        void login_credentialsNotFound() {
            when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
            when(authenticationRepository.findByUser_UserId(10L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(UserNotFoundException.class);
        }

        /**
         * Verifies a wrong password is rejected and no token is issued nor
         * last-login recorded.
         */
        @Test
        @DisplayName("throws on wrong password and never issues a token")
        void login_invalidPassword() {
            when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
            when(authenticationRepository.findByUser_UserId(10L)).thenReturn(Optional.of(auth));
            when(passwordEncoder.matches("password123", "hashed-pw")).thenReturn(false);

            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(InvalidPasswordException.class);

            verify(jwtUtil, never()).generateToken(any(), any());
            verify(authenticationRepository, never()).save(any());
        }
    }

    /** Tests for {@link UserServiceImpl#getUserById}. */
    @Nested
    @DisplayName("getUserById")
    class GetUserById {

        /** Verifies an existing user is returned as a mapped profile. */
        @Test
        @DisplayName("returns mapped profile when found")
        void getUserById_success() {
            when(userRepository.findById(10L))
                    .thenReturn(Optional.of(userWithId(10L, "Alice", "alice@example.com", customerRole)));

            UserResponse response = userService.getUserById(10L);

            assertThat(response.getUserId()).isEqualTo(10L);
            assertThat(response.getEmail()).isEqualTo("alice@example.com");
        }

        /** Verifies a missing user yields {@link UserNotFoundException}. */
        @Test
        @DisplayName("throws when user does not exist")
        void getUserById_notFound() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserById(99L))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    /** Tests for {@link UserServiceImpl#updateProfile}. */
    @Nested
    @DisplayName("updateProfile")
    class UpdateProfile {

        private User existing;

        /** Prepares an existing user before each test. */
        @BeforeEach
        void init() {
            existing = userWithId(10L, "Alice", "alice@example.com", customerRole);
        }

        /**
         * Verifies the profile is updated when the email changes to one not
         * already in use.
         */
        @Test
        @DisplayName("updates name and email when new email is free")
        void updateProfile_emailChanged_free() {
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .name("Alice B").email("new@example.com").build();

            when(userRepository.findById(10L)).thenReturn(Optional.of(existing));
            when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            UserResponse response = userService.updateProfile(10L, request);

            assertThat(response.getName()).isEqualTo("Alice B");
            assertThat(response.getEmail()).isEqualTo("new@example.com");
        }

        /**
         * Verifies the uniqueness check is skipped when the email is unchanged.
         */
        @Test
        @DisplayName("does not check uniqueness when email is unchanged")
        void updateProfile_emailUnchanged() {
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .name("Alice B").email("alice@example.com").build();

            when(userRepository.findById(10L)).thenReturn(Optional.of(existing));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            UserResponse response = userService.updateProfile(10L, request);

            assertThat(response.getName()).isEqualTo("Alice B");
            verify(userRepository, never()).existsByEmail(anyString());
        }

        /**
         * Verifies updating to an email already used by another account is
         * rejected and nothing is persisted.
         */
        @Test
        @DisplayName("throws when the new email is already taken")
        void updateProfile_emailTaken() {
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .name("Alice B").email("taken@example.com").build();

            when(userRepository.findById(10L)).thenReturn(Optional.of(existing));
            when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

            assertThatThrownBy(() -> userService.updateProfile(10L, request))
                    .isInstanceOf(UserAlreadyExistsException.class);

            verify(userRepository, never()).save(any());
        }

        /** Verifies updating a non-existent user yields {@link UserNotFoundException}. */
        @Test
        @DisplayName("throws when the user does not exist")
        void updateProfile_userNotFound() {
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .name("X").email("x@example.com").build();
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateProfile(99L, request))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    /** Tests for {@link UserServiceImpl#listAllUsers}. */
    @Nested
    @DisplayName("listAllUsers")
    class ListAllUsers {

        /** Verifies every user entity is mapped to a response in order. */
        @Test
        @DisplayName("maps every user to a response")
        void listAllUsers_returnsMapped() {
            when(userRepository.findAll()).thenReturn(List.of(
                    userWithId(1L, "Alice", "alice@example.com", customerRole),
                    userWithId(2L, "Bob", "bob@example.com", customerRole)
            ));

            List<UserResponse> result = userService.listAllUsers();

            assertThat(result).hasSize(2);
            assertThat(result).extracting(UserResponse::getEmail)
                    .containsExactly("alice@example.com", "bob@example.com");
        }

        /** Verifies an empty repository yields an empty list. */
        @Test
        @DisplayName("returns an empty list when there are no users")
        void listAllUsers_empty() {
            when(userRepository.findAll()).thenReturn(List.of());
            assertThat(userService.listAllUsers()).isEmpty();
        }
    }

    /** Tests for {@link UserServiceImpl#validate}. */
    @Nested
    @DisplayName("validate")
    class Validate {

        /**
         * Verifies the {@code Bearer} prefix is stripped and the extracted user
         * id is returned.
         */
        @Test
        @DisplayName("strips the Bearer prefix and returns the user id")
        void validate_success() {
            when(jwtUtil.extractUserId("the-token")).thenReturn("42");

            ValidationResponse response = userService.validate("Bearer the-token");

            assertThat(response.getUserId()).isEqualTo(42L);
        }

        /**
         * Documents current behavior: a non-numeric token subject surfaces as a
         * raw {@link NumberFormatException} rather than a custom exception.
         */
        @Test
        @DisplayName("propagates failures when the token id is not numeric")
        void validate_nonNumericId() {
            when(jwtUtil.extractUserId(anyString())).thenReturn("not-a-number");

            assertThatThrownBy(() -> userService.validate("Bearer the-token"))
                    .isInstanceOf(NumberFormatException.class);
        }
    }
}
