package ru.kraser.technical_helper.main_server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.kraser.technical_helper.common_module.dto.user.CreateUserDto;
import ru.kraser.technical_helper.main_server.service.UserService;

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
}
