package com.fitness.activityservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserValidationService {

    private final WebClient userServiceWebClient;

    public boolean validateUser(String userId) {
        try {
            log.info(userId + " This is my userId through validate method");
            String uri = "/api/users/" + userId + "/validate";
            log.info("Sending request to User Service: GET {}", uri);

            return userServiceWebClient.get()
                    .uri(uri) // Using the prepared string
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND)
                throw new RuntimeException("User Not Found:" + userId);
            else if (e.getStatusCode() == HttpStatus.BAD_REQUEST)
                throw new RuntimeException("Invalid Request" + userId);
            else {
                // This handles other 4xx/5xx errors
                throw new RuntimeException("Error communicating with User Service: " + e.getMessage());
            }
        }

    }

}
