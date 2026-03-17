package ru.kraser.technical_helper.main_server.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import ru.kraser.technical_helper.common_module.dto.api.ApiResponse;
import ru.kraser.technical_helper.common_module.dto.user.*;
import ru.kraser.technical_helper.common_module.enums.Role;
import ru.kraser.technical_helper.common_module.exception.NotFoundException;
import ru.kraser.technical_helper.main_server.service.UserService;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static ru.kraser.technical_helper.common_module.util.Constant.USER_NOT_EXIST;
import static ru.kraser.technical_helper.main_server.util.Constant.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserControllerTest {

    @Mock
    private Clock clock;
    @Mock
    private UserService userService;
    @InjectMocks
    private UserController userController;

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
    }

    @Nested
    class WhenUserCreating {

        private CreateUserDto createUserDto;

        @BeforeEach
        void setUp() {

            createUserDto = CreateUserDto.builder()
                    .username(USER_TEST_NAME)
                    .password(USER_TEST_PASSWORD)
                    .departmentId(DEPARTMENT_TEST_ID)
                    .role(Role.ADMIN)
                    .build();
        }

        @Test
        void whenCreateUserThenReturnCreated() {

            String responseMessage = "Сотрудник: " + createUserDto.username() + ", - был успешно создан.";

            ApiResponse response = ApiResponse.builder()
                    .message(responseMessage)
                    .status(201)
                    .httpStatus(HttpStatus.CREATED)
                    .timestamp(now)
                    .build();

            when(userService.createUser(createUserDto, DEFAULT_ADMIN_USER_ID)).thenReturn(response);

            ApiResponse apiResponse = userController.createUser(DEFAULT_ADMIN_USER_ID, createUserDto);

            assertEquals(responseMessage, apiResponse.message());
            assertEquals(201, apiResponse.status());
            assertEquals(HttpStatus.CREATED, apiResponse.httpStatus());
            assertEquals(now, apiResponse.timestamp());

            verify(userService, times(1))
                    .createUser(createUserDto, DEFAULT_ADMIN_USER_ID);
        }

        @Test
        void whenCreateUserThenReturnUnprocessableEntity() {

            String responseMessage = "Сотрудник: " + createUserDto.username() + ", - уже существует. " +
                    "Используйте другое имя !!!";

            ApiResponse response = ApiResponse.builder()
                    .message(responseMessage)
                    .status(422)
                    .httpStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                    .timestamp(now)
                    .build();

            when(userService.createUser(createUserDto, DEFAULT_ADMIN_USER_ID)).thenReturn(response);

            ApiResponse apiResponse = userController.createUser(DEFAULT_ADMIN_USER_ID, createUserDto);

            assertEquals(responseMessage, apiResponse.message());
            assertEquals(422, apiResponse.status());
            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, apiResponse.httpStatus());
            assertEquals(now, apiResponse.timestamp());

            verify(userService, times(1))
                    .createUser(createUserDto, DEFAULT_ADMIN_USER_ID);
        }

        @Test
        void whenCreateUserIfDepartmentNotExistThenReturnNotFound() {

            String responseMessage = "Отдел в котором находится сотрудник не существует !!!";

            ApiResponse response = ApiResponse.builder()
                    .message(responseMessage)
                    .status(404)
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .timestamp(now)
                    .build();

            when(userService.createUser(createUserDto, DEFAULT_ADMIN_USER_ID)).thenReturn(response);

            ApiResponse apiResponse = userController.createUser(DEFAULT_ADMIN_USER_ID, createUserDto);

            assertEquals(responseMessage, apiResponse.message());
            assertEquals(404, apiResponse.status());
            assertEquals(HttpStatus.NOT_FOUND, apiResponse.httpStatus());
            assertEquals(now, apiResponse.timestamp());

            verify(userService, times(1))
                    .createUser(createUserDto, DEFAULT_ADMIN_USER_ID);
        }
    }

    @Nested
    class WhenUserUpdating {

        private UpdateUserDto updateUserDto;

        @BeforeEach
        void setUp() {

            updateUserDto = UpdateUserDto.builder()
                    .username(USER_TEST_NAME)
                    .departmentId(DEPARTMENT_TEST_ID)
                    .role(Role.TECHNICIAN)
                    .build();
        }

        @Test
        void whenUpdateUserThenReturnOk() {

            String responseMessage = "Сотрудник: " + updateUserDto.username() + " - был успешно изменен.";

            ApiResponse response = ApiResponse.builder()
                    .message(responseMessage)
                    .status(200)
                    .httpStatus(HttpStatus.OK)
                    .timestamp(now)
                    .build();

            when(userService.updateUser(USER_TEST_ID, updateUserDto, DEFAULT_ADMIN_USER_ID))
                    .thenReturn(response);

            ApiResponse apiResponse =
                    userController.updateUser(USER_TEST_ID, DEFAULT_ADMIN_USER_ID, updateUserDto);

            assertEquals(responseMessage, apiResponse.message());
            assertEquals(200, apiResponse.status());
            assertEquals(HttpStatus.OK, apiResponse.httpStatus());
            assertEquals(now, apiResponse.timestamp());

            verify(userService, times(1))
                    .updateUser(USER_TEST_ID, updateUserDto, DEFAULT_ADMIN_USER_ID);
        }

        @Test
        void whenUpdateUserThenReturnUnprocessableEntity() {

            String responseMessage = "Сотрудник: " + updateUserDto.username() + ", - уже существует. " +
                    "Используйте другое имя !!!";

            ApiResponse response = ApiResponse.builder()
                    .message(responseMessage)
                    .status(422)
                    .httpStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                    .timestamp(now)
                    .build();

            when(userService.updateUser(USER_TEST_ID, updateUserDto, DEFAULT_ADMIN_USER_ID))
                    .thenReturn(response);

            ApiResponse apiResponse =
                    userController.updateUser(USER_TEST_ID, DEFAULT_ADMIN_USER_ID, updateUserDto);

            assertEquals(responseMessage, apiResponse.message());
            assertEquals(422, apiResponse.status());
            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, apiResponse.httpStatus());
            assertEquals(now, apiResponse.timestamp());

            verify(userService, times(1))
                    .updateUser(USER_TEST_ID, updateUserDto, DEFAULT_ADMIN_USER_ID);
        }

        @Test
        void whenUpdateUserIfUserNotExistThenReturnNotFound() {

            String responseMessage = "Данный пользователь не существует !!!";

            ApiResponse response = ApiResponse.builder()
                    .message(responseMessage)
                    .status(404)
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .timestamp(now)
                    .build();

            when(userService.updateUser(USER_TEST_ID, updateUserDto, DEFAULT_ADMIN_USER_ID))
                    .thenReturn(response);

            ApiResponse apiResponse =
                    userController.updateUser(USER_TEST_ID, DEFAULT_ADMIN_USER_ID, updateUserDto);

            assertEquals(responseMessage, apiResponse.message());
            assertEquals(404, apiResponse.status());
            assertEquals(HttpStatus.NOT_FOUND, apiResponse.httpStatus());
            assertEquals(now, apiResponse.timestamp());

            verify(userService, times(1))
                    .updateUser(USER_TEST_ID, updateUserDto, DEFAULT_ADMIN_USER_ID);
        }

        @Test
        void whenUpdateUserIfUserDepartmentNotExistThenReturnNotFound() {

            String responseMessage = "Отдел в котором находится сотрудник не существует !!!";

            ApiResponse response = ApiResponse.builder()
                    .message(responseMessage)
                    .status(404)
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .timestamp(now)
                    .build();

            when(userService.updateUser(USER_TEST_ID, updateUserDto, DEFAULT_ADMIN_USER_ID))
                    .thenReturn(response);

            ApiResponse apiResponse =
                    userController.updateUser(USER_TEST_ID, DEFAULT_ADMIN_USER_ID, updateUserDto);

            assertEquals(responseMessage, apiResponse.message());
            assertEquals(404, apiResponse.status());
            assertEquals(HttpStatus.NOT_FOUND, apiResponse.httpStatus());
            assertEquals(now, apiResponse.timestamp());

            verify(userService, times(1))
                    .updateUser(USER_TEST_ID, updateUserDto, DEFAULT_ADMIN_USER_ID);
        }
    }

    @Nested
    class WhenUserChangingPassword {

        private UserPasswordDto userPasswordDto;

        @BeforeEach
        void setUp() {

            userPasswordDto = new UserPasswordDto("new_user_password");
        }

        @Test
        void whenChangeUserPasswordThenReturnOk() {

            String responseMessage = "Пароль - был успешно изменён.";

            ApiResponse response = ApiResponse.builder()
                    .message(responseMessage)
                    .status(200)
                    .httpStatus(HttpStatus.OK)
                    .timestamp(now)
                    .build();

            when(userService.changeUserPassword(USER_TEST_ID, userPasswordDto, DEFAULT_ADMIN_USER_ID))
                    .thenReturn(response);

            ApiResponse apiResponse =
                    userController.changeUserPassword(USER_TEST_ID, DEFAULT_ADMIN_USER_ID, userPasswordDto);

            assertEquals(responseMessage, apiResponse.message());
            assertEquals(200, apiResponse.status());
            assertEquals(HttpStatus.OK, apiResponse.httpStatus());
            assertEquals(now, apiResponse.timestamp());

            verify(userService, times(1))
                    .changeUserPassword(USER_TEST_ID, userPasswordDto, DEFAULT_ADMIN_USER_ID);
        }

        @Test
        void whenChangeUserPasswordIfThisUserNotExistThenThrowNotFoundException() {

            String responseMessage = USER_NOT_EXIST;

            ApiResponse response = ApiResponse.builder()
                    .message(responseMessage)
                    .status(404)
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .timestamp(now)
                    .build();

            when(userService.changeUserPassword(USER_TEST_ID, userPasswordDto, DEFAULT_ADMIN_USER_ID))
                    .thenReturn(response);

            ApiResponse apiResponse =
                    userController.changeUserPassword(USER_TEST_ID, DEFAULT_ADMIN_USER_ID, userPasswordDto);

            assertEquals(responseMessage, apiResponse.message());
            assertEquals(404, apiResponse.status());
            assertEquals(HttpStatus.NOT_FOUND, apiResponse.httpStatus());
            assertEquals(now, apiResponse.timestamp());

            verify(userService, times(1))
                    .changeUserPassword(USER_TEST_ID, userPasswordDto, DEFAULT_ADMIN_USER_ID);
        }
    }

    @Nested
    class WhenGetAllUsersMethodsInvoked {

        @Test
        void whenGetAllUsersThenReturnListOfUsers() {

            UserDto userDto = UserDto.builder()
                    .id(USER_TEST_ID)
                    .username(USER_TEST_NAME)
                    .departmentId(DEPARTMENT_TEST_ID)
                    .department(DEPARTMENT_TEST_NAME)
                    .role(Role.ADMIN)
                    .createdBy(DEFAULT_ADMIN_USER_ID)
                    .createdDate(now)
                    .lastUpdatedBy(DEFAULT_ADMIN_USER_ID)
                    .lastUpdatedDate(now)
                    .build();

            List<UserDto> users = List.of(userDto);

            when(userService.getAllUsers()).thenReturn(users);

            List<UserDto> returnedUsers = userController.getAllUsers();

            assertEquals(users, returnedUsers);

            verify(userService, times(1)).getAllUsers();
        }

        @Test
        void whenAdminAndTechnicianListThenReturnListOfAdminAndTechnicians() {

            UserShortDto userShortAdminDto = new UserShortDto(DEFAULT_ADMIN_USER_ID, "test_admin");
            UserShortDto userShortTechnicianDto = new UserShortDto(USER_TEST_ID, USER_TEST_NAME);

            List<UserShortDto> adminAndTechnicianList = List.of(userShortAdminDto, userShortTechnicianDto);

            when(userService.getAdminAndTechnicianList()).thenReturn(adminAndTechnicianList);

            List<UserShortDto> returnedList = userService.getAdminAndTechnicianList();

            assertEquals(adminAndTechnicianList, returnedList);

            verify(userService, times(1)).getAdminAndTechnicianList();
        }
    }

    @Nested
    class WhenGetUserById {

        @Test
        void whenGetUserByIdThenReturnUserDto() {

            UserDto expectedUserDto = UserDto.builder()
                    .id(USER_TEST_ID)
                    .username(USER_TEST_NAME)
                    .departmentId(DEPARTMENT_TEST_ID)
                    .department(DEPARTMENT_TEST_NAME)
                    .role(Role.ADMIN)
                    .createdBy(DEFAULT_ADMIN_USER_ID)
                    .createdDate(now)
                    .lastUpdatedBy(DEFAULT_ADMIN_USER_ID)
                    .lastUpdatedDate(now)
                    .build();

            when(userService.getUser(USER_TEST_ID)).thenReturn(expectedUserDto);

            UserDto returnedUser = userController.getUser(USER_TEST_ID);

            assertEquals(expectedUserDto, returnedUser);

            verify(userService, times(1)).getUser(USER_TEST_ID);
        }

        @Test
        void whenGetUserIfThisUserNotExistThenThrowNotFoundException() {

            when(userService.getUser(USER_TEST_ID)).thenThrow(new NotFoundException(USER_NOT_EXIST));

            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> userService.getUser(USER_TEST_ID)
            );

            assertEquals(USER_NOT_EXIST, exception.getMessage());

            verify(userService, times(1)).getUser(USER_TEST_ID);
        }
    }


//
//    @Test
//    void getUserByName() {
//    }
//
//    @Test
//    void deleteUser() {
//    }
}