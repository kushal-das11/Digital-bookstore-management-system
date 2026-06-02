package com.cts.userservice.config;

import com.cts.userservice.security.filter.JwtAuthFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import static com.cts.userservice.model.RoleName.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration for the user service.
 *
 * <p>Configures a stateless, JWT-based security model: CSRF, form login, and
 * HTTP Basic are disabled, no HTTP session is created, and the
 * {@link JwtAuthFilter} runs before the standard username/password filter to
 * populate the security context from a bearer token.</p>
 *
 * <p>Authentication endpoints (login, register) and the API documentation are
 * left public. The gateway-facing {@code /api/users/auth/validate} endpoint
 * carries a custom, fine-grained authorization rule that inspects the original
 * request path and method (forwarded via the {@code X-Original-Path} and
 * {@code X-Http-Method} headers) to authorize access to the downstream
 * inventory, orders, cart, reviews, and catalog services by role. All other
 * requests require authentication.</p>
 *
 * <p>Also exposes a {@link BCryptPasswordEncoder} bean used throughout the
 * service for hashing and verifying passwords. The {@link JwtAuthFilter}
 * dependency is injected via the Lombok-generated constructor.</p>
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    /** Filter that authenticates requests from a JWT bearer token. */
    private final JwtAuthFilter jwtAuthFilter;

    // =========================
    // COMMON CONSTANTS
    // =========================
    /** HTTP GET method name. */
    private static final String GET = "GET";
    /** HTTP POST method name. */
    private static final String POST = "POST";
    /** HTTP PUT method name. */
    private static final String PUT = "PUT";
    /** HTTP PATCH method name. */
    private static final String PATCH = "PATCH";
    /** HTTP DELETE method name. */
    private static final String DELETE = "DELETE";

    /** Base path of the inventory service. */
    private static final String INVENTORY = "/api/inventory";
    /** Base path of the orders service. */
    private static final String ORDERS = "/api/orders";
    /** Base path of the cart endpoints. */
    private static final String CART = "/api/orders/cart";
    /** Base path of the reviews service. */
    private static final String REVIEWS = "/api/reviews";
    /** Base path of the catalog service. */
    private static final String CATALOG = "/api/catalog";

    /**
     * Builds the application's security filter chain.
     *
     * <p>Disables CSRF, form login, and HTTP Basic; enforces stateless session
     * management; and inserts the {@link JwtAuthFilter} ahead of the
     * {@link UsernamePasswordAuthenticationFilter}. It then declares the
     * authorization rules: a set of permit-all endpoints, a custom
     * path-and-method-based access rule for the validation endpoint that maps
     * downstream service routes to the {@code ADMIN} and {@code CUSTOMER} roles,
     * and a catch-all requiring authentication for everything else.</p>
     *
     * @param http the {@link HttpSecurity} builder to configure
     * @return the fully configured {@link SecurityFilterChain}
     * @throws Exception if an error occurs while building the chain
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                .authorizeHttpRequests(auth -> auth

                        // Public endpoints
                        .requestMatchers(HttpMethod.POST,
                                "/api/users/login",
                                "/api/users/register"
                        ).permitAll()

                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // Validation endpoint
                        .requestMatchers("/api/users/auth/validate")
                        .access((authentication, context) -> {

                            HttpServletRequest request = context.getRequest();

                            String path = request.getHeader("X-Original-Path");
                            String method = request.getHeader("X-Http-Method");

                            // Allow swagger/direct calls
                            if (path == null) {
                                return new AuthorizationDecision(true);
                            }

                            boolean isAdmin = hasRole(authentication.get(), ADMIN.name());
                            boolean isCustomer = hasRole(authentication.get(), CUSTOMER.name());

                            /*
                             * =========================
                             * INVENTORY
                             * =========================
                             */

                            if (path.startsWith(INVENTORY)
                                    && (path.endsWith("reserve")
                                    || path.endsWith("reduce")
                                    || path.contains("availability"))) {
                                return new AuthorizationDecision(isAdmin || isCustomer);
                            }

                            if (path.startsWith(INVENTORY)) {
                                return new AuthorizationDecision(isAdmin);
                            }

                            /*
                             * =========================
                             * ORDERS
                             * =========================
                             */

                            // place order
                            if (path.equals(ORDERS) && POST.equals(method)) {
                                return new AuthorizationDecision(isCustomer);
                            }

                            // get order / items / status
                            if (path.matches(ORDERS + "/\\d+")
                                    || path.matches(ORDERS + "/\\d+/items")) {
                                return new AuthorizationDecision(isCustomer || isAdmin);
                            }

                            // get orders by user || cancel order
                            if (path.startsWith(ORDERS) && (path.contains("/user/") || path.contains("/cancel"))) {
                                return new AuthorizationDecision(isCustomer);
                            }

                            //customer can check status
                            if (path.startsWith(ORDERS)  && path.contains("/status") && GET.equals(method)) {
                                return new AuthorizationDecision(isCustomer);
                            }

                            // update order status
                            if (path.startsWith(ORDERS)  && path.contains("/status") && PATCH.equals(method)) {
                                return new AuthorizationDecision(isAdmin);
                            }

                            // get all orders
                            if (path.equals(ORDERS) && GET.equals(method)) {
                                return new AuthorizationDecision(isAdmin);
                            }

                            /*
                             * =========================
                             * CART
                             * =========================
                             */

                            if (path.startsWith(CART)) {
                                return new AuthorizationDecision(isCustomer);
                            }

                            /*
                             * =========================
                             * REVIEWS
                             * =========================
                             */

                            // public read
                            if (path.equals(REVIEWS) && GET.equals(method)) {
                                return new AuthorizationDecision(true);
                            }

                            // customer
                            if (POST.equals(method) && path.equals(REVIEWS)) {
                                return new AuthorizationDecision(isCustomer);
                            }

                            if (path.contains("/edit")) {
                                return new AuthorizationDecision(isCustomer);
                            }

                            if (path.startsWith(REVIEWS + "/user")) {
                                return new AuthorizationDecision(isCustomer);
                            }

                            // admin
                            if (path.contains("/moderate")) {
                                return new AuthorizationDecision(isAdmin);
                            }


                            /*
                             * =========================
                             * CATALOG
                             * =========================
                             */

                            // authors & categories
                            if (path.startsWith(CATALOG + "/authors")
                                    || path.startsWith(CATALOG + "/categories")) {

                                if (GET.equals(method)) {
                                    return new AuthorizationDecision(isCustomer || isAdmin);
                                }
                                return new AuthorizationDecision(isAdmin);
                            }

                            // books
                            if (path.startsWith(CATALOG + "/books")) {
                                if (GET.equals(method)) {
                                    return new AuthorizationDecision(true);
                                }
                                return new AuthorizationDecision(isAdmin);
                            }

                            /*
                             * =========================
                             * DEFAULT
                             * =========================
                             */
                            return new AuthorizationDecision(true);
                        })

                        .anyRequest().authenticated()
                )
                .build();
    }

    /**
     * Checks whether the given authentication holds the specified role.
     *
     * <p>Matches against authorities using the {@code ROLE_} prefix convention
     * (for example, role {@code ADMIN} maps to authority {@code ROLE_ADMIN}).</p>
     *
     * @param auth the authentication to inspect; may be {@code null}
     * @param role the unprefixed role name to check for
     * @return {@code true} if the authentication carries the matching authority
     */
    private boolean hasRole(@Nullable Authentication auth, String role) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    /**
     * Provides the password encoder used for hashing and verifying passwords.
     *
     * @return a {@link BCryptPasswordEncoder} instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}