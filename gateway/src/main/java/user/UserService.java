package user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final WebClient userServiceWebClient;

    public Mono<Boolean> validateUser(String userId) {

            log.info(userId + " This is my userId through validate method");
//            String uri = "/api/users/" + userId + "/validate";
//            log.info("Sending request to User Service: GET {}", uri);

        return userServiceWebClient.get()
                .uri("/api/users/{userId}/validate", userId) // Using the prepared string
                .retrieve()
                .bodyToMono(Boolean.class)
                .onErrorResume(WebClientResponseException.class, e -> {
                            if (e.getStatusCode() == HttpStatus.NOT_FOUND)
                                return Mono.error(new RuntimeException("User Not Found:" + userId));
                            else if (e.getStatusCode() == HttpStatus.BAD_REQUEST)
                                return Mono.error(new RuntimeException("Invalid Request" + userId));
                            else {
                                // This handles other 4xx/5xx errors
                                return Mono.error(new RuntimeException("Error communicating with User Service: " + e.getMessage()));
                            }
                        }
                );
    }

    public Mono<UserResponse> registerUser(RegisterRequest request) {


        log.info(request.getEmail() + " Calling user Registration API");
//            String uri = "/api/users/" + userId + "/validate";
//            log.info("Sending request to User Service: GET {}", uri);

        return userServiceWebClient.post()
                .uri("/api/users/register")
                .bodyValue(request)// Using the prepared string
                .retrieve()
                .bodyToMono(UserResponse.class)
                .onErrorResume(WebClientResponseException.class, e -> {
                            if (e.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR)
                                return Mono.error(new RuntimeException("Internal server error" + e.getMessage()));
                            else if (e.getStatusCode() == HttpStatus.BAD_REQUEST)
                                return Mono.error(new RuntimeException("Bad Request" + e.getMessage()));
                            else {
                                // This handles other 4xx/5xx errors
                                return Mono.error(new RuntimeException("Error communicating with User Service: " + e.getMessage()));
                            }
                        }
                );
    }
}

//        } catch (WebClientResponseException e) {
//            if (e.getStatusCode() == HttpStatus.NOT_FOUND)
//                throw new RuntimeException("User Not Found:" + userId);
//            else if (e.getStatusCode() == HttpStatus.BAD_REQUEST)
//                throw new RuntimeException("Invalid Request" + userId);
//            else {
//                // This handles other 4xx/5xx errors
//                throw new RuntimeException("Error communicating with User Service: " + e.getMessage());
//            }
      //  }
//
//    }
//
//}
