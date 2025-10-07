package ru.kraser.technical_helper.main_server.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.kraser.technical_helper.common_module.dto.api.ApiResponse;
import ru.kraser.technical_helper.common_module.dto.user.*;
import ru.kraser.technical_helper.common_module.model.User;
import ru.kraser.technical_helper.main_server.service.UserService;

import java.util.List;

import static ru.kraser.technical_helper.common_module.util.Constant.*;

@RestController
@RequestMapping(path = BASE_URL + ADMIN_URL + USER_URL)
@Slf4j
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse createUser(@RequestHeader (CURRENT_USER_ID_HEADER) String currentUserId,
                                  @RequestBody CreateUserDto createUserDto) {
        log.info("Creating User with name - {}", createUserDto.username());
        ApiResponse apiResponse = userService.createUser(createUserDto, currentUserId);
        log.info("User with name - {}, successfully created", createUserDto.username());
        return apiResponse;
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse updateUser(@RequestHeader (USER_ID_HEADER) String userId,
                                  @RequestHeader (CURRENT_USER_ID_HEADER) String currentUserId,
                                  @RequestBody UpdateUserDto updateUserDto) {
        log.info("Updating User with Id={}", userId);
        ApiResponse apiResponse =  userService.updateUser(userId, updateUserDto, currentUserId);
        log.info("User with Id={}, successfully updated", userId);
        return apiResponse;
    }

    @PatchMapping(path = PASSWORD_URL)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse changeUserPassword(@RequestHeader (USER_ID_HEADER) String userId,
                                          @RequestHeader (CURRENT_USER_ID_HEADER) String currentUserId,
                                          @RequestBody ChangeUserPasswordDto passwordDto) {
        log.info("Change password of User with Id={}", userId);
        ApiResponse apiResponse = userService.changeUserPassword(userId, passwordDto, currentUserId);
        log.info("Password User with Id={}, successfully changed", userId);
        return apiResponse;
    }

    @GetMapping(path = ALL_URL)
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getAllUsers() {
        log.info("Getting all Users");
        List<UserDto> users =  userService.getAllUsers();
        log.info("All Users received successfully");
        return users;
    }

    @GetMapping(path = BREAKAGE_URL)
    @ResponseStatus(HttpStatus.OK)
    public List<UserShortDto> getAdminAndTechnicianList() {
        log.info("Getting admin and technician list");
        List<UserShortDto> list = userService.getAdminAndTechnicianList();
        log.info("Admin and technician list received successfully");
        return list;
    }

    @GetMapping(path = CURRENT_URL)
    @ResponseStatus(HttpStatus.OK)
    public UserDto getUser(@RequestHeader (USER_ID_HEADER) String userId) {
        log.info("Getting User with Id={}", userId);
        UserDto user = userService.getUser(userId);
        log.info("User with Id={}, received successfully", userId);
        return user;
    }

    @GetMapping(path = "/{username}")
    @ResponseStatus(HttpStatus.OK)
    public User getUserByName(@PathVariable("username") String username) {
        log.info("Getting User with username - {}", username);
        User user = userService.getUserByName(username);
        log.info("User with username - {}, received successfully", username);
        return user;
    }

    @PatchMapping(path = DELETE_URL)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse deleteUser(@RequestHeader (CURRENT_USER_ID_HEADER) String currentUserId,
                                  @RequestHeader (USER_ID_HEADER) String userId) {
        log.info("Deleting User with Id={}", userId);
        ApiResponse apiResponse = userService.deleteUser(userId, currentUserId);
        log.info("User with Id={}, successfully deleted", userId);
        return apiResponse;
    }
}
