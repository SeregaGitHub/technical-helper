package ru.kraser.technical_helper.main_server.service;

import ru.kraser.technical_helper.common_module.dto.user.CreateUserDto;
import ru.kraser.technical_helper.common_module.dto.user.UserDto;

public interface UserService {

    UserDto createUser(CreateUserDto createUserDto);
}
