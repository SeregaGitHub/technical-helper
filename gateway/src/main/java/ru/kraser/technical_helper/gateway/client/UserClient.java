package ru.kraser.technical_helper.gateway.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.kraser.technical_helper.common_module.dto.api.ApiResponse;
import ru.kraser.technical_helper.common_module.dto.user.*;
import ru.kraser.technical_helper.common_module.model.User;

import java.util.List;

import static ru.kraser.technical_helper.common_module.util.Constant.*;

@Service
public class UserClient extends BaseClient {
    public UserClient(WebClient webClient) {
        super(webClient);
    }

    public ApiResponse createUser(CreateUserDto createUserDto) {
        return super.post(
                MAIN_SERVER_URL + BASE_URL + ADMIN_URL + USER_URL,
                createUserDto,
                ParameterizedTypeReference.forType(ApiResponse.class));
    }

    public ApiResponse updateUser(String entityHeaderName, String userId,
                             UpdateUserDto updateUserDto) {
        return super.patch(
                MAIN_SERVER_URL + BASE_URL + ADMIN_URL + USER_URL,
                updateUserDto,
                entityHeaderName,
                userId,
                ParameterizedTypeReference.forType(ApiResponse.class)
        );
    }

    public ApiResponse changeUserPassword(String entityHeaderName, String userId,
                                     ChangeUserPasswordDto changeUserPasswordDto) {
        return super.patch(
                MAIN_SERVER_URL + BASE_URL + ADMIN_URL + USER_URL + PASSWORD_URL,
                changeUserPasswordDto,
                entityHeaderName,
                userId,
                ParameterizedTypeReference.forType(ApiResponse.class)
        );
    }

    public List<UserDto> getAllUsers() {
        return super.getAll(
                MAIN_SERVER_URL + BASE_URL + ADMIN_URL + USER_URL + ALL_URL,
                ParameterizedTypeReference.forType(UserDto.class)
        );
    }

    public List<UserShortDto> getAdminAndTechnicianList() {
        return super.getAll(
                MAIN_SERVER_URL + BASE_URL + ADMIN_URL + USER_URL + BREAKAGE_URL,
                ParameterizedTypeReference.forType(UserShortDto.class)
        );
    }

    public UserDto getUser(String userId, String userHeaderName) {
        return super.get(
                MAIN_SERVER_URL + BASE_URL + ADMIN_URL + USER_URL + CURRENT_URL,
                userHeaderName,
                userId,
                ParameterizedTypeReference.forType(UserDto.class)
        );
    }

    public User getUserByName(String username) {
        return super.get(
                MAIN_SERVER_URL + BASE_URL + ADMIN_URL + USER_URL + "/" + username,
                ParameterizedTypeReference.forType(User.class)
        );
    }

    public ApiResponse deleteUser(String userHeaderName, String userId, String jwt) {
        return super.delete(
                MAIN_SERVER_URL + BASE_URL + ADMIN_URL + USER_URL + DELETE_URL,
                jwt,
                userHeaderName,
                userId,
                ParameterizedTypeReference.forType(ApiResponse.class)
        );
    }
}
