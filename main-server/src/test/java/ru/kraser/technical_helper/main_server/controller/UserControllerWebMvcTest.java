package ru.kraser.technical_helper.main_server.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
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
import ru.kraser.technical_helper.common_module.dto.user.CreateUserDto;
import ru.kraser.technical_helper.common_module.dto.user.UpdateUserDto;
import ru.kraser.technical_helper.common_module.enums.Role;
import ru.kraser.technical_helper.main_server.service.UserService;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static ru.kraser.technical_helper.common_module.util.Constant.*;
import static ru.kraser.technical_helper.common_module.util.Constant.CURRENT_USER_ID_HEADER;
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