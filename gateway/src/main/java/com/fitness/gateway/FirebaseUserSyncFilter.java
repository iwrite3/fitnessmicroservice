package com.fitness.gateway;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import com.fitness.gateway.user.RegisterRequest;
import com.fitness.gateway.user.UserService;
import com.fitness.gateway.user.UserResponse;

@Component
@Slf4j
@RequiredArgsConstructor
public class FirebaseUserSyncFilter implements WebFilter {
    private final UserService userService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        RegisterRequest registerRequest = getUserDetails(token);

        if (registerRequest == null || registerRequest.getFirebaseId() == null) {
            return chain.filter(exchange);
        }

        String userId = registerRequest.getFirebaseId();

        return userService.validateUser(userId)
                .doOnError(e -> log.error("validateUser failed for {}: {}", userId, e.toString(), e))
                .doOnSuccess(exists -> log.info("validateUser returned {} for {}", exists, userId))
                .flatMap(exists -> {
                    if (!exists) {
                        return userService.registerUser(registerRequest)
                                .doOnError(e -> log.error("registerUser failed for {}: {}", userId, e.toString(), e))
                                .doOnSuccess(r -> log.info("registerUser succeeded for {}", userId));
                    }
                    return Mono.just(new UserResponse());
                })
                // SAFETY NET: never let a user-service hiccup kill the whole request.
                // Log it, but still let the request through to its real destination
                // (activity-service, etc.) with the header we already have.
                .onErrorResume(e -> {
                    log.error("User sync failed for {}, proceeding without blocking request: {}",
                            userId, e.toString(), e);
                    return Mono.empty();
                })
                .then(Mono.defer(() -> {
                    ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                            .header("X-User-ID", userId)
                            .build();
                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                }));
    }

    private RegisterRequest getUserDetails(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return null;
        }
        try {
            String tokenWithoutBearer = token.replace("Bearer ", "").trim();
            SignedJWT signedJWT = SignedJWT.parse(tokenWithoutBearer);
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            RegisterRequest registerRequest = new RegisterRequest();
            registerRequest.setEmail(claims.getStringClaim("email"));
            registerRequest.setFirebaseId(claims.getStringClaim("sub"));
            registerRequest.setPassword("dummy@123123");

            String fullName = claims.getStringClaim("name");
            if (fullName != null && fullName.contains(" ")) {
                String[] nameParts = fullName.split(" ", 2);
                registerRequest.setFirstName(nameParts[0]);
                registerRequest.setLastName(nameParts[1]);
            } else {
                registerRequest.setFirstName(fullName != null ? fullName : "Fitness");
                registerRequest.setLastName("User");
            }

            return registerRequest;
        } catch (Exception e) {
            log.error("Failed to parse Firebase JWT", e);
            return null;
        }
    }
}
