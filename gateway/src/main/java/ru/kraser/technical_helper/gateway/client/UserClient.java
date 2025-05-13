package ru.kraser.technical_helper.gateway.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.kraser.technical_helper.common_module.dto.user.ChangeUserPasswordDto;
import ru.kraser.technical_helper.common_module.dto.user.CreateUserDto;
import ru.kraser.technical_helper.common_module.dto.user.UpdateUserDto;
import ru.kraser.technical_helper.common_module.dto.user.UserDto;

import java.util.List;

import static ru.kraser.technical_helper.common_module.util.Constant.*;

@Service
public class UserClient extends BaseClient {
    public UserClient(WebClient webClient) {
        super(webClient);
    }

    public String createUser(CreateUserDto createUserDto, String jwt) {
        return super.post(
                MAIN_SERVER_URL + BASE_URL + ADMIN_URL + USER_URL,
                createUserDto,
                jwt,
                ParameterizedTypeReference.forType(String.class));
    }

    public String updateUser(String entityHeaderName, String userId,
                             UpdateUserDto updateUserDto, String jwt) {
        return super.patch(
                MAIN_SERVER_URL + BASE_URL + ADMIN_URL + USER_URL,
                updateUserDto,
                jwt,
                entityHeaderName,
                userId,
                ParameterizedTypeReference.forType(String.class)
        );
    }

    public String changeUserPassword(String entityHeaderName, String userId,
                                     ChangeUserPasswordDto changeUserPasswordDto, String jwt) {
        return super.patch(
                MAIN_SERVER_URL + BASE_URL + ADMIN_URL + USER_URL + PASSWORD_URL,
                changeUserPasswordDto,
                jwt,
                entityHeaderName,
                userId,
                ParameterizedTypeReference.forType(String.class)
        );
    }

    public List<UserDto> getAllUsers(String jwt) {
        return super.getAll(
                MAIN_SERVER_URL + BASE_URL + ADMIN_URL + USER_URL + ALL_URL,
                jwt,
                ParameterizedTypeReference.forType(UserDto.class)
        );
    }

    public UserDto getUser(String userId, String jwt, String userHeaderName) {
        return super.get(
                MAIN_SERVER_URL + BASE_URL + ADMIN_URL + USER_URL + CURRENT_URL,
                jwt,
                userHeaderName,
                userId,
                ParameterizedTypeReference.forType(UserDto.class)
        );
    }
}
