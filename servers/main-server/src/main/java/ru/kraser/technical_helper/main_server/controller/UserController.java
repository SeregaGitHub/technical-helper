package ru.kraser.technical_helper.main_server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.kraser.technical_helper.common_module.dto.api.ApiResponse;
import ru.kraser.technical_helper.common_module.dto.user.ChangeUserPasswordDto;
import ru.kraser.technical_helper.common_module.dto.user.CreateUserDto;
import ru.kraser.technical_helper.common_module.dto.user.UpdateUserDto;
import ru.kraser.technical_helper.common_module.dto.user.UserDto;
import ru.kraser.technical_helper.main_server.service.UserService;

import java.util.List;

import static ru.kraser.technical_helper.common_module.util.Constant.*;

@RestController
@RequestMapping(path = BASE_URL + ADMIN_URL + USER_URL)
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse createUser(@RequestBody CreateUserDto createUserDto) {
        return userService.createUser(createUserDto);
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse updateUser(@RequestHeader (USER_ID_HEADER) String userId,
                             @RequestBody UpdateUserDto updateUserDto) {
        return userService.updateUser(userId, updateUserDto);
    }

    @PatchMapping(path = PASSWORD_URL)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse changeUserPassword(@RequestHeader (USER_ID_HEADER) String userId,
                                     @RequestBody ChangeUserPasswordDto passwordDto) {
        return userService.changeUserPassword(userId, passwordDto);
    }

    @GetMapping(path = ALL_URL)
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping(path = CURRENT_URL)
    @ResponseStatus(HttpStatus.OK)
    public UserDto getUser(@RequestHeader (USER_ID_HEADER) String userId) {
        return userService.getUser(userId);
    }

    @PatchMapping(path = DELETE_URL)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse deleteUser(@RequestHeader (USER_ID_HEADER) String userId) {
        return userService.deleteUser(userId);
    }
}
