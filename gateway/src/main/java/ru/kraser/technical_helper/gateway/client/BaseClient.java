package ru.kraser.technical_helper.gateway.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.kraser.technical_helper.common_module.exception.AlreadyExistsException;
import ru.kraser.technical_helper.common_module.exception.NotFoundException;
import ru.kraser.technical_helper.common_module.exception.ServerException;

import java.util.List;

import static ru.kraser.technical_helper.common_module.util.Constant.*;

public abstract class BaseClient {
    protected final WebClient webClient;

    public BaseClient(WebClient webClient) {
        this.webClient = webClient;
    }

    protected <T, O> T post(String url, O obj, String jwt, ParameterizedTypeReference<T> typeReference) {
        Mono<T> entityMono = webClient
                .post()
                .uri(url)
                .header(AUTHORIZATION, jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(obj)
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

    protected <T, O> T patch(String url, O obj, String jwt, String entityHeaderName,
                             String entityId, ParameterizedTypeReference<T> typeReference) {
        Mono<T> patchResponse = webClient
                .patch()
                .uri(url)
                .header(AUTHORIZATION, jwt)
                .header(entityHeaderName, entityId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(obj)
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

        return patchResponse.block();
    }

    protected <T> List<T> getAll(String url, String jwt, ParameterizedTypeReference<T> typeReference) {
        Mono<List<T>> getResponse = webClient
                .get()
                .uri(url)
                .header(AUTHORIZATION, jwt)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError,
                        clientResponse -> Mono.error(new ServerException(SERVER_ERROR)))
                .bodyToFlux(typeReference)
                .collectList();

        return getResponse.block();
    }

    protected <T> T get(String url, String jwt, String entityHeaderName,
                        String entityId, ParameterizedTypeReference<T> typeReference) {
        Mono<T> getResponse = webClient
                .get()
                .uri(url)
                .header(AUTHORIZATION, jwt)
                .header(entityHeaderName, entityId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError,
                        clientResponse -> Mono.error(
                                new ServerException(SERVER_ERROR)))
                .onStatus(HttpStatus.NOT_FOUND::equals,
                        clientResponse -> clientResponse.bodyToMono(NotFoundException.class)
                )
                .bodyToMono(typeReference);

        return getResponse.block();
    }
}
