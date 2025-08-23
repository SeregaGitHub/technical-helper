package ru.kraser.technical_helper.gateway.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.kraser.technical_helper.common_module.dto.api.ApiResponse;
import ru.kraser.technical_helper.common_module.dto.user.ChangeUserPasswordDto;
import ru.kraser.technical_helper.common_module.dto.user.CreateUserDto;
import ru.kraser.technical_helper.common_module.dto.user.UpdateUserDto;
import ru.kraser.technical_helper.common_module.dto.user.UserDto;
import ru.kraser.technical_helper.gateway.client.UserClient;

import java.util.List;

import static ru.kraser.technical_helper.common_module.util.Constant.*;

@CrossOrigin(origins = FRONT_URL)
@RestController
@RequestMapping(path = BASE_URL + ADMIN_URL + USER_URL)
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserGatewayController {
    private final UserClient userClient;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse createUser(@Validated() @RequestBody CreateUserDto createUserDto,
                                  @RequestHeader(AUTH_HEADER) String jwt) {
        log.info("Creating User with name - {}", createUserDto.username());
        ApiResponse response = userClient.createUser(createUserDto, jwt);
        log.info("User with name - {}, successfully created", createUserDto.username());
        return response;
    }

    @PatchMapping()
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse updateUser(@Validated() @RequestBody UpdateUserDto updateUserDto,
                                  @RequestHeader(AUTH_HEADER) String jwt,
                                  @RequestHeader(USER_ID_HEADER) String userId) {
        log.info("Updating User with Id={}", userId);
        ApiResponse response = userClient.updateUser(USER_ID_HEADER, userId, updateUserDto, jwt);
        log.info("User with Id={}, successfully updated", userId);
        return response;
    }

    @PatchMapping(path = PASSWORD_URL)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse changeUserPassword(@Validated() @RequestBody ChangeUserPasswordDto changeUserPasswordDto,
                                          @RequestHeader(AUTH_HEADER) String jwt,
                                          @RequestHeader(USER_ID_HEADER) String userId) {
        log.info("Change password of User with Id={}", userId);
        ApiResponse response = userClient.changeUserPassword(USER_ID_HEADER, userId, changeUserPasswordDto, jwt);
        log.info("Password User with Id={}, successfully changed", userId);
        return response;
    }

    @GetMapping(path = ALL_URL)
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getAllUsers(@RequestHeader(AUTH_HEADER) String jwt) {
        log.info("Getting all Users");
        List<UserDto> userDtoList = userClient.getAllUsers(jwt);
        log.info("All Users received successfully");
        return userDtoList;
    }

    @GetMapping(path = CURRENT_URL)
    @ResponseStatus(HttpStatus.OK)
    public UserDto getUser(@RequestHeader(AUTH_HEADER) String jwt,
                           @RequestHeader(USER_ID_HEADER) String userId) {
        log.info("Getting User with Id={}", userId);
        UserDto userDto = userClient.getUser(userId, jwt, USER_ID_HEADER);
        log.info("User with Id={}, received successfully", userId);
        return userDto;
    }

    @PatchMapping(path = DELETE_URL)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse deleteUser(@RequestHeader(AUTH_HEADER) String jwt,
                                  @RequestHeader(USER_ID_HEADER) String userId) {
        log.info("Deleting User with Id={}", userId);
        ApiResponse response = userClient.deleteUser(USER_ID_HEADER, userId, jwt);
        log.info("User with Id={}, successfully deleted", userId);
        return response;
    }
}
