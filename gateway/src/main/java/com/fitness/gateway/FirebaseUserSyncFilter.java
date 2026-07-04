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

@Component
@Slf4j
@RequiredArgsConstructor
public class FirebaseUserSyncFilter implements WebFilter {
    private final UserService userService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        RegisterRequest registerRequest = getUserDetails(token);

        if (userId == null && registerRequest != null) {
            userId = registerRequest.getFirebaseId();
        }

        if (userId != null && token != null) {
            String finalUserId = userId;
            return userService.validateUser(userId)
                    .flatMap(exist -> {
                        if (!exist && registerRequest != null) {
                            return userService.registerUser(registerRequest).then(Mono.empty());
                        } else {
                            log.info("User already exists, Skipping sync");
                            return Mono.empty();
                        }
                    })
                    .then(Mono.defer(() -> {
                        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                                .header("X-User-ID", finalUserId)
                                .build();
                        return chain.filter(exchange.mutate().request(mutatedRequest).build());
                    }));
        }

        return chain.filter(exchange);
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
            
            // Firebase uses "sub" for the unique user ID (UID)
            registerRequest.setFirebaseId(claims.getStringClaim("sub"));
            registerRequest.setPassword("dummy@123123");

            // Firebase provides a single "name" claim instead of given/family names
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
