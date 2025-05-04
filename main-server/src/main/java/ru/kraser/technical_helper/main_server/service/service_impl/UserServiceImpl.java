package ru.kraser.technical_helper.main_server.service.service_impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kraser.technical_helper.common_module.dto.user.ChangeUserPasswordDto;
import ru.kraser.technical_helper.common_module.dto.user.CreateUserDto;
import ru.kraser.technical_helper.common_module.dto.user.UpdateUserDto;
import ru.kraser.technical_helper.common_module.dto.user.UserDto;
import ru.kraser.technical_helper.common_module.exception.NotFoundException;
import ru.kraser.technical_helper.main_server.model.Department;
import ru.kraser.technical_helper.main_server.model.User;
import ru.kraser.technical_helper.main_server.repository.DepartmentRepository;
import ru.kraser.technical_helper.main_server.repository.UserRepository;
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

    @Override
    @Transactional
    public String createUser(CreateUserDto createUserDto) {
        Department department = departmentRepository.getReferenceById(createUserDto.departmentId());
        try {
            userRepository.saveAndFlush(UserMapper.toUser(createUserDto, department));
        } catch (Exception e) {
            ThrowException.userUpdateHandler(e.getMessage(), createUserDto.username());
        }
        return "Сотрудник: " + createUserDto.username() + ", - был успешно создан.";
    }

    @Override
    @Transactional
    public String updateUser(String userId, UpdateUserDto updateUserDto) {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        // TODO - change to the current user
        // TODO - try return something
        try {
            userRepository.updateUser(
                    userId,
                    updateUserDto.username(),
                    updateUserDto.departmentId(),
                    updateUserDto.role(),
                    "some_new_id",
                    now);
        } catch (Exception e) {
            ThrowException.userUpdateHandler(e.getMessage(), updateUserDto.username());
        }
        return "Сотрудник: " + updateUserDto.username() + " - успешно изменен.";
    }

    @Override
    @Transactional
    public String changeUserPassword(String userId, ChangeUserPasswordDto passwordDto) {
        int response;
        response = userRepository.changeUserPassword(userId, passwordDto.newPassword());

        if (response != 1) {
            throw new NotFoundException(USER_NOT_EXIST);
        }
        return "Пароль - был успешно изменён.";
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
                () -> new NotFoundException("Данного пользователя не существует !!!")
        );
        return UserMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public String deleteUser(String userId) {
        int response;
        response = userRepository.deleteUser(userId);

        if (response != 1) {
            throw new NotFoundException(USER_NOT_EXIST);
        }
        return "Пользователь - был успешно удалён.";
    }
}
