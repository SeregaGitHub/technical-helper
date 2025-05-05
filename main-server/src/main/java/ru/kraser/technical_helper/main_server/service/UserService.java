package ru.kraser.technical_helper.main_server.service;

import ru.kraser.technical_helper.common_module.dto.user.ChangeUserPasswordDto;
import ru.kraser.technical_helper.common_module.dto.user.CreateUserDto;
import ru.kraser.technical_helper.common_module.dto.user.UpdateUserDto;
import ru.kraser.technical_helper.common_module.dto.user.UserDto;

import java.util.List;

public interface UserService {

    String createUser(CreateUserDto createUserDto);

    String updateUser(String userId, UpdateUserDto updateUserDto);

    String changeUserPassword(String userId, ChangeUserPasswordDto passwordDto);

    List<UserDto> getAllUsers();

    UserDto getUser(String userId);

    String deleteUser(String userId);
}
