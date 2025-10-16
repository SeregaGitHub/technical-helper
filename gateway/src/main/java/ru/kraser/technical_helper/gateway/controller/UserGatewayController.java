package ru.kraser.technical_helper.gateway.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.kraser.technical_helper.common_module.dto.api.ApiResponse;
import ru.kraser.technical_helper.common_module.dto.user.*;
import ru.kraser.technical_helper.gateway.client.UserClient;
import ru.kraser.technical_helper.gateway.util.user_util.UserUtil;

import java.util.List;

import static ru.kraser.technical_helper.common_module.util.Constant.*;

//@CrossOrigin(origins = FRONT_URL)
@RestController
@RequestMapping(path = BASE_URL)
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserGatewayController {
    private final UserClient userClient;
    private final PasswordEncoder passwordEncoder;

    @Value("${default.admin.password}")
    private String defaultAdminPassword;

    @PostMapping(path = ADMIN_URL + USER_URL)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse createUser(@Validated() @RequestBody CreateUserDto createUserDto) {
        log.info("Creating User with name - {}", createUserDto.username());
        ApiResponse response = userClient.createUser(
                UserUtil.hashCreateUserDtoPassword(createUserDto, passwordEncoder));
        log.info("User with name - {}, successfully created", createUserDto.username());
        return response;
    }

    @PatchMapping(path = ADMIN_URL + USER_URL)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse updateUser(@Validated() @RequestBody UpdateUserDto updateUserDto,
                                  @RequestHeader(USER_ID_HEADER) String userId) {
        log.info("Updating User with Id={}", userId);
        ApiResponse response = userClient.updateUser(USER_ID_HEADER, userId, updateUserDto);
        log.info("User with Id={}, successfully updated", userId);
        return response;
    }

    @PatchMapping(path = ADMIN_URL + USER_URL + PASSWORD_URL)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse changeUserPassword(@Validated() @RequestBody UserPasswordDto userPasswordDto,
                                          @RequestHeader(USER_ID_HEADER) String userId) {
        log.info("Change password of User with Id={}", userId);
        ApiResponse response = userClient.changeUserPassword(USER_ID_HEADER, userId,
                UserUtil.hashUserPasswordDto(userPasswordDto, passwordEncoder));
        log.info("Password User with Id={}, successfully changed", userId);
        return response;
    }

    @GetMapping(path = ADMIN_URL + USER_URL + ALL_URL)
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getAllUsers() {
        log.info("Getting all Users");
        List<UserDto> userDtoList = userClient.getAllUsers();
        log.info("All Users received successfully");
        return userDtoList;
    }

    @GetMapping(path = ADMIN_URL + USER_URL + BREAKAGE_URL)
    @ResponseStatus(HttpStatus.OK)
    public List<UserShortDto> getAdminAndTechnicianList() {
        log.info("Getting admin and technician list");
        List<UserShortDto> list = userClient.getAdminAndTechnicianList();
        log.info("Admin and technician list received successfully");
        return list;
    }

    @GetMapping(path = ADMIN_URL + USER_URL + CURRENT_URL)
    @ResponseStatus(HttpStatus.OK)
    public UserDto getUser(@RequestHeader(USER_ID_HEADER) String userId) {
        log.info("Getting User with Id={}", userId);
        UserDto userDto = userClient.getUser(userId, USER_ID_HEADER);
        log.info("User with Id={}, received successfully", userId);
        return userDto;
    }

    @PatchMapping(path = ADMIN_URL + USER_URL + DELETE_URL)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse deleteUser(@RequestHeader(USER_ID_HEADER) String userId) {
        log.info("Deleting User with Id={}", userId);
        ApiResponse response = userClient.deleteUser(USER_ID_HEADER, userId);
        log.info("User with Id={}, successfully deleted", userId);
        return response;
    }

    @PostMapping(path = DEFAULT_URL)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse createDefaultAdmin() {
        log.info("Creating User with name - admin");
        UserPasswordDto userPasswordDto = new UserPasswordDto(defaultAdminPassword);
        ApiResponse response = userClient.createDefaultAdmin(
                UserUtil.hashUserPasswordDto(userPasswordDto, passwordEncoder));
        log.info("User with name - admin, successfully created");
        return response;
    }
}
