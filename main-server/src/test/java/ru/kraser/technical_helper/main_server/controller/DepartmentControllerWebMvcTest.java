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
import static ru.kraser.technical_helper.main_server.util.Constant.*;

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

        department = Department.builder()
                .id(DEPARTMENT_TEST_ID)
                .name(DEPARTMENT_TEST_NAME)
                .enabled(true)
                .createdBy(DEFAULT_ADMIN_USER_ID)
                .createdDate(now)
                .lastUpdatedBy(DEFAULT_ADMIN_USER_ID)
                .lastUpdatedDate(now)
                .build();

        createDepartmentDto = new CreateDepartmentDto(department.getName());

    }

    @Nested
    class WhenDepartmentCreating {

        @SneakyThrows
        @Test
        void whenCreateDepartmentThenReturnCreated() {

            String responseMessage = "Отдел: " + createDepartmentDto.name() + ", - был успешно создан.";

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(responseMessage)
                    .status(201)
                    .httpStatus(HttpStatus.CREATED)
                    .timestamp(now)
                    .build();

            when(departmentService.createDepartment(createDepartmentDto, DEFAULT_ADMIN_USER_ID)).thenReturn(apiResponse);

            String result = mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + ADMIN_URL + DEPARTMENT_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(CURRENT_USER_ID_HEADER, DEFAULT_ADMIN_USER_ID)
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
            verify(departmentService, times(1)).createDepartment(createDepartmentDto, DEFAULT_ADMIN_USER_ID);
        }

        @SneakyThrows
        @Test
        void whenCreateDepartmentThenReturnUnprocessableEntity() {

            String responseMessage = "Отдел: " + createDepartmentDto.name() + ", - уже существует. " +
                    "Используйте другое имя !!!";

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(responseMessage)
                    .status(422)
                    .httpStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                    .timestamp(now)
                    .build();

            when(departmentService.createDepartment(createDepartmentDto, DEFAULT_ADMIN_USER_ID)).thenReturn(apiResponse);

            String result = mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + ADMIN_URL + DEPARTMENT_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(CURRENT_USER_ID_HEADER, DEFAULT_ADMIN_USER_ID)
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
            verify(departmentService, times(1)).createDepartment(createDepartmentDto, DEFAULT_ADMIN_USER_ID);
        }
    }

    @Nested
    class WhenDepartmentUpdating {

        @SneakyThrows
        @Test
        void whenUpdateDepartmentThenReturnOk() {

            String responseMessage = "Отдел: " + createDepartmentDto.name() + ", - был успешно изменен.";

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(responseMessage)
                    .status(200)
                    .httpStatus(HttpStatus.OK)
                    .timestamp(now)
                    .build();

            when(departmentService.updateDepartment(
                    department.getId(), createDepartmentDto, DEFAULT_ADMIN_USER_ID)
            ).thenReturn(apiResponse);

            String result = mockMvc.perform(MockMvcRequestBuilders.patch(BASE_URL + ADMIN_URL + DEPARTMENT_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(CURRENT_USER_ID_HEADER, DEFAULT_ADMIN_USER_ID)
                            .header(DEPARTMENT_ID_HEADER, department.getId())
                            .content(objectMapper.writeValueAsString(createDepartmentDto)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(200))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value("OK"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(dtf.format(now)))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertEquals(objectMapper.writeValueAsString(apiResponse), result);
            verify(departmentService, times(1))
                    .updateDepartment(department.getId(), createDepartmentDto, DEFAULT_ADMIN_USER_ID);
        }

        @SneakyThrows
        @Test
        void whenUpdateDepartmentThenReturnUnprocessableEntity() {

            String responseMessage = "Отдел: " + createDepartmentDto.name() + ", - уже существует. " +
                    "Используйте другое имя !!!";

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(responseMessage)
                    .status(422)
                    .httpStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                    .timestamp(now)
                    .build();

            when(departmentService.updateDepartment(
                    department.getId(), createDepartmentDto, DEFAULT_ADMIN_USER_ID)
            ).thenReturn(apiResponse);

            String result = mockMvc.perform(MockMvcRequestBuilders.patch(BASE_URL + ADMIN_URL + DEPARTMENT_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(CURRENT_USER_ID_HEADER, DEFAULT_ADMIN_USER_ID)
                            .header(DEPARTMENT_ID_HEADER, department.getId())
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
            verify(departmentService, times(1))
                    .updateDepartment(department.getId(), createDepartmentDto, DEFAULT_ADMIN_USER_ID);
        }

        @SneakyThrows
        @Test
        void whenUpdateDepartmentThenReturnNotFound() {

            String responseMessage = "Данный отдел не существует !!!";

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(responseMessage)
                    .status(404)
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .timestamp(now)
                    .build();

            when(departmentService.updateDepartment(
                    department.getId(), createDepartmentDto, DEFAULT_ADMIN_USER_ID)
            ).thenReturn(apiResponse);

            String result = mockMvc.perform(MockMvcRequestBuilders.patch(BASE_URL + ADMIN_URL + DEPARTMENT_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(CURRENT_USER_ID_HEADER, DEFAULT_ADMIN_USER_ID)
                            .header(DEPARTMENT_ID_HEADER, department.getId())
                            .content(objectMapper.writeValueAsString(createDepartmentDto)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                            .value(responseMessage))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status")
                            .value(404))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus")
                            .value("NOT_FOUND"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp")
                            .value(dtf.format(now)))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertEquals(objectMapper.writeValueAsString(apiResponse), result);
            verify(departmentService, times(1))
                    .updateDepartment(department.getId(), createDepartmentDto, DEFAULT_ADMIN_USER_ID);
        }
    }

    @Nested
    class WhenGetMethodsAreExecuting {

        DepartmentDto expectedDepartmentDto;

        @BeforeEach
        void initializeDepartmentDto() {
            expectedDepartmentDto = DepartmentDto.builder()
                    .id(department.getId())
                    .name(department.getName())
                    .createdBy(department.getCreatedBy())
                    .createdDate(department.getCreatedDate())
                    .lastUpdatedBy(department.getLastUpdatedBy())
                    .lastUpdatedDate(department.getLastUpdatedDate())
                    .build();
        }

        @SneakyThrows
        @Test
        void whenGetAllDepartmentsThenReturnDepartmentDtoList() {

            when(departmentService.getAllDepartments()).thenReturn(List.of(expectedDepartmentDto));

            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + ADMIN_URL + DEPARTMENT_URL + ALL_URL)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.size()", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].id")
                            .value(expectedDepartmentDto.id()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].name")
                            .value(expectedDepartmentDto.name()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].createdBy")
                            .value(expectedDepartmentDto.createdBy()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].createdDate")
                            .value(dtf.format(expectedDepartmentDto.createdDate())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].lastUpdatedBy")
                            .value(expectedDepartmentDto.lastUpdatedBy()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].lastUpdatedDate")
                            .value(dtf.format(expectedDepartmentDto.lastUpdatedDate())));

            verify(departmentService ,times(1)).getAllDepartments();
        }

        @SneakyThrows
        @Test
        void whenGetDepartmentByIdThenReturnDepartmentDto() {

            when(departmentService.getDepartment(DEPARTMENT_ID_HEADER, expectedDepartmentDto.id()))
                    .thenReturn(expectedDepartmentDto);

            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + ADMIN_URL + DEPARTMENT_URL + CURRENT_URL)
                            .accept(MediaType.APPLICATION_JSON)
                            .header(DEPARTMENT_ID_HEADER, expectedDepartmentDto.id()))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id")
                            .value(expectedDepartmentDto.id()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.name")
                            .value(expectedDepartmentDto.name()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.createdBy")
                            .value(expectedDepartmentDto.createdBy()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.createdDate")
                            .value(dtf.format(expectedDepartmentDto.createdDate())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.lastUpdatedBy")
                            .value(expectedDepartmentDto.lastUpdatedBy()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.lastUpdatedDate")
                            .value(dtf.format(expectedDepartmentDto.lastUpdatedDate())));

            verify(departmentService ,times(1))
                    .getDepartment(DEPARTMENT_ID_HEADER, expectedDepartmentDto.id());
        }
    }

    @SneakyThrows
    @Test
    void whenDeleteDepartmentThenReturnOk() {

        String responseMessage = "Отдел - был успешно удалён.";

        ApiResponse apiResponse = ApiResponse.builder()
                .message(responseMessage)
                .status(200)
                .httpStatus(HttpStatus.OK)
                .timestamp(now)
                .build();

        when(departmentService.deleteDepartment(
                department.getId(), DEFAULT_ADMIN_USER_ID)
        ).thenReturn(apiResponse);

        String result = mockMvc.perform(MockMvcRequestBuilders.patch(
                                BASE_URL + ADMIN_URL + DEPARTMENT_URL + DELETE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(CURRENT_USER_ID_HEADER, DEFAULT_ADMIN_USER_ID)
                        .header(DEPARTMENT_ID_HEADER, department.getId()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value("OK"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(dtf.format(now)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(apiResponse), result);
        verify(departmentService, times(1))
                .deleteDepartment(department.getId(), DEFAULT_ADMIN_USER_ID);
    }

}