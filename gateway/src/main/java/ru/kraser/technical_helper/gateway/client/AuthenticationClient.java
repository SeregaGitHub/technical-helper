package ru.kraser.technical_helper.gateway.client;

//import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.kraser.technical_helper.common_module.dto.auth.AuthenticationRequest;
//import ru.kraser.technical_helper.common_module.dto.auth.AuthenticationResponse;
import ru.kraser.technical_helper.common_module.exception.AuthException;
import ru.kraser.technical_helper.common_module.exception.ServerException;
import ru.kraser.technical_helper.common_module.model.User;

import static ru.kraser.technical_helper.common_module.util.Constant.*;

@Service
@RequiredArgsConstructor
public class AuthenticationClient {
    private final WebClient webClient;
    //private final ObjectMapper objectMapper;

    public User authenticate(AuthenticationRequest request) {

        Mono<User> postResponse = webClient
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
                                        return Mono.httpStatus(
                                                new AuthException(errorResponse.message()
                                                        *//*clientResponse.statusCode().value(),
                                                        "My custom httpStatus message", errorResponse*//*));
                                    } catch (JsonProcessingException jsonProcessingException) {
                                        return Mono.httpStatus(new ServerException(SERVER_ERROR));
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
                                        return Mono.httpStatus(authException);
                                    } catch (JsonProcessingException jsonProcessingException) {
                                        return Mono.httpStatus(new ServerException(SERVER_ERROR));
                                    }
                                }))*/

                /*.onStatus(HttpStatusCode::is4xxClientError,
                        clientResponse -> Mono.httpStatus(
                                new RuntimeException(
                                        "Пользователь с логином - " + request.username() + ", не был найден.")))*/
                /*.onStatus(HttpStatusCode::is4xxClientError,
                        clientResponse -> {
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(body -> {
                                return
                            });
                        })*/
                .bodyToMono(User.class);
                //.bodyToMono(AuthenticationResponse.class);

        /*AuthenticationResponse authenticationResponse = postResponse.block();
        assert authenticationResponse != null;
        return authenticationResponse;*/
        return postResponse.block();
    }
}
