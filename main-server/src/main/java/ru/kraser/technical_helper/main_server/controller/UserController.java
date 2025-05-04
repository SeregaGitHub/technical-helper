package ru.kraser.technical_helper.main_server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.kraser.technical_helper.common_module.dto.user.CreateUserDto;
import ru.kraser.technical_helper.common_module.dto.user.UpdateUserDto;
import ru.kraser.technical_helper.common_module.dto.user.UserDto;
import ru.kraser.technical_helper.main_server.service.UserService;

import java.util.List;

import static ru.kraser.technical_helper.common_module.util.Constant.ADMIN_URL;
import static ru.kraser.technical_helper.common_module.util.Constant.BASE_URL;

@RestController
@RequestMapping(path = BASE_URL + ADMIN_URL + "/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String createUser(@RequestBody CreateUserDto createUserDto) {
        return userService.createUser(createUserDto);
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.OK)
    public String updateUser(@RequestHeader ("X-TH-User-Id") String userId,
                             @RequestBody UpdateUserDto updateUserDto) {
        return userService.updateUser(userId, updateUserDto);
    }

    @GetMapping(path = "/all")
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers();
    }

    /*@GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public UserDto getUser(@RequestHeader ("X-TH-User-Id") String userId) {
        return userService.getUser(userId);
    }*/
}
