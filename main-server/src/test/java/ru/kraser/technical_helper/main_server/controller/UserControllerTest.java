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
import ru.kraser.technical_helper.common_module.dto.user.CreateUserDto;
import ru.kraser.technical_helper.common_module.enums.Role;
import ru.kraser.technical_helper.main_server.service.UserService;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
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

    private CreateUserDto createUserDto;
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

        createUserDto = CreateUserDto.builder()
                .username(USER_TEST_NAME)
                .password(USER_TEST_PASSWORD)
                .departmentId(DEPARTMENT_TEST_ID)
                .role(Role.ADMIN)
                .build();
    }

    @Nested
    class WhenUserCreating {

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

//    @Test
//    void updateUser() {
//    }
//
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