package ru.kraser.technical_helper.breakage_server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import ru.kraser.technical_helper.common_module.dto.breakage.CreateBreakageFullDto;
import ru.kraser.technical_helper.common_module.enums.Priority;
import ru.kraser.technical_helper.common_module.enums.Role;
import ru.kraser.technical_helper.common_module.enums.Status;
import ru.kraser.technical_helper.common_module.model.Breakage;
import ru.kraser.technical_helper.common_module.model.Department;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
    }


//    @Test
//    void updateBreakageStatus() {
//    }
//
//    @Test
//    void updateBreakagePriority() {
//    }
//
//    @Test
//    void addBreakageExecutor() {
//    }
//
//    @Test
//    void dropBreakageExecutor() {
//    }
//
//    @Test
//    void getAllBreakages() {
//    }
//
//    @Test
//    void getBreakageEmployee() {
//    }
//
//    @Test
//    void getBreakage() {
//    }
//
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