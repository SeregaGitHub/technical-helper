package ru.kraser.technical_helper.main_server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.kraser.technical_helper.common_module.dto.api.ApiResponse;
import ru.kraser.technical_helper.common_module.dto.user.*;
import ru.kraser.technical_helper.common_module.enums.Role;
import ru.kraser.technical_helper.common_module.exception.NotFoundException;
import ru.kraser.technical_helper.main_server.service.UserService;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static ru.kraser.technical_helper.common_module.util.Constant.*;
import static ru.kraser.technical_helper.main_server.util.Constant.*;

@WebMvcTest(controllers = UserController.class)
class UserControllerWebMvcTest {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private Clock clock;

    private DateTimeFormatter dtf;
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

        dtf = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
        //dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
        @SneakyThrows
        void whenCreateUserThenReturnCreated() {

            String responseMessage = "Сотрудник: " + createUserDto.username() + ", - был успешно создан.";

            ApiResponse response = ApiResponse.builder()
                    .message(responseMessage)
                    .status(201)
                    .httpStatus(HttpStatus.CREATED)
                    .timestamp(now)
                    .build();

            when(userService.createUser(createUserDto, DEFAULT_ADMIN_USER_ID)).thenReturn(response);

            String result = mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + ADMIN_URL + USER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(CURRENT_USER_ID_HEADER, DEFAULT_ADMIN_USER_ID)
                            .content(objectMapper.writeValueAsString(createUserDto)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(201))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value("CREATED"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(dtf.format(now)))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertEquals(objectMapper.writeValueAsString(response), result);
            verify(userService, times(1))
                    .createUser(createUserDto, DEFAULT_ADMIN_USER_ID);
        }

        @Test
        @SneakyThrows
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

            String result = mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + ADMIN_URL + USER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(CURRENT_USER_ID_HEADER, DEFAULT_ADMIN_USER_ID)
                            .content(objectMapper.writeValueAsString(createUserDto)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(422))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value("UNPROCESSABLE_ENTITY"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(dtf.format(now)))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertEquals(objectMapper.writeValueAsString(response), result);
            verify(userService, times(1))
                    .createUser(createUserDto, DEFAULT_ADMIN_USER_ID);
        }

        @Test
        @SneakyThrows
        void whenCreateUserIfDepartmentNotExistThenReturnNotFound() {

            String responseMessage = "Отдел в котором находится сотрудник не существует !!!";

            ApiResponse response = ApiResponse.builder()
                    .message(responseMessage)
                    .status(404)
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .timestamp(now)
                    .build();

            when(userService.createUser(createUserDto, DEFAULT_ADMIN_USER_ID)).thenReturn(response);

            String result = mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + ADMIN_URL + USER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(CURRENT_USER_ID_HEADER, DEFAULT_ADMIN_USER_ID)
                            .content(objectMapper.writeValueAsString(createUserDto)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(404))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(dtf.format(now)))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertEquals(objectMapper.writeValueAsString(response), result);
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
        @SneakyThrows
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

            String result = mockMvc.perform(MockMvcRequestBuilders.patch(BASE_URL + ADMIN_URL + USER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(CURRENT_USER_ID_HEADER, DEFAULT_ADMIN_USER_ID)
                            .header(USER_ID_HEADER, USER_TEST_ID)
                            .content(objectMapper.writeValueAsString(updateUserDto)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(200))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value("OK"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(dtf.format(now)))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertEquals(objectMapper.writeValueAsString(response), result);
            verify(userService, times(1))
                    .updateUser(USER_TEST_ID, updateUserDto, DEFAULT_ADMIN_USER_ID);
        }

        @Test
        @SneakyThrows
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

            String result = mockMvc.perform(MockMvcRequestBuilders.patch(BASE_URL + ADMIN_URL + USER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(CURRENT_USER_ID_HEADER, DEFAULT_ADMIN_USER_ID)
                            .header(USER_ID_HEADER, USER_TEST_ID)
                            .content(objectMapper.writeValueAsString(updateUserDto)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(422))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value("UNPROCESSABLE_ENTITY"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(dtf.format(now)))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertEquals(objectMapper.writeValueAsString(response), result);
            verify(userService, times(1))
                    .updateUser(USER_TEST_ID, updateUserDto, DEFAULT_ADMIN_USER_ID);
        }

        @Test
        @SneakyThrows
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

            String result = mockMvc.perform(MockMvcRequestBuilders.patch(BASE_URL + ADMIN_URL + USER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(CURRENT_USER_ID_HEADER, DEFAULT_ADMIN_USER_ID)
                            .header(USER_ID_HEADER, USER_TEST_ID)
                            .content(objectMapper.writeValueAsString(updateUserDto)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(404))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(dtf.format(now)))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertEquals(objectMapper.writeValueAsString(response), result);
            verify(userService, times(1))
                    .updateUser(USER_TEST_ID, updateUserDto, DEFAULT_ADMIN_USER_ID);
        }

        @Test
        @SneakyThrows
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

            String result = mockMvc.perform(MockMvcRequestBuilders.patch(BASE_URL + ADMIN_URL + USER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(CURRENT_USER_ID_HEADER, DEFAULT_ADMIN_USER_ID)
                            .header(USER_ID_HEADER, USER_TEST_ID)
                            .content(objectMapper.writeValueAsString(updateUserDto)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(404))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(dtf.format(now)))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertEquals(objectMapper.writeValueAsString(response), result);
            verify(userService, times(1))
                    .updateUser(USER_TEST_ID, updateUserDto, DEFAULT_ADMIN_USER_ID);
        }
    }

    @Nested
    class WhenUserChangingPassword {

        private UserPasswordDto userPasswordDto;

        @BeforeEach
        void setUp() {

            userPasswordDto = new UserPasswordDto(USER_NEW_TEST_PASSWORD);
        }

        @Test
        @SneakyThrows
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

            String result = mockMvc.perform(MockMvcRequestBuilders.patch(BASE_URL + ADMIN_URL + USER_URL + PASSWORD_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(CURRENT_USER_ID_HEADER, DEFAULT_ADMIN_USER_ID)
                            .header(USER_ID_HEADER, USER_TEST_ID)
                            .content(objectMapper.writeValueAsString(userPasswordDto)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(200))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value("OK"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(dtf.format(now)))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertEquals(objectMapper.writeValueAsString(response), result);
            verify(userService, times(1))
                    .changeUserPassword(USER_TEST_ID, userPasswordDto, DEFAULT_ADMIN_USER_ID);
        }

        @Test
        @SneakyThrows
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

            String result = mockMvc.perform(MockMvcRequestBuilders.patch(BASE_URL + ADMIN_URL + USER_URL + PASSWORD_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(CURRENT_USER_ID_HEADER, DEFAULT_ADMIN_USER_ID)
                            .header(USER_ID_HEADER, USER_TEST_ID)
                            .content(objectMapper.writeValueAsString(userPasswordDto)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(404))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value("NOT_FOUND"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(dtf.format(now)))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertEquals(objectMapper.writeValueAsString(response), result);
            verify(userService, times(1))
                    .changeUserPassword(USER_TEST_ID, userPasswordDto, DEFAULT_ADMIN_USER_ID);
        }
    }

    @Nested
    class WhenGetAllUsersMethodsInvoked {

        private DateTimeFormatter userDtoDtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        @Test
        @SneakyThrows
        void whenGetAllUsersThenReturnListOfUsers() {

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

            List<UserDto> users = List.of(expectedUserDto);

            when(userService.getAllUsers()).thenReturn(users);

            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + ADMIN_URL + USER_URL + ALL_URL)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.size()", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].id")
                            .value(expectedUserDto.id()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].username")
                            .value(expectedUserDto.username()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].departmentId")
                            .value(expectedUserDto.departmentId()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].department")
                            .value(expectedUserDto.department()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].role")
                            .value(expectedUserDto.role().toString()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].createdBy")
                            .value(expectedUserDto.createdBy()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].createdDate")
                            .value(userDtoDtf.format(expectedUserDto.createdDate())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].lastUpdatedBy")
                            .value(expectedUserDto.lastUpdatedBy()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].lastUpdatedDate")
                            .value(userDtoDtf.format(expectedUserDto.lastUpdatedDate())));

            verify(userService ,times(1)).getAllUsers();
        }

        @Test
        @SneakyThrows
        void whenAdminAndTechnicianListThenReturnListOfAdminAndTechnicians() {

            UserShortDto userShortAdminDto = new UserShortDto(DEFAULT_ADMIN_USER_ID, "test_admin");
            UserShortDto userShortTechnicianDto = new UserShortDto(USER_TEST_ID, USER_TEST_NAME);

            List<UserShortDto> adminAndTechnicianList = List.of(userShortAdminDto, userShortTechnicianDto);

            when(userService.getAdminAndTechnicianList()).thenReturn(adminAndTechnicianList);

            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + ADMIN_URL + USER_URL + BREAKAGE_URL)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.size()", Matchers.is(2)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].id")
                            .value(userShortAdminDto.id()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].username")
                            .value(userShortAdminDto.username()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[1].id")
                            .value(userShortTechnicianDto.id()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[1].username")
                            .value(userShortTechnicianDto.username()));

            verify(userService ,times(1)).getAdminAndTechnicianList();
        }
    }

    @Nested
    class WhenGetUserById {

        private DateTimeFormatter userDtoDtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        @Test
        @SneakyThrows
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

            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + ADMIN_URL + USER_URL + CURRENT_URL)
                            .accept(MediaType.APPLICATION_JSON)
                            .header(USER_ID_HEADER, USER_TEST_ID))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id")
                            .value(expectedUserDto.id()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.username")
                            .value(expectedUserDto.username()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.departmentId")
                            .value(expectedUserDto.departmentId()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.department")
                            .value(expectedUserDto.department()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.role")
                            .value(expectedUserDto.role().toString()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.createdBy")
                            .value(expectedUserDto.createdBy()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.createdDate")
                            .value(userDtoDtf.format(expectedUserDto.createdDate())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.lastUpdatedBy")
                            .value(expectedUserDto.lastUpdatedBy()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.lastUpdatedDate")
                            .value(userDtoDtf.format(expectedUserDto.lastUpdatedDate())));

            verify(userService ,times(1)).getUser(USER_TEST_ID);
        }

        @Test
        @SneakyThrows
        void whenGetUserIfThisUserNotExistThenThrowNotFoundException() {

            when(userService.getUser(USER_TEST_ID)).thenThrow(new NotFoundException(USER_NOT_EXIST));

            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + ADMIN_URL + USER_URL + CURRENT_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(USER_ID_HEADER, USER_TEST_ID))
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(USER_NOT_EXIST))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            verify(userService ,times(1)).getUser(USER_TEST_ID);
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