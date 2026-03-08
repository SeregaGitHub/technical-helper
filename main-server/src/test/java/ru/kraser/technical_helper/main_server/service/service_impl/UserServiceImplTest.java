package ru.kraser.technical_helper.main_server.service.service_impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import ru.kraser.technical_helper.common_module.dto.api.ApiResponse;
import ru.kraser.technical_helper.common_module.dto.user.CreateUserDto;
import ru.kraser.technical_helper.common_module.dto.user.UpdateUserDto;
import ru.kraser.technical_helper.common_module.enums.Role;
import ru.kraser.technical_helper.common_module.exception.AlreadyExistsException;
import ru.kraser.technical_helper.common_module.exception.NotFoundException;
import ru.kraser.technical_helper.common_module.model.Department;
import ru.kraser.technical_helper.common_module.model.User;
import ru.kraser.technical_helper.main_server.repository.DepartmentRepository;
import ru.kraser.technical_helper.main_server.repository.UserRepository;
import ru.kraser.technical_helper.main_server.util.mapper.UserMapper;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static ru.kraser.technical_helper.main_server.util.Constant.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceImplTest {

    @Mock
    private Clock clock;
    @Mock
    private UserRepository userRepository;
    @Mock
    private DepartmentRepository departmentRepository;
    @InjectMocks
    UserServiceImpl userService;

    private Department department;
    private User user;
    private LocalDateTime now;

    private static final ZonedDateTime NOW_ZDT = ZonedDateTime.of(
            2025,
            9,
            29,
            13,
            0,
            0,
            0,
            ZoneId.of("UTC")
    );

    @BeforeEach
    void setUp() {

        now = LocalDateTime.of(
                2025,
                9,
                29,
                13,
                0,
                0);

        when(clock.getZone()).thenReturn(NOW_ZDT.getZone());
        when(clock.instant()).thenReturn(NOW_ZDT.toInstant());

        department = Department.builder()
                .id(DEPARTMENT_TEST_ID)
                .name(DEPARTMENT_TEST_NAME)
                .enabled(true)
                .createdBy(DEFAULT_ADMIN_USER_ID)
                .createdDate(now)
                .lastUpdatedBy(DEFAULT_ADMIN_USER_ID)
                .lastUpdatedDate(now)
                .build();

        user = User.builder()
                .id(USER_TEST_ID)
                .username(USER_TEST_NAME)
                .password(USER_TEST_PASSWORD)
                .enabled(true)
                .role(Role.ADMIN)
                .department(department)
                .createdBy(DEFAULT_ADMIN_USER_ID)
                .createdDate(now)
                .lastUpdatedBy(DEFAULT_ADMIN_USER_ID)
                .lastUpdatedDate(now)
                .build();
    }

    @Nested
    class WhenUserCreating {

        private CreateUserDto createUserDto;

        @BeforeEach
        void setUp() {

            createUserDto = new CreateUserDto(
                    user.getUsername(), user.getPassword(), user.getDepartment().getId(), user.getRole()
            );
        }

        @Test
        void whenCreateUserThenReturnCreated() {

            String responseMessage = "Сотрудник: " + createUserDto.username() + ", - был успешно создан.";

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(responseMessage)
                    .status(201)
                    .httpStatus(HttpStatus.CREATED)
                    .timestamp(now)
                    .build();

            when(departmentRepository.getReferenceById(user.getDepartment().getId()))
                    .thenReturn(department);
            when(userRepository.saveAndFlush(UserMapper.toUser(createUserDto, department, DEFAULT_ADMIN_USER_ID, now)))
                    .thenReturn(user);

            ApiResponse returnedApiResponse = userService.createUser(createUserDto, DEFAULT_ADMIN_USER_ID);

            assertEquals(apiResponse, returnedApiResponse);

            verify(userRepository, times(1))
                    .saveAndFlush(UserMapper.toUser(createUserDto, department, DEFAULT_ADMIN_USER_ID, now));
        }

        @Test
        void whenCreateUserWithNotUniqueNameThenThrowAlreadyExistsException() {

            String responseMessage = "Сотрудник: " + createUserDto.username() + ", - уже существует. " +
                    "Используйте другое имя !!!";

            when(departmentRepository.getReferenceById(user.getDepartment().getId()))
                    .thenReturn(department);
            when(userRepository.saveAndFlush(UserMapper.toUser(createUserDto, department, DEFAULT_ADMIN_USER_ID, now)))
                    .thenThrow(new DataIntegrityViolationException(
                                    "ОШИБКА: повторяющееся значение ключа нарушает ограничение уникальности " +
                                            "\"uk_users_username\""
                            )
                    );

            AlreadyExistsException exception = assertThrows(
                    AlreadyExistsException.class,
                    () -> userService.createUser(createUserDto, DEFAULT_ADMIN_USER_ID)
            );

            assertEquals(responseMessage, exception.getMessage());

            verify(userRepository, times(1))
                    .saveAndFlush(UserMapper.toUser(createUserDto, department, DEFAULT_ADMIN_USER_ID, now));
        }

        @Test
        void whenCreateUserWithNotExistDepartmentThenThrowNotFoundException() {

            String responseMessage = "Отдел в котором находится сотрудник не существует !!!";

            when(departmentRepository.getReferenceById(user.getDepartment().getId()))
                    .thenReturn(department);
            when(userRepository.saveAndFlush(UserMapper.toUser(createUserDto, department, DEFAULT_ADMIN_USER_ID, now)))
                    .thenThrow(new NotFoundException(
                                    "ОШИБКА: повторяющееся значение ключа нарушает ограничение уникальности " +
                                            "\"fk_users_department\""
                            )
                    );

            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> userService.createUser(createUserDto, DEFAULT_ADMIN_USER_ID)
            );

            assertEquals(responseMessage, exception.getMessage());

            verify(userRepository, times(1))
                    .saveAndFlush(UserMapper.toUser(createUserDto, department, DEFAULT_ADMIN_USER_ID, now));
        }
    }

    @Nested
    class WhenUserUpdating {

        UpdateUserDto updateUserDto;

        @BeforeEach
        void setUp() {

            updateUserDto = new UpdateUserDto("new_username", department.getId(), Role.TECHNICIAN);
        }

        @Test
        void whenUpdateUserThenReturnOk() {

            String responseMessage = "Сотрудник: " + updateUserDto.username() + " - был успешно изменен.";

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(responseMessage)
                    .status(200)
                    .httpStatus(HttpStatus.OK)
                    .timestamp(now)
                    .build();

            when(userRepository.updateUser(
                    user.getId(),
                    updateUserDto.username(),
                    updateUserDto.departmentId(),
                    updateUserDto.role(),
                    DEFAULT_ADMIN_USER_ID,
                    now)
            ).thenReturn(1);

            ApiResponse returnedApiResponse = userService.updateUser(
                    user.getId(), updateUserDto, DEFAULT_ADMIN_USER_ID);

            assertEquals(apiResponse, returnedApiResponse);

            verify(userRepository, times(1))
                    .updateUser(user.getId(),
                            updateUserDto.username(),
                            updateUserDto.departmentId(),
                            updateUserDto.role(),
                            DEFAULT_ADMIN_USER_ID,
                            now);
        }

        @Test
        void whenUpdateUserIfNotUniqueNameThenThrowAlreadyExistsException() {

            String responseMessage = "Сотрудник: " + updateUserDto.username() + ", - уже существует. " +
                    "Используйте другое имя !!!";

            when(userRepository.updateUser(
                    user.getId(),
                    updateUserDto.username(),
                    updateUserDto.departmentId(),
                    updateUserDto.role(),
                    DEFAULT_ADMIN_USER_ID,
                    now)
            ).thenThrow(new DataIntegrityViolationException(
                            "ОШИБКА: повторяющееся значение ключа нарушает ограничение уникальности \"uk_users_username\""
                    )
            );

            AlreadyExistsException exception = assertThrows(
                    AlreadyExistsException.class,
                    () -> userService.updateUser(user.getId(), updateUserDto, DEFAULT_ADMIN_USER_ID)
            );

            assertEquals(responseMessage, exception.getMessage());

            verify(userRepository, times(1))
                    .updateUser(user.getId(),
                            updateUserDto.username(),
                            updateUserDto.departmentId(),
                            updateUserDto.role(),
                            DEFAULT_ADMIN_USER_ID,
                            now);
        }

        @Test
        void whenUpdateUserIfThisUserNotExistThenThrowNotFoundException() {

            String responseMessage = "Отдел в котором находится сотрудник не существует !!!";

            when(userRepository.updateUser(
                    user.getId(),
                    updateUserDto.username(),
                    updateUserDto.departmentId(),
                    updateUserDto.role(),
                    DEFAULT_ADMIN_USER_ID,
                    now)
            ).thenThrow(new NotFoundException(responseMessage));

            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> userService.updateUser(user.getId(), updateUserDto, DEFAULT_ADMIN_USER_ID)
            );

            assertEquals(responseMessage, exception.getMessage());

            verify(userRepository, times(1))
                    .updateUser(user.getId(),
                            updateUserDto.username(),
                            updateUserDto.departmentId(),
                            updateUserDto.role(),
                            DEFAULT_ADMIN_USER_ID,
                            now);
        }
    }

//    @Test
//    void changeUserPassword() {
//    }
//
//    @Test
//    void getAllUsers() {
//    }
//
//    @Test
//    void getAdminAndTechnicianList() {
//    }
//
//    @Test
//    void getUser() {
//    }
//
//    @Test
//    void getUserByName() {
//    }
//
//    @Test
//    void deleteUser() {
//    }
}