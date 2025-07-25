package ru.kraser.technical_helper.main_server.service.service_impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kraser.technical_helper.common_module.dto.api.ApiResponse;
import ru.kraser.technical_helper.common_module.dto.user.ChangeUserPasswordDto;
import ru.kraser.technical_helper.common_module.dto.user.CreateUserDto;
import ru.kraser.technical_helper.common_module.dto.user.UpdateUserDto;
import ru.kraser.technical_helper.common_module.dto.user.UserDto;
import ru.kraser.technical_helper.common_module.exception.NotFoundException;
import ru.kraser.technical_helper.main_server.model.Department;
import ru.kraser.technical_helper.main_server.model.User;
import ru.kraser.technical_helper.main_server.repository.DepartmentRepository;
import ru.kraser.technical_helper.main_server.repository.UserRepository;
import ru.kraser.technical_helper.main_server.security.SecurityUtil;
import ru.kraser.technical_helper.main_server.service.UserService;
import ru.kraser.technical_helper.main_server.util.error_handler.ThrowException;
import ru.kraser.technical_helper.main_server.util.mapper.UserMapper;

import java.time.LocalDateTime;
import java.util.List;

import static ru.kraser.technical_helper.common_module.util.Constant.USER_NOT_EXIST;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public ApiResponse createUser(CreateUserDto createUserDto) {
        Department department = departmentRepository.getReferenceById(createUserDto.departmentId());
        try {
            userRepository.saveAndFlush(UserMapper.toUser(createUserDto, department, passwordEncoder));
        } catch (Exception e) {
            ThrowException.userHandler(e.getMessage(), createUserDto.username());
        }
        return ApiResponse.builder()
                .message("Сотрудник: " + createUserDto.username() + ", - был успешно создан.")
                .status(201)
                .httpStatus(HttpStatus.CREATED)
                .timestamp(LocalDateTime.now().withNano(0))
                .build();
    }

    @Override
    @Transactional
    public ApiResponse updateUser(String userId, UpdateUserDto updateUserDto) {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        int response;
        try {
            response = userRepository.updateUser(
                    userId,
                    updateUserDto.username(),
                    updateUserDto.departmentId(),
                    updateUserDto.role(),
                    SecurityUtil.getCurrentUserId(),
                    now);
            ThrowException.isExist(response, "пользователь");
        } catch (Exception e) {
            ThrowException.userHandler(e.getMessage(), updateUserDto.username());
        }
        return ApiResponse.builder()
                .message("Сотрудник: " + updateUserDto.username() + " - был успешно изменен.")
                .status(200)
                .httpStatus(HttpStatus.OK)
                .timestamp(LocalDateTime.now().withNano(0))
                .build();
    }

    @Override
    @Transactional
    public ApiResponse changeUserPassword(String userId, ChangeUserPasswordDto passwordDto) {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        int response;
        response = userRepository.changeUserPassword(
                userId,
                passwordEncoder.encode(passwordDto.newPassword()),
                SecurityUtil.getCurrentUserId(),
                now
        );

        if (response != 1) {
            throw new NotFoundException(USER_NOT_EXIST);
        }
        return ApiResponse.builder()
                .message("Пароль - был успешно изменён.")
                .status(200)
                .httpStatus(HttpStatus.OK)
                .timestamp(LocalDateTime.now().withNano(0))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.getAllUsers();
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUser(String userId) {
        User user = userRepository.findByIdAndEnabledTrue(userId).orElseThrow(
                () -> new NotFoundException(USER_NOT_EXIST)
        );
        return UserMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public ApiResponse deleteUser(String userId) {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        int response;
        response = userRepository.deleteUser(userId, SecurityUtil.getCurrentUserId(), now);

        if (response != 1) {
            throw new NotFoundException(USER_NOT_EXIST);
        }
        return ApiResponse.builder()
                .message("Пользователь - был успешно удалён.")
                .status(200)
                .httpStatus(HttpStatus.OK)
                .timestamp(LocalDateTime.now().withNano(0))
                .build();
    }
}
