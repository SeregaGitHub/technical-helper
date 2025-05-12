package ru.kraser.technical_helper.gateway.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.kraser.technical_helper.common_module.dto.auth.AuthenticationResponse;
import ru.kraser.technical_helper.common_module.exception.AlreadyExistsException;
import ru.kraser.technical_helper.common_module.exception.AuthException;
import ru.kraser.technical_helper.common_module.exception.NotFoundException;
import ru.kraser.technical_helper.common_module.exception.ServerException;

import static ru.kraser.technical_helper.common_module.util.Constant.AUTHORIZATION;
import static ru.kraser.technical_helper.common_module.util.Constant.SERVER_ERROR;

public abstract class BaseClient {
    protected final WebClient webClient;

    public BaseClient(WebClient webClient) {
        this.webClient = webClient;
    }

    protected <T, O> T post(String url, O obj, String jwt/*, String message*/, ParameterizedTypeReference<T> typeReference) {
        Mono<T> entityMono = webClient
                .post()
                .uri(url)
                .header(AUTHORIZATION, jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(obj)
                /*.exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.CREATED)) {
                        return response.bodyToMono(typeReference);
                    }
                    else {
                        return response.createError();
                    }
                });*/
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError,
                        clientResponse -> Mono.error(
                                new ServerException(SERVER_ERROR)))
                .onStatus(HttpStatus.UNPROCESSABLE_ENTITY::equals,
                        clientResponse -> clientResponse.bodyToMono(AlreadyExistsException.class)
                )
                .onStatus(HttpStatus.NOT_FOUND::equals,
                        clientResponse -> clientResponse.bodyToMono(NotFoundException.class)
                )
                .bodyToMono(typeReference);
        return entityMono.block();
    }
}
