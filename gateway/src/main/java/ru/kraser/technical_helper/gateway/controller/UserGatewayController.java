package ru.kraser.technical_helper.gateway.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.kraser.technical_helper.common_module.dto.user.ChangeUserPasswordDto;
import ru.kraser.technical_helper.common_module.dto.user.CreateUserDto;
import ru.kraser.technical_helper.common_module.dto.user.UpdateUserDto;
import ru.kraser.technical_helper.common_module.dto.user.UserDto;
import ru.kraser.technical_helper.gateway.client.UserClient;

import java.util.List;

import static ru.kraser.technical_helper.common_module.util.Constant.*;

@RestController
@RequestMapping(path = BASE_URL + ADMIN_URL + USER_URL)
@RequiredArgsConstructor
@Validated
public class UserGatewayController {
    private final UserClient userClient;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String createUser(@Validated() @RequestBody CreateUserDto createUserDto,
                              @RequestHeader(AUTHORIZATION) String jwt) {
        String response = userClient.createUser(createUserDto, jwt);

        return response;
    }

    @PatchMapping()
    @ResponseStatus(HttpStatus.OK)
    public String updateUser(@Validated() @RequestBody UpdateUserDto updateUserDto,
                             @RequestHeader(AUTHORIZATION) String jwt,
                             @RequestHeader(USER_ID_HEADER) String userId) {
        String response = userClient.updateUser(USER_ID_HEADER, userId, updateUserDto, jwt);

        return response;
    }

    @PatchMapping(path = PASSWORD_URL)
    @ResponseStatus(HttpStatus.OK)
    public String changeUserPassword(@Validated() @RequestBody ChangeUserPasswordDto changeUserPasswordDto,
                                     @RequestHeader(AUTHORIZATION) String jwt,
                                     @RequestHeader(USER_ID_HEADER) String userId) {
        String response = userClient.changeUserPassword(USER_ID_HEADER, userId, changeUserPasswordDto, jwt);

        return response;
    }

    @GetMapping(path = ALL_URL)
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getAllUsers(@RequestHeader(AUTHORIZATION) String jwt) {
        List<UserDto> userDtoList = userClient.getAllUsers(jwt);

        return userDtoList;
    }

    @GetMapping(path = CURRENT_URL)
    @ResponseStatus(HttpStatus.OK)
    public UserDto getUser(@RequestHeader(AUTHORIZATION) String jwt,
                           @RequestHeader(USER_ID_HEADER) String userId) {
        UserDto userDto = userClient.getUser(userId, jwt, USER_ID_HEADER);

        return userDto;
    }
}
