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
import ru.kraser.technical_helper.common_module.dto.department.CreateDepartmentDto;
import ru.kraser.technical_helper.common_module.dto.department.DepartmentDto;
import ru.kraser.technical_helper.common_module.model.Department;
import ru.kraser.technical_helper.main_server.service.DepartmentService;

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

@WebMvcTest(controllers = DepartmentController.class)
class DepartmentControllerWebMvcTest {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private DepartmentService departmentService;
    @MockitoBean
    private Clock clock;

    private Department department;
    private CreateDepartmentDto createDepartmentDto;
    private DateTimeFormatter dtf;
    private LocalDateTime now;
    private String currentUserId = "u1u11111-11u1-1u11-1111-u111111u1111";

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

        department = Department.builder()
                .id("d1d11111-11d1-1d11-1111-d111111d1111")
                .name("test_department")
                .enabled(true)
                .createdBy(currentUserId)
                .createdDate(now)
                .lastUpdatedBy(currentUserId)
                .lastUpdatedDate(now)
                .build();

        createDepartmentDto = new CreateDepartmentDto(department.getName());

    }

    @Nested
    class WhenDepartmentCreating {

        @SneakyThrows
        @Test
        void whenCreateDepartmentThenReturnCreated() {

            when(clock.getZone()).thenReturn(NOW_ZDT.getZone());
            when(clock.instant()).thenReturn(NOW_ZDT.toInstant());

            String responseMessage = "Отдел: " + createDepartmentDto.name() + ", - был успешно создан.";

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(responseMessage)
                    .status(201)
                    .httpStatus(HttpStatus.CREATED)
                    .timestamp(now)
                    .build();

            when(departmentService.createDepartment(createDepartmentDto, currentUserId)).thenReturn(apiResponse);

            String result = mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + ADMIN_URL + DEPARTMENT_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(CURRENT_USER_ID_HEADER, currentUserId)
                            .content(objectMapper.writeValueAsString(createDepartmentDto)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(201))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value("CREATED"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(dtf.format(now)))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertEquals(objectMapper.writeValueAsString(apiResponse), result);
            verify(departmentService, times(1)).createDepartment(createDepartmentDto, currentUserId);
        }

        @SneakyThrows
        @Test
        void whenCreateDepartmentThenReturnUnprocessableEntity() {

            when(clock.getZone()).thenReturn(NOW_ZDT.getZone());
            when(clock.instant()).thenReturn(NOW_ZDT.toInstant());

            String responseMessage = "Отдел: " + createDepartmentDto.name() + ", - уже существует. " +
                    "Используйте другое имя !!!";

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(responseMessage)
                    .status(422)
                    .httpStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                    .timestamp(now)
                    .build();

            when(departmentService.createDepartment(createDepartmentDto, currentUserId)).thenReturn(apiResponse);

            String result = mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + ADMIN_URL + DEPARTMENT_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(CURRENT_USER_ID_HEADER, currentUserId)
                            .content(objectMapper.writeValueAsString(createDepartmentDto)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                            .value(responseMessage))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status")
                            .value(422))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus")
                            .value("UNPROCESSABLE_ENTITY"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp")
                            .value(dtf.format(now)))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertEquals(objectMapper.writeValueAsString(apiResponse), result);
            verify(departmentService, times(1)).createDepartment(createDepartmentDto, currentUserId);
        }
    }



//    @Test
//    void getAllDepartments() {
//    }
//
//    @Test
//    void getDepartmentById() {
//    }
//
//    @Test
//    void deleteDepartment() {
//    }
}