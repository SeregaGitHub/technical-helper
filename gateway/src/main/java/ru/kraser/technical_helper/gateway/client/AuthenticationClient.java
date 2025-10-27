package ru.kraser.technical_helper.gateway.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.kraser.technical_helper.common_module.dto.auth.AuthenticationRequest;
import ru.kraser.technical_helper.common_module.exception.AuthException;
import ru.kraser.technical_helper.common_module.exception.ServerException;
import ru.kraser.technical_helper.common_module.model.User;

import static ru.kraser.technical_helper.common_module.util.Constant.*;

@Service
@RequiredArgsConstructor
public class AuthenticationClient {
    private final WebClient webClient;

    public User authenticate(AuthenticationRequest request) {

        Mono<User> postResponse = webClient
                .post()
                .uri(MAIN_SERVER_URL + BASE_URL + AUTH_URL)
                .contentType(MediaType.APPLICATION_JSON)

                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError,
                        clientResponse -> Mono.error(
                                new ServerException(SERVER_ERROR)))
                .onStatus(HttpStatus.UNAUTHORIZED::equals,
                        clientResponse -> clientResponse.bodyToMono(AuthException.class)
                )
                .bodyToMono(User.class);

        return postResponse.block();
    }
}
