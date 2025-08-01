package ru.kraser.technical_helper.main_server.service;

import ru.kraser.technical_helper.common_module.dto.api.ApiResponse;
import ru.kraser.technical_helper.common_module.dto.user.ChangeUserPasswordDto;
import ru.kraser.technical_helper.common_module.dto.user.CreateUserDto;
import ru.kraser.technical_helper.common_module.dto.user.UpdateUserDto;
import ru.kraser.technical_helper.common_module.dto.user.UserDto;

import java.util.List;

public interface UserService {

    ApiResponse createUser(CreateUserDto createUserDto);

    ApiResponse updateUser(String userId, UpdateUserDto updateUserDto);

    ApiResponse changeUserPassword(String userId, ChangeUserPasswordDto passwordDto);

    List<UserDto> getAllUsers();

    UserDto getUser(String userId);

    ApiResponse deleteUser(String userId);
}
