package ru.kraser.technical_helper.main_server.service;

import ru.kraser.technical_helper.common_module.dto.api.ApiResponse;
import ru.kraser.technical_helper.common_module.dto.user.*;
import ru.kraser.technical_helper.common_module.model.User;

import java.util.List;

public interface UserService {

    ApiResponse createUser(CreateUserDto createUserDto, String currentUserId);

    ApiResponse updateUser(String userId, UpdateUserDto updateUserDto);

    ApiResponse changeUserPassword(String userId, ChangeUserPasswordDto passwordDto, String currentUserId);

    List<UserDto> getAllUsers();

    List<UserShortDto> getAdminAndTechnicianList();

    UserDto getUser(String userId);

    User getUserByName(String username);

    ApiResponse deleteUser(String userId);
}
