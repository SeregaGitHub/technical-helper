package ru.kraser.technical_helper.breakage_server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.kraser.technical_helper.BreakageServer;
import ru.kraser.technical_helper.breakage_server.service.BreakageService;
import ru.kraser.technical_helper.common_module.dto.api.ApiResponse;
import ru.kraser.technical_helper.common_module.dto.api.AppPage;
import ru.kraser.technical_helper.common_module.dto.breakage.*;
import ru.kraser.technical_helper.common_module.dto.breakage_comment.BreakageCommentFrontDto;
import ru.kraser.technical_helper.common_module.enums.Priority;
import ru.kraser.technical_helper.common_module.enums.Role;
import ru.kraser.technical_helper.common_module.enums.Status;
import ru.kraser.technical_helper.common_module.exception.ForbiddenException;
import ru.kraser.technical_helper.common_module.exception.NotFoundException;
import ru.kraser.technical_helper.common_module.model.Breakage;
import ru.kraser.technical_helper.common_module.model.Department;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static ru.kraser.technical_helper.common_module.util.Constant.*;
import static ru.kraser.technical_helper.common_module.util.ConstantForTests.*;

@WebMvcTest(controllers = BreakageController.class)
@ContextConfiguration(classes = BreakageServer.class)
class BreakageControllerWebMvcTest {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private BreakageService breakageService;

    private DateTimeFormatter dtf;
    private LocalDateTime now;
    private Department testDepartment;
    private Breakage testBreakage;

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

        testDepartment = Department.builder()
                .id(DEPARTMENT_TEST_ID)
                .name(DEPARTMENT_TEST_NAME)
                .enabled(true)
                .createdBy(DEFAULT_ADMIN_USER_ID)
                .createdDate(now)
                .lastUpdatedBy(DEFAULT_ADMIN_USER_ID)
                .lastUpdatedDate(now)
                .build();

        testBreakage = Breakage.builder()
                .id(BREAKAGE_TEST_ID)
                .department(testDepartment)
                .room("some_room")
                .breakageTopic("test_breakage_topic")
                .breakageText("test_breakage_text")
                .status(Status.NEW)
                .priority(Priority.MEDIUM)
                .executor(null)
                .executorAppointedBy(null)
                .deadline(null)
                .createdBy(USER_TEST_ID)
                .createdDate(now)
                .lastUpdatedBy(USER_TEST_ID)
                .lastUpdatedDate(now)
                .build();
    }

    @Nested
    class WhenBreakageCreating {

        private CreateBreakageFullDto createBreakageFullDto;

        @BeforeEach
        void setUp() {

            createBreakageFullDto = CreateBreakageFullDto.builder()
                    .department(testBreakage.getDepartment())
                    .room(testBreakage.getRoom())
                    .breakageTopic(testBreakage.getBreakageTopic())
                    .breakageText(testBreakage.getBreakageText())
                    .build();
        }

        @Test
        @SneakyThrows
        void whenCreateBreakageThenReturnCreated() {

            String responseMessage = "Заявка о неисправности с темой: " +
                    createBreakageFullDto.breakageTopic() +
                    ", - была успешно создана.";

            ApiResponse response = ApiResponse.builder()
                    .message(responseMessage)
                    .status(201)
                    .httpStatus(HttpStatus.CREATED)
                    .timestamp(now)
                    .build();

            when(breakageService.createBreakage(createBreakageFullDto, USER_TEST_ID)).thenReturn(response);

            String result = mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + BREAKAGE_URL + EMPLOYEE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(CURRENT_USER_ID_HEADER, USER_TEST_ID)
                            .content(objectMapper.writeValueAsString(createBreakageFullDto)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(201))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value(HttpStatus.CREATED.name()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(dtf.format(now)))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertEquals(objectMapper.writeValueAsString(response), result);
            verify(breakageService, times(1))
                    .createBreakage(createBreakageFullDto, USER_TEST_ID);
        }

        @Test
        @SneakyThrows
        void whenCreateBreakageThenReturnNotFoundException() {

            String responseMessage = "Отдел, за которым числится неисправность не существует !!!";

            testDepartment.setId(SOME_NOT_EXIST_ID);

            ApiResponse response = ApiResponse.builder()
                    .message(responseMessage)
                    .status(404)
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .timestamp(now)
                    .build();

            when(breakageService.createBreakage(createBreakageFullDto, USER_TEST_ID)).thenReturn(response);

            createBreakageFullDto = CreateBreakageFullDto.builder()
                    .department(testDepartment)
                    .room(testBreakage.getRoom())
                    .breakageTopic(testBreakage.getBreakageTopic())
                    .breakageText(testBreakage.getBreakageText())
                    .build();

            String result = mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + BREAKAGE_URL + EMPLOYEE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(CURRENT_USER_ID_HEADER, USER_TEST_ID)
                            .content(objectMapper.writeValueAsString(createBreakageFullDto)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(404))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value(HttpStatus.NOT_FOUND.name()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(dtf.format(now)))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertEquals(objectMapper.writeValueAsString(response), result);
            verify(breakageService, times(1))
                    .createBreakage(createBreakageFullDto, USER_TEST_ID);
        }
    }

    @Nested
    class WhenBreakageCancelling {

        @Test
        @SneakyThrows
        void whenCancelBreakageByTechnicianThenReturnOk() {

            String responseMessage = "Заявка на неисправность была успешно отменена.";

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(responseMessage)
                    .status(200)
                    .httpStatus(HttpStatus.OK)
                    .timestamp(now)
                    .data(DEFAULT_ADMIN_USERNAME)
                    .build();

            when(breakageService.cancelBreakage(
                            testBreakage.getId(), testBreakage.getDepartment().getId(), DEFAULT_ADMIN_USER_ID,
                            Role.TECHNICIAN, DEFAULT_ADMIN_DEPARTMENT_ID, DEFAULT_ADMIN_USERNAME
                    )
            ).thenReturn(apiResponse);

            String result = mockMvc.perform(MockMvcRequestBuilders.patch(
                                    BASE_URL + BREAKAGE_URL + EMPLOYEE_URL + "/" + DEFAULT_ADMIN_USERNAME
                            )
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(CURRENT_USER_ID_HEADER, DEFAULT_ADMIN_USER_ID)
                            .header(BREAKAGE_ID_HEADER, testBreakage.getId())
                            .header(DEPARTMENT_ID_HEADER, testBreakage.getDepartment().getId())
                            .header(USER_ROLE_HEADER, Role.TECHNICIAN)
                            .header(USER_DEPARTMENT_ID_HEADER, DEFAULT_ADMIN_DEPARTMENT_ID))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(200))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value(HttpStatus.OK.name()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(dtf.format(now)))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertEquals(objectMapper.writeValueAsString(apiResponse), result);
            verify(breakageService, times(1))
                    .cancelBreakage(
                            testBreakage.getId(), testBreakage.getDepartment().getId(), DEFAULT_ADMIN_USER_ID,
                            Role.TECHNICIAN, DEFAULT_ADMIN_DEPARTMENT_ID, DEFAULT_ADMIN_USERNAME
                    );
        }

        @Test
        @SneakyThrows
        void whenCancelBreakageByEmployeeFromSameDepartmentThenReturnOk() {

            String responseMessage = "Заявка на неисправность была успешно отменена.";

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(responseMessage)
                    .status(200)
                    .httpStatus(HttpStatus.OK)
                    .timestamp(now)
                    .data(USER_TEST_NAME)
                    .build();

            when(breakageService.cancelBreakage(
                            testBreakage.getId(), testBreakage.getDepartment().getId(), USER_TEST_ID,
                            Role.EMPLOYEE, DEPARTMENT_TEST_ID, USER_TEST_NAME
                    )
            ).thenReturn(apiResponse);

            String result = mockMvc.perform(MockMvcRequestBuilders.patch(
                                    BASE_URL + BREAKAGE_URL + EMPLOYEE_URL + "/" + USER_TEST_NAME
                            )
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(CURRENT_USER_ID_HEADER, USER_TEST_ID)
                            .header(BREAKAGE_ID_HEADER, testBreakage.getId())
                            .header(DEPARTMENT_ID_HEADER, testBreakage.getDepartment().getId())
                            .header(USER_ROLE_HEADER, Role.EMPLOYEE)
                            .header(USER_DEPARTMENT_ID_HEADER, DEPARTMENT_TEST_ID))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(200))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value(HttpStatus.OK.name()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(dtf.format(now)))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertEquals(objectMapper.writeValueAsString(apiResponse), result);
            verify(breakageService, times(1))
                    .cancelBreakage(
                            testBreakage.getId(), testBreakage.getDepartment().getId(), USER_TEST_ID,
                            Role.EMPLOYEE, DEPARTMENT_TEST_ID, USER_TEST_NAME
                    );
        }

        @Test
        @SneakyThrows
        void whenCancelBreakageWhichNotExistThenReturnNotFoundException() {

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(BREAKAGE_NOT_EXIST)
                    .status(404)
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .timestamp(now)
                    .data(DEFAULT_ADMIN_USERNAME)
                    .build();

            when(breakageService.cancelBreakage(
                            testBreakage.getId(), testBreakage.getDepartment().getId(), DEFAULT_ADMIN_USER_ID,
                            Role.TECHNICIAN, DEFAULT_ADMIN_DEPARTMENT_ID, DEFAULT_ADMIN_USERNAME
                    )
            ).thenReturn(apiResponse);

            String result = mockMvc.perform(MockMvcRequestBuilders.patch(
                                    BASE_URL + BREAKAGE_URL + EMPLOYEE_URL + "/" + DEFAULT_ADMIN_USERNAME
                            )
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(CURRENT_USER_ID_HEADER, DEFAULT_ADMIN_USER_ID)
                            .header(BREAKAGE_ID_HEADER, testBreakage.getId())
                            .header(DEPARTMENT_ID_HEADER, testBreakage.getDepartment().getId())
                            .header(USER_ROLE_HEADER, Role.TECHNICIAN)
                            .header(USER_DEPARTMENT_ID_HEADER, DEFAULT_ADMIN_DEPARTMENT_ID))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(BREAKAGE_NOT_EXIST))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(404))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value(HttpStatus.NOT_FOUND.name()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(dtf.format(now)))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertEquals(objectMapper.writeValueAsString(apiResponse), result);
            verify(breakageService, times(1))
                    .cancelBreakage(
                            testBreakage.getId(), testBreakage.getDepartment().getId(), DEFAULT_ADMIN_USER_ID,
                            Role.TECHNICIAN, DEFAULT_ADMIN_DEPARTMENT_ID, DEFAULT_ADMIN_USERNAME
                    );
        }

        @Test
        @SneakyThrows
        void whenCancelBreakageByEmployeeFromOtherDepartmentThenReturnForbiddenException() {

            String responseMessage = "Только технический специалист или сотрудник отдела, " +
                    "в котором произошла неисправность, могут отменить заявку !!!";

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(responseMessage)
                    .status(422)
                    .httpStatus(HttpStatus.FORBIDDEN)
                    .timestamp(now)
                    .data(USER_TEST_NAME)
                    .build();

            when(breakageService.cancelBreakage(
                            testBreakage.getId(), testBreakage.getDepartment().getId(), USER_TEST_ID,
                            Role.EMPLOYEE, SOME_NOT_EXIST_ID, USER_TEST_NAME
                    )
            ).thenReturn(apiResponse);

            String result = mockMvc.perform(MockMvcRequestBuilders.patch(
                                    BASE_URL + BREAKAGE_URL + EMPLOYEE_URL + "/" + USER_TEST_NAME
                            )
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(CURRENT_USER_ID_HEADER, USER_TEST_ID)
                            .header(BREAKAGE_ID_HEADER, testBreakage.getId())
                            .header(DEPARTMENT_ID_HEADER, testBreakage.getDepartment().getId())
                            .header(USER_ROLE_HEADER, Role.EMPLOYEE)
                            .header(USER_DEPARTMENT_ID_HEADER, SOME_NOT_EXIST_ID))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(422))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value(HttpStatus.FORBIDDEN.name()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(dtf.format(now)))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertEquals(objectMapper.writeValueAsString(apiResponse), result);
            verify(breakageService, times(1))
                    .cancelBreakage(
                            testBreakage.getId(), testBreakage.getDepartment().getId(), USER_TEST_ID,
                            Role.EMPLOYEE, SOME_NOT_EXIST_ID, USER_TEST_NAME
                    );
        }
    }

    @Nested
    class WhenBreakageStatusUpdating {

        private UpdateBreakageStatusDto updateBreakageStatusDto;

        @Test
        @SneakyThrows
        void whenUpdateBreakageStatusThenReturnOk() {

            updateBreakageStatusDto = new UpdateBreakageStatusDto(Status.IN_PROGRESS);

            String responseMessage = "Статус заявки на неисправность был успешно изменен";

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(responseMessage)
                    .status(200)
                    .httpStatus(HttpStatus.OK)
                    .timestamp(now)
                    .data(DEFAULT_ADMIN_USERNAME)
                    .build();

            when(breakageService.updateBreakageStatus(
                            testBreakage.getId(), updateBreakageStatusDto, DEFAULT_ADMIN_USER_ID, DEFAULT_ADMIN_USERNAME
                    )
            ).thenReturn(apiResponse);

            String result = mockMvc.perform(MockMvcRequestBuilders.patch(
                                    BASE_URL + BREAKAGE_URL +
                                            TECHNICIAN_URL + STATUS_URL + "/" + DEFAULT_ADMIN_USERNAME
                            )
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(CURRENT_USER_ID_HEADER, DEFAULT_ADMIN_USER_ID)
                            .header(BREAKAGE_ID_HEADER, testBreakage.getId())
                            .content(objectMapper.writeValueAsString(updateBreakageStatusDto)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(200))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value(HttpStatus.OK.name()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(dtf.format(now)))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertEquals(objectMapper.writeValueAsString(apiResponse), result);
            verify(breakageService, times(1))
                    .updateBreakageStatus(
                            testBreakage.getId(), updateBreakageStatusDto, DEFAULT_ADMIN_USER_ID, DEFAULT_ADMIN_USERNAME
                    );
        }

        @Test
        @SneakyThrows
        void whenUpdateBreakageStatusThenResetExecutorAndReturnOk() {

            updateBreakageStatusDto = new UpdateBreakageStatusDto(Status.PAUSED);

            String responseMessage = "Статус заявки на неисправность был успешно изменен";

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(responseMessage)
                    .status(200)
                    .httpStatus(HttpStatus.OK)
                    .timestamp(now)
                    .data(DEFAULT_ADMIN_USERNAME)
                    .build();

            when(breakageService.updateBreakageStatus(
                            testBreakage.getId(), updateBreakageStatusDto, DEFAULT_ADMIN_USER_ID, DEFAULT_ADMIN_USERNAME
                    )
            ).thenReturn(apiResponse);

            String result = mockMvc.perform(MockMvcRequestBuilders.patch(
                                    BASE_URL + BREAKAGE_URL +
                                            TECHNICIAN_URL + STATUS_URL + "/" + DEFAULT_ADMIN_USERNAME
                            )
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(CURRENT_USER_ID_HEADER, DEFAULT_ADMIN_USER_ID)
                            .header(BREAKAGE_ID_HEADER, testBreakage.getId())
                            .content(objectMapper.writeValueAsString(updateBreakageStatusDto)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(200))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value(HttpStatus.OK.name()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(dtf.format(now)))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertEquals(objectMapper.writeValueAsString(apiResponse), result);
            verify(breakageService, times(1))
                    .updateBreakageStatus(
                            testBreakage.getId(), updateBreakageStatusDto, DEFAULT_ADMIN_USER_ID, DEFAULT_ADMIN_USERNAME
                    );
        }

        @Test
        @SneakyThrows
        void whenUpdateBreakageWhichNotExistThenReturnNotFoundException() {

            updateBreakageStatusDto = new UpdateBreakageStatusDto(Status.IN_PROGRESS);

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(BREAKAGE_NOT_EXIST)
                    .status(404)
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .timestamp(now)
                    .data(DEFAULT_ADMIN_USERNAME)
                    .build();

            when(breakageService.updateBreakageStatus(
                            SOME_NOT_EXIST_ID, updateBreakageStatusDto, DEFAULT_ADMIN_USER_ID, DEFAULT_ADMIN_USERNAME
                    )
            ).thenReturn(apiResponse);

            String result = mockMvc.perform(MockMvcRequestBuilders.patch(
                                    BASE_URL + BREAKAGE_URL +
                                            TECHNICIAN_URL + STATUS_URL + "/" + DEFAULT_ADMIN_USERNAME
                            )
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(CURRENT_USER_ID_HEADER, DEFAULT_ADMIN_USER_ID)
                            .header(BREAKAGE_ID_HEADER, SOME_NOT_EXIST_ID)
                            .content(objectMapper.writeValueAsString(updateBreakageStatusDto)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(BREAKAGE_NOT_EXIST))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(404))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value(HttpStatus.NOT_FOUND.name()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(dtf.format(now)))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertEquals(objectMapper.writeValueAsString(apiResponse), result);
            verify(breakageService, times(1))
                    .updateBreakageStatus(
                            SOME_NOT_EXIST_ID, updateBreakageStatusDto, DEFAULT_ADMIN_USER_ID, DEFAULT_ADMIN_USERNAME
                    );
        }

        @Test
        @SneakyThrows
        void whenUpdateBreakageStatusIfStatusIsNewThenReturnNotCorrectParameter() {

            updateBreakageStatusDto = new UpdateBreakageStatusDto(Status.NEW);

            String responseMessage = "Заявка на неисправность не может изменить статус на - \"Новая\" !!!";

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(responseMessage)
                    .status(400)
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .timestamp(now)
                    .data(DEFAULT_ADMIN_USERNAME)
                    .build();

            when(breakageService.updateBreakageStatus(
                            testBreakage.getId(), updateBreakageStatusDto, DEFAULT_ADMIN_USER_ID, DEFAULT_ADMIN_USERNAME
                    )
            ).thenReturn(apiResponse);

            String result = mockMvc.perform(MockMvcRequestBuilders.patch(
                                    BASE_URL + BREAKAGE_URL +
                                            TECHNICIAN_URL + STATUS_URL + "/" + DEFAULT_ADMIN_USERNAME
                            )
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(CURRENT_USER_ID_HEADER, DEFAULT_ADMIN_USER_ID)
                            .header(BREAKAGE_ID_HEADER, testBreakage.getId())
                            .content(objectMapper.writeValueAsString(updateBreakageStatusDto)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value(HttpStatus.BAD_REQUEST.name()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(dtf.format(now)))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertEquals(objectMapper.writeValueAsString(apiResponse), result);
            verify(breakageService, times(1))
                    .updateBreakageStatus(
                            testBreakage.getId(), updateBreakageStatusDto, DEFAULT_ADMIN_USER_ID, DEFAULT_ADMIN_USERNAME
                    );
        }
    }

    @Nested
    class WhenBreakagePriorityUpdating {

        private UpdateBreakagePriorityDto updateBreakagePriorityDto;

        @Test
        @SneakyThrows
        void whenUpdateBreakagePriorityThenReturnOk() {

            updateBreakagePriorityDto = new UpdateBreakagePriorityDto(Priority.HIGH, Status.IN_PROGRESS);

            String responseMessage = "Приоритет заявки на неисправность был успешно изменен";

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(responseMessage)
                    .status(200)
                    .httpStatus(HttpStatus.OK)
                    .timestamp(now)
                    .data(DEFAULT_ADMIN_USERNAME)
                    .build();

            when(breakageService.updateBreakagePriority(
                            testBreakage.getId(), updateBreakagePriorityDto,
                            DEFAULT_ADMIN_USER_ID, DEFAULT_ADMIN_USERNAME
                    )
            ).thenReturn(apiResponse);

            String result = mockMvc.perform(MockMvcRequestBuilders.patch(
                                    BASE_URL + BREAKAGE_URL +
                                            ADMIN_URL + PRIORITY_URL + "/" + DEFAULT_ADMIN_USERNAME
                            )
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(CURRENT_USER_ID_HEADER, DEFAULT_ADMIN_USER_ID)
                            .header(BREAKAGE_ID_HEADER, testBreakage.getId())
                            .content(objectMapper.writeValueAsString(updateBreakagePriorityDto)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(200))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value(HttpStatus.OK.name()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(dtf.format(now)))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertEquals(objectMapper.writeValueAsString(apiResponse), result);
            verify(breakageService, times(1))
                    .updateBreakagePriority(
                            testBreakage.getId(), updateBreakagePriorityDto, DEFAULT_ADMIN_USER_ID, DEFAULT_ADMIN_USERNAME
                    );
        }

        @Test
        @SneakyThrows
        void whenUpdateBreakageWhichNotExistThenReturnNotFoundException() {

            updateBreakagePriorityDto = new UpdateBreakagePriorityDto(Priority.HIGH, Status.IN_PROGRESS);

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(BREAKAGE_NOT_EXIST)
                    .status(404)
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .timestamp(now)
                    .data(DEFAULT_ADMIN_USERNAME)
                    .build();

            when(breakageService.updateBreakagePriority(
                            SOME_NOT_EXIST_ID, updateBreakagePriorityDto,
                            DEFAULT_ADMIN_USER_ID, DEFAULT_ADMIN_USERNAME
                    )
            ).thenReturn(apiResponse);

            String result = mockMvc.perform(MockMvcRequestBuilders.patch(
                                    BASE_URL + BREAKAGE_URL +
                                            ADMIN_URL + PRIORITY_URL + "/" + DEFAULT_ADMIN_USERNAME
                            )
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(CURRENT_USER_ID_HEADER, DEFAULT_ADMIN_USER_ID)
                            .header(BREAKAGE_ID_HEADER, SOME_NOT_EXIST_ID)
                            .content(objectMapper.writeValueAsString(updateBreakagePriorityDto)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(BREAKAGE_NOT_EXIST))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(404))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value(HttpStatus.NOT_FOUND.name()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(dtf.format(now)))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertEquals(objectMapper.writeValueAsString(apiResponse), result);
            verify(breakageService, times(1))
                    .updateBreakagePriority(
                            SOME_NOT_EXIST_ID, updateBreakagePriorityDto, DEFAULT_ADMIN_USER_ID, DEFAULT_ADMIN_USERNAME
                    );
        }

        @Test
        @SneakyThrows
        void whenUpdateBreakagePriorityIfStatusIsSolvedOrCancelledThenReturnNotCorrectParameter() {

            updateBreakagePriorityDto = new UpdateBreakagePriorityDto(Priority.HIGH, Status.SOLVED);

            String responseMessage = "Заявка на неисправность со статусом: \"Решена\" или \"Отменена\"" +
                    " - не может быть изменена !!!";

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(responseMessage)
                    .status(400)
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .timestamp(now)
                    .data(DEFAULT_ADMIN_USERNAME)
                    .build();

            when(breakageService.updateBreakagePriority(
                            testBreakage.getId(), updateBreakagePriorityDto,
                            DEFAULT_ADMIN_USER_ID, DEFAULT_ADMIN_USERNAME
                    )
            ).thenReturn(apiResponse);

            String result = mockMvc.perform(MockMvcRequestBuilders.patch(
                                    BASE_URL + BREAKAGE_URL +
                                            ADMIN_URL + PRIORITY_URL + "/" + DEFAULT_ADMIN_USERNAME
                            )
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(CURRENT_USER_ID_HEADER, DEFAULT_ADMIN_USER_ID)
                            .header(BREAKAGE_ID_HEADER, testBreakage.getId())
                            .content(objectMapper.writeValueAsString(updateBreakagePriorityDto)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value(HttpStatus.BAD_REQUEST.name()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(dtf.format(now)))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertEquals(objectMapper.writeValueAsString(apiResponse), result);
            verify(breakageService, times(1))
                    .updateBreakagePriority(
                            testBreakage.getId(), updateBreakagePriorityDto, DEFAULT_ADMIN_USER_ID, DEFAULT_ADMIN_USERNAME
                    );
        }
    }

    @Nested
    class WhenBreakageExecutorAdding {

        private LocalDate afterNowDate;
        private AppointBreakageExecutorDto appointBreakageExecutorDto;

        @BeforeEach
        void setUp() {

            afterNowDate = now.plusDays(1).toLocalDate();
        }

        @Test
        @SneakyThrows
        void whenAddBreakageExecutorThenReturnOk() {

            appointBreakageExecutorDto = new AppointBreakageExecutorDto(DEFAULT_ADMIN_USER_ID, afterNowDate, Status.NEW);

            String responseMessage = "Исполнитель заявки на неисправность и срок исполнения были успешно назначены.";

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(responseMessage)
                    .status(200)
                    .httpStatus(HttpStatus.OK)
                    .timestamp(now)
                    .data(DEFAULT_ADMIN_USERNAME)
                    .build();

            when(breakageService.addBreakageExecutor(
                            testBreakage.getId(), appointBreakageExecutorDto,
                            DEFAULT_ADMIN_USER_ID, DEFAULT_ADMIN_USERNAME
                    )
            ).thenReturn(apiResponse);

            String result = mockMvc.perform(MockMvcRequestBuilders.patch(
                                    BASE_URL + BREAKAGE_URL +
                                            ADMIN_URL + EXECUTOR_URL + "/" + DEFAULT_ADMIN_USERNAME
                            )
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(CURRENT_USER_ID_HEADER, DEFAULT_ADMIN_USER_ID)
                            .header(BREAKAGE_ID_HEADER, testBreakage.getId())
                            .content(objectMapper.writeValueAsString(appointBreakageExecutorDto)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(200))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value(HttpStatus.OK.name()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(dtf.format(now)))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertEquals(objectMapper.writeValueAsString(apiResponse), result);
            verify(breakageService, times(1))
                    .addBreakageExecutor(
                            testBreakage.getId(), appointBreakageExecutorDto, DEFAULT_ADMIN_USER_ID, DEFAULT_ADMIN_USERNAME
                    );
        }

        @Test
        @SneakyThrows
        void whenAddBreakageExecutorIfBreakageNotExistThenReturnNotFoundException() {

            appointBreakageExecutorDto = new AppointBreakageExecutorDto(DEFAULT_ADMIN_USER_ID, afterNowDate, Status.NEW);

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(BREAKAGE_NOT_EXIST)
                    .status(404)
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .timestamp(now)
                    .data(DEFAULT_ADMIN_USERNAME)
                    .build();

            when(breakageService.addBreakageExecutor(
                            SOME_NOT_EXIST_ID, appointBreakageExecutorDto,
                            DEFAULT_ADMIN_USER_ID, DEFAULT_ADMIN_USERNAME
                    )
            ).thenReturn(apiResponse);

            String result = mockMvc.perform(MockMvcRequestBuilders.patch(
                                    BASE_URL + BREAKAGE_URL +
                                            ADMIN_URL + EXECUTOR_URL + "/" + DEFAULT_ADMIN_USERNAME
                            )
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(CURRENT_USER_ID_HEADER, DEFAULT_ADMIN_USER_ID)
                            .header(BREAKAGE_ID_HEADER, SOME_NOT_EXIST_ID)
                            .content(objectMapper.writeValueAsString(appointBreakageExecutorDto)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(BREAKAGE_NOT_EXIST))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(404))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value(HttpStatus.NOT_FOUND.name()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(dtf.format(now)))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertEquals(objectMapper.writeValueAsString(apiResponse), result);
            verify(breakageService, times(1))
                    .addBreakageExecutor(
                            SOME_NOT_EXIST_ID, appointBreakageExecutorDto, DEFAULT_ADMIN_USER_ID, DEFAULT_ADMIN_USERNAME
                    );
        }

        @Test
        @SneakyThrows
        void whenAddBreakageExecutorIfExecutorNotExistThenReturnNotFoundException() {

            appointBreakageExecutorDto = new AppointBreakageExecutorDto(SOME_NOT_EXIST_ID, afterNowDate, Status.NEW);

            String responseMessage =
                    "Пользователь, который назначается исполнителем заявки на неисправность, не существует.";

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(responseMessage)
                    .status(404)
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .timestamp(now)
                    .build();

            when(breakageService.addBreakageExecutor(
                            testBreakage.getId(), appointBreakageExecutorDto,
                            DEFAULT_ADMIN_USER_ID, DEFAULT_ADMIN_USERNAME
                    )
            ).thenReturn(apiResponse);

            String result = mockMvc.perform(MockMvcRequestBuilders.patch(
                                    BASE_URL + BREAKAGE_URL +
                                            ADMIN_URL + EXECUTOR_URL + "/" + DEFAULT_ADMIN_USERNAME
                            )
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(CURRENT_USER_ID_HEADER, DEFAULT_ADMIN_USER_ID)
                            .header(BREAKAGE_ID_HEADER, testBreakage.getId())
                            .content(objectMapper.writeValueAsString(appointBreakageExecutorDto)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(404))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value(HttpStatus.NOT_FOUND.name()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(dtf.format(now)))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertEquals(objectMapper.writeValueAsString(apiResponse), result);
            verify(breakageService, times(1))
                    .addBreakageExecutor(
                            testBreakage.getId(), appointBreakageExecutorDto, DEFAULT_ADMIN_USER_ID, DEFAULT_ADMIN_USERNAME
                    );
        }

        @Test
        @SneakyThrows
        void whenAddBreakageExecutorIfDeadlineIsNotCorrectThenReturnNotCorrectParameter() {

            LocalDateTime beforeNow = now.minusDays(1);
            LocalDate beforeNowDate = beforeNow.toLocalDate();

            appointBreakageExecutorDto = new AppointBreakageExecutorDto(DEFAULT_ADMIN_USER_ID, beforeNowDate, Status.NEW);

            String responseMessage = "Необходимо указать корректный срок исполнения заявки на неисправность.";

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(responseMessage)
                    .status(400)
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .timestamp(now)
                    .build();

            when(breakageService.addBreakageExecutor(
                            testBreakage.getId(), appointBreakageExecutorDto,
                            DEFAULT_ADMIN_USER_ID, DEFAULT_ADMIN_USERNAME
                    )
            ).thenReturn(apiResponse);

            String result = mockMvc.perform(MockMvcRequestBuilders.patch(
                                    BASE_URL + BREAKAGE_URL +
                                            ADMIN_URL + EXECUTOR_URL + "/" + DEFAULT_ADMIN_USERNAME
                            )
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(CURRENT_USER_ID_HEADER, DEFAULT_ADMIN_USER_ID)
                            .header(BREAKAGE_ID_HEADER, testBreakage.getId())
                            .content(objectMapper.writeValueAsString(appointBreakageExecutorDto)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value(HttpStatus.BAD_REQUEST.name()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(dtf.format(now)))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertEquals(objectMapper.writeValueAsString(apiResponse), result);
            verify(breakageService, times(1))
                    .addBreakageExecutor(
                            testBreakage.getId(), appointBreakageExecutorDto, DEFAULT_ADMIN_USER_ID, DEFAULT_ADMIN_USERNAME
                    );
        }

        @Test
        @SneakyThrows
        void whenAddBreakageExecutorIfStatusIsNotCorrectThenReturnNotCorrectParameter() {

            appointBreakageExecutorDto = new AppointBreakageExecutorDto(USER_TEST_ID, afterNowDate, Status.SOLVED);

            String responseMessage = "Заявке на неисправность со статусами: \"В ожидании\", \"Передана\"" +
                    ", \"Решена\" или \"Отменена\" - не может быть назначен исполнитель !!!";

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(responseMessage)
                    .status(400)
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .timestamp(now)
                    .build();

            when(breakageService.addBreakageExecutor(
                            testBreakage.getId(), appointBreakageExecutorDto,
                            DEFAULT_ADMIN_USER_ID, DEFAULT_ADMIN_USERNAME
                    )
            ).thenReturn(apiResponse);

            String result = mockMvc.perform(MockMvcRequestBuilders.patch(
                                    BASE_URL + BREAKAGE_URL +
                                            ADMIN_URL + EXECUTOR_URL + "/" + DEFAULT_ADMIN_USERNAME
                            )
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(CURRENT_USER_ID_HEADER, DEFAULT_ADMIN_USER_ID)
                            .header(BREAKAGE_ID_HEADER, testBreakage.getId())
                            .content(objectMapper.writeValueAsString(appointBreakageExecutorDto)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value(HttpStatus.BAD_REQUEST.name()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(dtf.format(now)))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertEquals(objectMapper.writeValueAsString(apiResponse), result);
            verify(breakageService, times(1))
                    .addBreakageExecutor(
                            testBreakage.getId(), appointBreakageExecutorDto, DEFAULT_ADMIN_USER_ID, DEFAULT_ADMIN_USERNAME
                    );
        }
    }

    @Nested
    class WhenBreakageExecutorDropping {

        @Test
        @SneakyThrows
        void whenDropBreakageThenReturnOk() {

            String responseMessage = "Исполнитель заявки на неисправность и срок исполнения были успешно удалены.";

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(responseMessage)
                    .status(200)
                    .httpStatus(HttpStatus.OK)
                    .timestamp(now)
                    .data(DEFAULT_ADMIN_USERNAME)
                    .build();

            when(breakageService.dropBreakageExecutor(
                            testBreakage.getId(), DEFAULT_ADMIN_USER_ID, DEFAULT_ADMIN_USERNAME
                    )
            ).thenReturn(apiResponse);

            String result = mockMvc.perform(MockMvcRequestBuilders.patch(
                                    BASE_URL + BREAKAGE_URL +
                                            ADMIN_URL + EXECUTOR_URL + DELETE_URL + "/" + DEFAULT_ADMIN_USERNAME
                            )
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(CURRENT_USER_ID_HEADER, DEFAULT_ADMIN_USER_ID)
                            .header(BREAKAGE_ID_HEADER, testBreakage.getId()))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(200))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value(HttpStatus.OK.name()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(dtf.format(now)))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertEquals(objectMapper.writeValueAsString(apiResponse), result);
            verify(breakageService, times(1))
                    .dropBreakageExecutor(
                            testBreakage.getId(), DEFAULT_ADMIN_USER_ID, DEFAULT_ADMIN_USERNAME
                    );
        }

        @Test
        @SneakyThrows
        void whenDropBreakageThenReturnNotFoundException() {

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(BREAKAGE_NOT_EXIST)
                    .status(404)
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .timestamp(now)
                    .build();

            when(breakageService.dropBreakageExecutor(
                            SOME_NOT_EXIST_ID, DEFAULT_ADMIN_USER_ID, DEFAULT_ADMIN_USERNAME
                    )
            ).thenReturn(apiResponse);

            String result = mockMvc.perform(MockMvcRequestBuilders.patch(
                                    BASE_URL + BREAKAGE_URL +
                                            ADMIN_URL + EXECUTOR_URL + DELETE_URL + "/" + DEFAULT_ADMIN_USERNAME
                            )
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(CURRENT_USER_ID_HEADER, DEFAULT_ADMIN_USER_ID)
                            .header(BREAKAGE_ID_HEADER, SOME_NOT_EXIST_ID))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(BREAKAGE_NOT_EXIST))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(404))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value(HttpStatus.NOT_FOUND.name()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(dtf.format(now)))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertEquals(objectMapper.writeValueAsString(apiResponse), result);
            verify(breakageService, times(1))
                    .dropBreakageExecutor(
                            SOME_NOT_EXIST_ID, DEFAULT_ADMIN_USER_ID, DEFAULT_ADMIN_USERNAME
                    );
        }
    }

    @Nested
    class WhenBreakagesGetting {

        private Integer pageSize;
        private Integer pageIndex;
        private String defaultSortBy;
        private String defaultDirection;
        private String defaultExecutor;
        private String defaultSearchText;

        @BeforeEach
        void setUp() {

            pageSize = 10;
            pageIndex = 0;
            defaultSortBy = "lastUpdatedDate";
            defaultDirection = "DESC";
            defaultExecutor = "ALL";
            defaultSearchText = "breakage";
        }

        @Nested
        class WhenAllBreakagesGettingByEmployee {

            private BreakageEmployeeDto breakageEmployeeDto;
            private List<BreakageEmployeeDto> content;
            private AppPage employeeAppPage;

            @BeforeEach
            void setUp() {

                breakageEmployeeDto = BreakageEmployeeDto.builder()
                        .id(testBreakage.getId())
                        .departmentId(testBreakage.getDepartment().getId())
                        .departmentName(testBreakage.getDepartment().getName())
                        .room(testBreakage.getRoom())
                        .breakageTopic(testBreakage.getBreakageTopic())
                        .breakageText(testBreakage.getBreakageText())
                        .status(testBreakage.getStatus())
                        .breakageExecutor(null)
                        .createdBy(testBreakage.getCreatedBy())
                        .createdDate(testBreakage.getCreatedDate())
                        .build();

                content = List.of(breakageEmployeeDto);

                employeeAppPage = AppPage.builder()
                        .content(content)
                        .totalElements(1L)
                        .totalPages(1)
                        .numberOfElements(1)
                        .pageNumber(0)
                        .pageSize(10)
                        .offset(0L)
                        .first(true)
                        .last(true)
                        .isForEmployee(true)
                        .now(null)
                        .build();
            }

            @Test
            @SneakyThrows
            void whenGetAllEmployeeBreakagesThenReturnAppPage() {

                when(breakageService.getAllBreakages(
                                pageSize, pageIndex, defaultSortBy, defaultDirection,
                                true, true, true, true, true,
                                true, true, true, true, true,
                                defaultExecutor, false, null,
                                Role.EMPLOYEE, DEPARTMENT_TEST_ID, USER_TEST_ID
                        )
                ).thenReturn(employeeAppPage);

                String result = mockMvc.perform(MockMvcRequestBuilders.get(
                                        BASE_URL + BREAKAGE_URL + EMPLOYEE_URL
                                )
                                .accept(MediaType.APPLICATION_JSON)
                                .header(USER_ROLE_HEADER, Role.EMPLOYEE)
                                .header(USER_DEPARTMENT_ID_HEADER, DEPARTMENT_TEST_ID)
                                .header(CURRENT_USER_ID_HEADER, USER_TEST_ID)
                                .param("pageSize", pageSize.toString())
                                .param("pageIndex", pageIndex.toString())
                                .param("sortBy", defaultSortBy)
                                .param("direction", defaultDirection)
                                .param("statusNew", String.valueOf(true))
                                .param("statusSolved", String.valueOf(true))
                                .param("statusInProgress", String.valueOf(true))
                                .param("statusPaused", String.valueOf(true))
                                .param("statusRedirected", String.valueOf(true))
                                .param("statusCancelled", String.valueOf(true))
                                .param("priorityUrgently", String.valueOf(true))
                                .param("priorityHigh", String.valueOf(true))
                                .param("priorityMedium", String.valueOf(true))
                                .param("priorityLow", String.valueOf(true))
                                .param("breakageExecutor", "ALL")
                                .param("deadline", String.valueOf(false))
                                .param("searchText", (String) null))
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id")
                                .value(breakageEmployeeDto.getId()))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].departmentId")
                                .value(testBreakage.getDepartment().getId()))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].departmentName")
                                .value(testBreakage.getDepartment().getName()))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].room")
                                .value(testBreakage.getRoom()))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].breakageTopic")
                                .value(testBreakage.getBreakageTopic()))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].breakageText")
                                .value(testBreakage.getBreakageText()))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].status")
                                .value(testBreakage.getStatus().name()))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].breakageExecutor")
                                .value(testBreakage.getExecutor()))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].createdBy")
                                .value(testBreakage.getCreatedBy()))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].createdDate")
                                .value(dtf.format(testBreakage.getCreatedDate())))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements").value(1))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value(1))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.numberOfElements").value(1))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.pageNumber").value(pageIndex))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.pageSize").value(pageSize))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.offset").value(0))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.first").value(true))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.last").value(true))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.isForEmployee").value(true))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.now").value((String) null))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();


                assertEquals(objectMapper.writeValueAsString(employeeAppPage), result);
                verify(breakageService, times(1))
                        .getAllBreakages(
                                pageSize, pageIndex, defaultSortBy, defaultDirection,
                                true, true, true, true, true,
                                true, true, true, true, true,
                                defaultExecutor, false, null,
                                Role.EMPLOYEE, DEPARTMENT_TEST_ID, USER_TEST_ID
                        );
            }
        }
    }

    @Nested
    class WhenBreakageByEmployeeGetting {

        @Test
        @SneakyThrows
        void whenGetBreakageByEmployeeThenReturnBreakage() {

            BreakageEmployeeDto breakageEmployeeDto = BreakageEmployeeDto.builder()
                    .id(testBreakage.getId())
                    .departmentId(testBreakage.getDepartment().getId())
                    .departmentName(testBreakage.getDepartment().getName())
                    .room(testBreakage.getRoom())
                    .breakageTopic(testBreakage.getBreakageTopic())
                    .breakageText(testBreakage.getBreakageText())
                    .status(testBreakage.getStatus())
                    .breakageExecutor(null)
                    .createdBy(testBreakage.getCreatedBy())
                    .createdDate(testBreakage.getCreatedDate())
                    .build();

            when(breakageService.getBreakageEmployee(testBreakage.getId(), testBreakage.getDepartment().getId()))
                    .thenReturn(breakageEmployeeDto);

            String result = mockMvc.perform(MockMvcRequestBuilders.get(
                                    BASE_URL + BREAKAGE_URL + EMPLOYEE_URL + CURRENT_URL
                            )
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(USER_DEPARTMENT_ID_HEADER, testBreakage.getDepartment().getId())
                            .header(BREAKAGE_ID_HEADER, testBreakage.getId()))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id")
                            .value(breakageEmployeeDto.getId()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.departmentId")
                            .value(breakageEmployeeDto.getDepartmentId()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.departmentName")
                            .value(breakageEmployeeDto.getDepartmentName()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.room")
                            .value(breakageEmployeeDto.getRoom()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.breakageTopic")
                            .value(breakageEmployeeDto.getBreakageTopic()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.breakageText")
                            .value(breakageEmployeeDto.getBreakageText()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status")
                            .value(breakageEmployeeDto.getStatus().name()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.breakageExecutor")
                            .value(breakageEmployeeDto.getBreakageExecutor()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.createdBy")
                            .value(breakageEmployeeDto.getCreatedBy()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.createdDate")
                            .value(dtf.format(breakageEmployeeDto.getCreatedDate())))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertEquals(objectMapper.writeValueAsString(breakageEmployeeDto), result);

            verify(breakageService, times(1))
                    .getBreakageEmployee(testBreakage.getId(), testBreakage.getDepartment().getId());
        }

        @Test
        @SneakyThrows
        void whenGetBreakageByEmployeeThenReturnForbiddenException() {

            String message = "Данный пользователь не имеет право на получение информации по " +
                    "этой заявке на неисправность !!!";

            when(breakageService.getBreakageEmployee(testBreakage.getId(), SOME_NOT_EXIST_ID))
                    .thenThrow(new ForbiddenException(message));

            mockMvc.perform(MockMvcRequestBuilders.get(
                    BASE_URL + BREAKAGE_URL + EMPLOYEE_URL + CURRENT_URL
                            )
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(USER_DEPARTMENT_ID_HEADER, SOME_NOT_EXIST_ID)
                            .header(BREAKAGE_ID_HEADER, testBreakage.getId()))
                    .andExpect(MockMvcResultMatchers.status().isForbidden())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                            .value(message));

            verify(breakageService, times(1))
                    .getBreakageEmployee(testBreakage.getId(), SOME_NOT_EXIST_ID);
        }

        @Test
        @SneakyThrows
        void whenGetBreakageByEmployeeThenReturnNotFoundException() {

            when(breakageService.getBreakageEmployee(SOME_NOT_EXIST_ID, DEPARTMENT_TEST_ID))
                    .thenThrow(new NotFoundException(BREAKAGE_NOT_EXIST));

            mockMvc.perform(MockMvcRequestBuilders.get(
                                    BASE_URL + BREAKAGE_URL + EMPLOYEE_URL + CURRENT_URL
                            )
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(USER_DEPARTMENT_ID_HEADER, testBreakage.getDepartment().getId())
                            .header(BREAKAGE_ID_HEADER, SOME_NOT_EXIST_ID))
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                            .value(BREAKAGE_NOT_EXIST));

            verify(breakageService, times(1))
                    .getBreakageEmployee(SOME_NOT_EXIST_ID, testBreakage.getDepartment().getId());
        }
    }

    @Nested
    class WhenBreakageGetting {

        @Test
        @SneakyThrows
        void whenGetBreakageThenReturnBreakage() {

            BreakageCommentFrontDto comment = BreakageCommentFrontDto.builder()
                    .id(BREAKAGE_COMMENT_TEST_ID)
                    .comment(BREAKAGE_COMMENT_TEST_TEXT)
                    .actionEnabled(true)
                    .creatorName(testBreakage.getCreatedBy())
                    .createdDate(testBreakage.getCreatedDate())
                    .lastUpdatedDate(testBreakage.getCreatedDate())
                    .build();

            BreakageFullDto breakageFullDto = BreakageFullDto.builder()
                    .id(testBreakage.getId())
                    .departmentId(testBreakage.getDepartment().getId())
                    .breakageExecutorId(null)
                    .departmentName(testBreakage.getDepartment().getName())
                    .room(testBreakage.getRoom())
                    .breakageTopic(testBreakage.getBreakageTopic())
                    .breakageText(testBreakage.getBreakageText())
                    .status(testBreakage.getStatus())
                    .priority(testBreakage.getPriority())
                    .breakageExecutor(null)
                    .executorAppointedBy(null)
                    .createdBy(testBreakage.getCreatedBy())
                    .createdDate(testBreakage.getCreatedDate())
                    .lastUpdatedBy(testBreakage.getCreatedBy())
                    .lastUpdatedDate(testBreakage.getCreatedDate())
                    .comments(List.of(comment))
                    .build();

            String responseMessage = "Заявка на неисправность с ID=" + testBreakage.getId() + ", получена успешно";

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(responseMessage)
                    .status(200)
                    .httpStatus(HttpStatus.OK)
                    .timestamp(now)
                    .data(breakageFullDto)
                    .build();

            when(breakageService.getBreakage(testBreakage.getId(), DEFAULT_ADMIN_USER_ID))
                    .thenReturn(apiResponse);

            String result = mockMvc.perform(MockMvcRequestBuilders.get(
                                    BASE_URL + BREAKAGE_URL +
                                            TECHNICIAN_URL + CURRENT_URL
                            )
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(CURRENT_USER_ID_HEADER, DEFAULT_ADMIN_USER_ID)
                            .header(BREAKAGE_ID_HEADER, testBreakage.getId()))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(200))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value(HttpStatus.OK.name()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(dtf.format(now)))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            LinkedHashMap<String, Object> dataMap = JsonPath.read(result, "$.data");
            BreakageFullDto returnedBreakageFullDto = objectMapper.convertValue(dataMap, BreakageFullDto.class);

            assertEquals(breakageFullDto, returnedBreakageFullDto);
            verify(breakageService, times(1))
                    .getBreakage(
                            testBreakage.getId(), DEFAULT_ADMIN_USER_ID
                    );
        }

        @Test
        @SneakyThrows
        void whenGetBreakageThenReturnNotFoundException() {

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(BREAKAGE_NOT_EXIST)
                    .status(404)
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .timestamp(now)
                    .build();

            when(breakageService.getBreakage(SOME_NOT_EXIST_ID, DEFAULT_ADMIN_USER_ID))
                    .thenReturn(apiResponse);

            String result = mockMvc.perform(MockMvcRequestBuilders.get(
                                    BASE_URL + BREAKAGE_URL +
                                            TECHNICIAN_URL + CURRENT_URL
                            )
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(CURRENT_USER_ID_HEADER, DEFAULT_ADMIN_USER_ID)
                            .header(BREAKAGE_ID_HEADER, SOME_NOT_EXIST_ID))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(BREAKAGE_NOT_EXIST))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(404))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value(HttpStatus.NOT_FOUND.name()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(dtf.format(now)))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertEquals(objectMapper.writeValueAsString(apiResponse), result);
            verify(breakageService, times(1))
                    .getBreakage(
                            SOME_NOT_EXIST_ID, DEFAULT_ADMIN_USER_ID
                    );
        }
    }



//    @Test
//    void createBreakageComment() {
//    }
//
//    @Test
//    void updateBreakageComment() {
//    }
//
//    @Test
//    void deleteBreakageComment() {
//    }
}