package ru.kraser.technical_helper.main_server.service.service_impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kraser.technical_helper.common_module.dto.api.ApiResponse;
import ru.kraser.technical_helper.common_module.dto.user.*;
import ru.kraser.technical_helper.common_module.enums.Role;
import ru.kraser.technical_helper.common_module.exception.AlreadyExistsException;
import ru.kraser.technical_helper.common_module.exception.NotFoundException;
import ru.kraser.technical_helper.common_module.model.Department;
import ru.kraser.technical_helper.common_module.model.User;
import ru.kraser.technical_helper.main_server.repository.DepartmentRepository;
import ru.kraser.technical_helper.main_server.repository.UserRepository;
import ru.kraser.technical_helper.main_server.service.UserService;
import ru.kraser.technical_helper.main_server.util.error_handler.ThrowMainServerException;
import ru.kraser.technical_helper.main_server.util.mapper.UserMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static ru.kraser.technical_helper.common_module.util.Constant.USER_NOT_EXIST;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    @Value("${default.admin.name}")
    private String defaultAdminName;

    @Value("${default.admin.department}")
    private String defaultAdminDepartment;

    @Value("${default.admin.id}")
    private String defaultAdminId;

    @Override
    @Transactional
    public ApiResponse createUser(CreateUserDto createUserDto, String currentUserId) {
        try {
            Department department = departmentRepository.getReferenceById(createUserDto.departmentId());
            userRepository.saveAndFlush(UserMapper.toUser(createUserDto, department, currentUserId));
        } catch (Exception e) {
            ThrowMainServerException.userHandler(e.getMessage(), createUserDto.username());
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
    public ApiResponse updateUser(String userId, UpdateUserDto updateUserDto, String currentUserId) {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        int response;
        try {
            response = userRepository.updateUser(
                    userId,
                    updateUserDto.username(),
                    updateUserDto.departmentId(),
                    updateUserDto.role(),
                    currentUserId,
                    now);
            ThrowMainServerException.isExist(response, "пользователь");
        } catch (Exception e) {
            ThrowMainServerException.userHandler(e.getMessage(), updateUserDto.username());
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
    public ApiResponse changeUserPassword(String userId, UserPasswordDto passwordDto, String currentUserId) {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        int response;
        response = userRepository.changeUserPassword(
                userId,
                passwordDto.newPassword(),
                currentUserId,
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
    public List<UserShortDto> getAdminAndTechnicianList() {
        return userRepository.getAdminAndTechnicianList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUser(String userId) {
        return userRepository.getUserById(userId).orElseThrow(
                () -> new NotFoundException(USER_NOT_EXIST)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByName(String username) {
        UserFullDto userFullDto = userRepository.getUserByUsernameAndEnabledTrue(username).orElseThrow(
                () -> new NotFoundException(USER_NOT_EXIST)
        );
        return UserMapper.toUserFull(userFullDto);
    }

    @Override
    @Transactional
    public ApiResponse deleteUser(String userId, String currentUserId) {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        int response;
        response = userRepository.deleteUser(userId, currentUserId, now);

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

    @Override
    public ApiResponse createDefaultAdmin(UserPasswordDto userPasswordDto) {
        Optional<User> optionalUser = userRepository.findTop1ByRoleAndEnabledTrue(Role.ADMIN);

        if (optionalUser.isEmpty()) {
            LocalDateTime now = LocalDateTime.now().withNano(0);
            String temporaryAdminId = defaultAdminId;

            userRepository.dropConstraints();

            Optional<Department> departmentOptional = departmentRepository.findByName(defaultAdminDepartment);
            Department adminDepartment;

            if (departmentOptional.isPresent()) {
                adminDepartment = departmentOptional.get();
                if (!adminDepartment.isEnabled()) {
                    adminDepartment.setEnabled(true);
                    adminDepartment.setLastUpdatedBy(temporaryAdminId);
                    adminDepartment.setLastUpdatedDate(now);
                    adminDepartment = departmentRepository.save(adminDepartment);
                }
            } else {
                adminDepartment = new Department(defaultAdminDepartment, true);
                adminDepartment.setCreatedBy(temporaryAdminId);
                adminDepartment.setCreatedDate(now);
                adminDepartment.setLastUpdatedBy(temporaryAdminId);
                adminDepartment.setLastUpdatedDate(now);
                adminDepartment = departmentRepository.save(adminDepartment);
            }

            Optional<User> userOptional = userRepository.findUserByUsername(defaultAdminName);
            User admin;

            if (userOptional.isPresent()) {
                admin = userOptional.get();
                admin.setEnabled(true);
                admin.setRole(Role.ADMIN);
                admin.setLastUpdatedBy(temporaryAdminId);
                admin.setLastUpdatedDate(now);
                admin = userRepository.save(admin);
            } else {
                admin = new User(
                        defaultAdminName,
                        userPasswordDto.newPassword(),
                        true,
                        adminDepartment,
                        Role.ADMIN);
                admin.setCreatedBy(temporaryAdminId);
                admin.setCreatedDate(now);
                admin.setLastUpdatedBy(temporaryAdminId);
                admin.setLastUpdatedDate(now);
                admin = userRepository.save(admin);
            }

            userRepository.setCurrentId(adminDepartment.getId(), admin.getId(),
                    adminDepartment.getCreatedBy(), admin.getCreatedBy(),
                    adminDepartment.getLastUpdatedBy(), admin.getLastUpdatedBy(),
                    temporaryAdminId);

            return ApiResponse.builder()
                    .message("Сотрудник: " + defaultAdminName + ", - был успешно создан.")
                    .status(201)
                    .httpStatus(HttpStatus.CREATED)
                    .timestamp(LocalDateTime.now().withNano(0))
                    .build();
        } else {
            throw new AlreadyExistsException("Пользователь с правами администратора - уже существует !!!");
        }
    }
}
