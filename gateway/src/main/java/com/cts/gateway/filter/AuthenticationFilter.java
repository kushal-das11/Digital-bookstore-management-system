package com.cts.gateway.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationFilter implements GlobalFilter, Ordered{

    private final WebClient.Builder webClientBuilder;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 1. Allow public endpoints
        if (isPublic(path)) {
            return chain.filter(exchange);
        }

        // 2. Extract Authorization header
        String authHeader = request.getHeaders().getFirst("Authorization");
        log.info(authHeader);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return deny(exchange, HttpStatus.UNAUTHORIZED);
        }

        // 3. Call user-service for validation + authorization
        return webClientBuilder.build()
                .post()
                .uri("http://user-service/api/users/auth/validate")
                .header("Authorization", authHeader)
                .header("X-Original-Path", path)
                .header("X-Http-Method", request.getMethod() != null ? request.getMethod().name() : "GET")
                .retrieve()
                .bodyToMono(ValidationResponse.class)
                .flatMap(response -> {

                    // 4. Inject only USER ID (no role needed)
                    ServerHttpRequest mutatedRequest = request.mutate()
                            .header("X-User-Id", response.userId().toString())
                            .build();

                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                })
                .onErrorResume(error -> {
                    if (error instanceof WebClientResponseException ex &&
                            ex.getStatusCode().value() == 403) {
                        return deny(exchange, HttpStatus.FORBIDDEN);
                    }
                    return deny(exchange, HttpStatus.UNAUTHORIZED);
                });
    }

    private boolean isPublic(String path) {
        return path.startsWith("/api/users/login")
                || path.startsWith("/api/users/register")

                //  Swagger UI
                || path.startsWith("/swagger-ui")
                || path.startsWith("/webjars")

                //  Swagger docs (gateway)
                || path.startsWith("/v3/api-docs")

                // CRITICAL FIX (service swagger)
                || path.contains("/v3/api-docs")

                //fallback safety
                || path.contains("swagger")
                || path.contains("api-docs");
    }

    private Mono<Void> deny(ServerWebExchange exchange, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -1; // execute before routing
    }

    // match user-service response (ONLY userId needed)
    private record ValidationResponse(Long userId) {}
}