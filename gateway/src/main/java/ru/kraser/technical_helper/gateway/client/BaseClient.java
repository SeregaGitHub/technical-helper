package ru.kraser.technical_helper.gateway.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.kraser.technical_helper.common_module.enums.Role;
import ru.kraser.technical_helper.common_module.exception.AlreadyExistsException;
import ru.kraser.technical_helper.common_module.exception.NotCorrectParameter;
import ru.kraser.technical_helper.common_module.exception.NotFoundException;
import ru.kraser.technical_helper.common_module.exception.ServerException;
import ru.kraser.technical_helper.common_module.util.SecurityUtil;

import java.util.List;

import static ru.kraser.technical_helper.common_module.util.Constant.*;

public abstract class BaseClient {
    protected final WebClient webClient;

    public BaseClient(WebClient webClient) {
        this.webClient = webClient;
    }

    protected <T, O> T post(String url, O obj, ParameterizedTypeReference<T> typeReference) {
        Mono<T> entityMono = webClient
                .post()
                .uri(url)
                .header(CURRENT_USER_ID_HEADER, SecurityUtil.getCurrentUserId())
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

    // NEED FOR DELETE !!!
    /*protected <T, O> T post(String url, O obj, String jwt, ParameterizedTypeReference<T> typeReference) {
        Mono<T> entityMono = webClient
                .post()
                .uri(url)
                .header(AUTH_HEADER, jwt)
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
    }*/

    protected <T, O> T post(String url, O obj, String entityHeaderName, String entityId,
                            ParameterizedTypeReference<T> typeReference) {
        Mono<T> entityMono = webClient
                .post()
                .uri(url)
                .header(entityHeaderName, entityId)
                //.header(AUTH_HEADER, jwt)
                .header(CURRENT_USER_ID_HEADER, SecurityUtil.getCurrentUserId())
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

    protected <T, O> T patch(String url, O obj, String entityHeaderName,
                             String entityId, ParameterizedTypeReference<T> typeReference) {
        Mono<T> patchResponse = webClient
                .patch()
                .uri(url)
                .header(CURRENT_USER_ID_HEADER, SecurityUtil.getCurrentUserId())
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
                .onStatus(HttpStatus.BAD_REQUEST::equals,
                        clientResponse -> clientResponse.bodyToMono(NotCorrectParameter.class)
                )
                .bodyToMono(typeReference);

        return patchResponse.block();
    }

    // NEED FOR DELETE
    protected <T, O> T patch(String url, O obj, String jwt, String entityHeaderName,
                             String entityId, ParameterizedTypeReference<T> typeReference) {
        Mono<T> patchResponse = webClient
                .patch()
                .uri(url)
                .header(AUTH_HEADER, jwt)
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
                .onStatus(HttpStatus.BAD_REQUEST::equals,
                        clientResponse -> clientResponse.bodyToMono(NotCorrectParameter.class)
                )
                .bodyToMono(typeReference);

        return patchResponse.block();
    }

    protected <T> T patch(String url, String entityHeaderName1, String entityId1,
                          String entityHeaderName2, String entityId2,
                          String entityHeaderName3, String entityId3,
                          String entityHeaderName4, String entityId4,
                          ParameterizedTypeReference<T> typeReference) {
        Mono<T> patchResponse = webClient
                .patch()
                .uri(url)
                .header(CURRENT_USER_ID_HEADER, SecurityUtil.getCurrentUserId())
                .header(entityHeaderName1, entityId1)
                .header(entityHeaderName2, entityId2)
                .header(entityHeaderName3, entityId3)
                .header(entityHeaderName4, entityId4)
                .contentType(MediaType.APPLICATION_JSON)
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

    // NEED FOR DELETE
    protected <T, O> T patch(String url, String jwt, String entityHeaderName1, String entityId1,
                             String entityHeaderName2, String entityId2, ParameterizedTypeReference<T> typeReference) {
        Mono<T> patchResponse = webClient
                .patch()
                .uri(url)
                .header(AUTH_HEADER, jwt)
                .header(entityHeaderName1, entityId1)
                .header(entityHeaderName2, entityId2)
                .contentType(MediaType.APPLICATION_JSON)
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

    protected <T> List<T> getAll(String url, ParameterizedTypeReference<T> typeReference) {
        Mono<List<T>> getResponse = webClient
                .get()
                .uri(url)
                //.header(AUTH_HEADER, jwt)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError,
                        clientResponse -> Mono.error(new ServerException(SERVER_ERROR)))
                .bodyToFlux(typeReference)
                .collectList();

        return getResponse.block();
    }

    // NEED FOR DELETE
    protected <T> List<T> getAll(String url, String jwt, ParameterizedTypeReference<T> typeReference) {
        Mono<List<T>> getResponse = webClient
                .get()
                .uri(url)
                .header(AUTH_HEADER, jwt)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError,
                        clientResponse -> Mono.error(new ServerException(SERVER_ERROR)))
                .bodyToFlux(typeReference)
                .collectList();

        return getResponse.block();
    }

    protected <T> T getAllByPage(String url, Integer pageSize, Integer pageIndex, String sortBy, String direction,
                                 boolean statusNew, boolean statusSolved, boolean statusInProgress,
                                 boolean statusPaused, boolean statusRedirected, boolean statusCancelled,
                                 boolean priorityUrgently, boolean priorityHigh,
                                 boolean priorityMedium, boolean priorityLow, String executor, boolean deadline,
                                 String searchText, Role role, String currentUserDepartmentId,
                                 String currentUserId, ParameterizedTypeReference<T> typeReference) {
        Mono<T> getResponse = webClient
                .get()
                .uri(url, uriBuilder ->
                        uriBuilder.queryParam("pageSize", pageSize)
                                .queryParam("pageIndex", pageIndex)
                                .queryParam("sortBy", sortBy)
                                .queryParam("direction", direction)
                                .queryParam("statusNew", statusNew)
                                .queryParam("statusSolved", statusSolved)
                                .queryParam("statusInProgress", statusInProgress)
                                .queryParam("statusPaused", statusPaused)
                                .queryParam("statusRedirected", statusRedirected)
                                .queryParam("statusCancelled", statusCancelled)
                                .queryParam("priorityUrgently", priorityUrgently)
                                .queryParam("priorityHigh", priorityHigh)
                                .queryParam("priorityMedium", priorityMedium)
                                .queryParam("priorityLow", priorityLow)
                                .queryParam("breakageExecutor", executor)
                                .queryParam("deadline", deadline)
                                .queryParam("searchText", searchText)
                                .build())
                //.header(AUTH_HEADER, jwt)
                .header(USER_ROLE_HEADER, role.toString())
                .header(USER_DEPARTMENT_ID_HEADER, currentUserDepartmentId)
                .header(CURRENT_USER_ID_HEADER, currentUserId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError,
                        clientResponse -> Mono.error(new ServerException(SERVER_ERROR)))
                .bodyToMono(typeReference);

        return getResponse.block();
    }

    /*protected <T> T getAllByText(String url, String jwt, Integer pageIndex, Integer pageSize,
                                 String sortBy, String direction, ParameterizedTypeReference<T> typeReference) {
        Mono<T> getResponse = webClient
                .get()
                .uri(url, uriBuilder ->
                        uriBuilder.queryParam("pageSize", pageSize)
                                .queryParam("pageIndex", pageIndex)
                                .queryParam("sortBy", sortBy)
                                .queryParam("direction", direction)
                                .build())
                .header(AUTH_HEADER, jwt)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError,
                        clientResponse -> Mono.error(new ServerException(SERVER_ERROR)))
                .bodyToMono(typeReference);

        return getResponse.block();
    }*/

    protected <T> T get(String url, String entityHeaderName,
                        String entityId, ParameterizedTypeReference<T> typeReference) {
        Mono<T> getResponse = webClient
                .get()
                .uri(url)
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

    // New getEmployeeBreakage
    protected <T> T get(String url, String entityHeaderName1, String entityId1,
                        String entityHeaderName2, String entityId2, ParameterizedTypeReference<T> typeReference) {
        Mono<T> getResponse = webClient
                .get()
                .uri(url)
                .header(entityHeaderName1, entityId1)
                .header(entityHeaderName2, entityId2)
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

    protected <T> T get(String url, ParameterizedTypeReference<T> typeReference) {
        Mono<T> getResponse = webClient
                .get()
                .uri(url)
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

    protected <T> T delete(String url, String entityHeaderName,
                              String entityId, ParameterizedTypeReference<T> typeReference) {
        Mono<T> deleteResponse = webClient
                .patch()
                .uri(url)
                .header(CURRENT_USER_ID_HEADER, SecurityUtil.getCurrentUserId())
                .header(entityHeaderName, entityId)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError,
                        clientResponse -> Mono.error(new ServerException(SERVER_ERROR)))
                .onStatus(HttpStatus.NOT_FOUND::equals,
                        clientResponse -> clientResponse.bodyToMono(NotFoundException.class)
                )
                .bodyToMono(typeReference);

        return deleteResponse.block();
    }

    // NEED FOR DELETE
    protected <T> T delete(String url, String jwt, String entityHeaderName,
                           String entityId, ParameterizedTypeReference<T> typeReference) {
        Mono<T> deleteResponse = webClient
                .patch()
                .uri(url)
                .header(AUTH_HEADER, jwt)
                .header(entityHeaderName, entityId)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError,
                        clientResponse -> Mono.error(new ServerException(SERVER_ERROR)))
                .onStatus(HttpStatus.NOT_FOUND::equals,
                        clientResponse -> clientResponse.bodyToMono(NotFoundException.class)
                )
                .bodyToMono(typeReference);

        return deleteResponse.block();
    }

    protected <T> T hardDelete(String url, String jwt, String entityHeaderName,
                           String entityId, ParameterizedTypeReference<T> typeReference) {
        Mono<T> deleteResponse = webClient
                .delete()
                .uri(url)
                .header(AUTH_HEADER, jwt)
                .header(entityHeaderName, entityId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError,
                        clientResponse -> Mono.error(new ServerException(SERVER_ERROR)))
                .onStatus(HttpStatus.NOT_FOUND::equals,
                        clientResponse -> clientResponse.bodyToMono(NotFoundException.class)
                )
                .bodyToMono(typeReference);

        return deleteResponse.block();
    }
}
