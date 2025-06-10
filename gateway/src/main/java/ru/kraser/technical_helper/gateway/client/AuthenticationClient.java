package ru.kraser.technical_helper.gateway.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.kraser.technical_helper.common_module.dto.auth.AuthenticationRequest;
import ru.kraser.technical_helper.common_module.dto.auth.AuthenticationResponse;
import ru.kraser.technical_helper.common_module.exception.AuthException;
import ru.kraser.technical_helper.common_module.exception.ServerException;

import static ru.kraser.technical_helper.common_module.util.Constant.*;

@Service
@RequiredArgsConstructor
public class AuthenticationClient {
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public AuthenticationResponse authenticate(AuthenticationRequest request) {

        Mono<AuthenticationResponse> postResponse = webClient
                .post()
                .uri(MAIN_SERVER_URL + BASE_URL + AUTH_URL)
                .contentType(MediaType.APPLICATION_JSON)

                .bodyValue(request)
                .retrieve()
                /*.onStatus(HttpStatusCode::is5xxServerError,
                        clientResponse -> clientResponse.bodyToMono(ServerException.class)
                )*/
                .onStatus(HttpStatusCode::is5xxServerError,
                        clientResponse -> Mono.error(
                                new ServerException(SERVER_ERROR)))

                /*.onStatus(HttpStatusCode::is4xxClientError,
                        clientResponse -> clientResponse.bodyToMono(Map.class)
                                .flatMap(body -> {
                                    try {
                                        var message = objectMapper.writeValueAsString(body);
                                        ApiError errorResponse = objectMapper.readValue(message, ApiError.class);
                                        return Mono.error(
                                                new AuthException(errorResponse.message()
                                                        *//*clientResponse.statusCode().value(),
                                                        "My custom error message", errorResponse*//*));
                                    } catch (JsonProcessingException jsonProcessingException) {
                                        return Mono.error(new ServerException(SERVER_ERROR));
                                    }
                                }))*/

                /*.onStatus(HttpStatusCode::is4xxClientError,
                        clientResponse -> clientResponse.bodyToMono(AuthException.class)
                                )*/

                .onStatus(HttpStatus.UNAUTHORIZED::equals,
                        clientResponse -> clientResponse.bodyToMono(AuthException.class)
                )

                /*.onStatus(HttpStatusCode::is4xxClientError,
                        clientResponse -> clientResponse.bodyToMono(Map.class)
                                .flatMap(body -> {
                                    try {
                                        var exception = objectMapper.writeValueAsString(body);
                                        AuthException authException = objectMapper.readValue(exception, AuthException.class);
                                        return Mono.error(authException);
                                    } catch (JsonProcessingException jsonProcessingException) {
                                        return Mono.error(new ServerException(SERVER_ERROR));
                                    }
                                }))*/

                /*.onStatus(HttpStatusCode::is4xxClientError,
                        clientResponse -> Mono.error(
                                new RuntimeException(
                                        "Пользователь с логином - " + request.username() + ", не был найден.")))*/
                /*.onStatus(HttpStatusCode::is4xxClientError,
                        clientResponse -> {
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(body -> {
                                return
                            });
                        })*/
                .bodyToMono(AuthenticationResponse.class);

        /*AuthenticationResponse authenticationResponse = postResponse.block();
        assert authenticationResponse != null;
        return authenticationResponse;*/
        return postResponse.block();
    }
}
