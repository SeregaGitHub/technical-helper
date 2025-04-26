package ru.kraser.technical_helper.main_server.service;

import ru.kraser.technical_helper.common_module.dto.user.CreateUserDto;

public interface UserService {

    String createUser(CreateUserDto createUserDto);

}
