package ru.kraser.technical_helper.main_server.service.service_impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kraser.technical_helper.common_module.dto.user.CreateUserDto;
import ru.kraser.technical_helper.common_module.dto.user.UpdateUserDto;
import ru.kraser.technical_helper.common_module.dto.user.UserDto;
import ru.kraser.technical_helper.common_module.exception.AlreadyExistsException;
import ru.kraser.technical_helper.common_module.exception.NotFoundException;
import ru.kraser.technical_helper.main_server.model.Department;
import ru.kraser.technical_helper.main_server.repository.DepartmentRepository;
import ru.kraser.technical_helper.main_server.repository.UserRepository;
import ru.kraser.technical_helper.main_server.service.UserService;
import ru.kraser.technical_helper.main_server.util.error_handler.ThrowException;
import ru.kraser.technical_helper.main_server.util.mapper.UserMapper;

import java.time.LocalDateTime;
import java.util.List;

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
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream().map(UserMapper::toUserDto).toList();
    }
}
