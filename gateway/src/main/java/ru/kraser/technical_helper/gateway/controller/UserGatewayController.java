package ru.kraser.technical_helper.gateway.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.kraser.technical_helper.common_module.dto.user.CreateUserDto;
import ru.kraser.technical_helper.gateway.client.UserClient;

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
}
