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
import ru.kraser.technical_helper.common_module.dto.department.CreateDepartmentDto;
import ru.kraser.technical_helper.common_module.dto.department.DepartmentDto;
import ru.kraser.technical_helper.common_module.model.Department;
import ru.kraser.technical_helper.main_server.service.DepartmentService;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import static org.mockito.Mockito.when;
import static ru.kraser.technical_helper.common_module.util.Constant.DEPARTMENT_ID_HEADER;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)

class DepartmentControllerTest {

    @Mock
    private Clock clock;
    @Mock
    private DepartmentService departmentService;
    @InjectMocks
    private DepartmentController departmentController;

    private Department department;
    private CreateDepartmentDto createDepartmentDto;
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

        @Test
        void whenCreateDepartmentThenReturnCreated() {

            when(clock.getZone()).thenReturn(NOW_ZDT.getZone());
            when(clock.instant()).thenReturn(NOW_ZDT.toInstant());

            String responseMessage = "Отдел: " + createDepartmentDto.name() + ", - был успешно создан.";

            ApiResponse response = ApiResponse.builder()
                    .message(responseMessage)
                    .status(201)
                    .httpStatus(HttpStatus.CREATED)
                    .timestamp(now)
                    .build();

            when(departmentService.createDepartment(createDepartmentDto, currentUserId)).thenReturn(response);

            ApiResponse apiResponse = departmentController.createDepartment(
                    currentUserId, createDepartmentDto);

            assertEquals(responseMessage, apiResponse.message());
            assertEquals(201, apiResponse.status());
            assertEquals(HttpStatus.CREATED, apiResponse.httpStatus());
            assertEquals(now, apiResponse.timestamp());

            verify(departmentService, times(1))
                    .createDepartment(createDepartmentDto, currentUserId);
        }

        @Test
        void whenCreateDepartmentThenReturnUnprocessableEntity() {

            when(clock.getZone()).thenReturn(NOW_ZDT.getZone());
            when(clock.instant()).thenReturn(NOW_ZDT.toInstant());

            String responseMessage = "Отдел: " + createDepartmentDto.name() + ", - уже существует. " +
                    "Используйте другое имя !!!";

            ApiResponse response = ApiResponse.builder()
                    .message(responseMessage)
                    .status(422)
                    .httpStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                    .timestamp(now)
                    .build();

            when(departmentService.createDepartment(createDepartmentDto, currentUserId)).thenReturn(response);

            ApiResponse apiResponse = departmentController.createDepartment(
                    currentUserId, createDepartmentDto);

            assertEquals(responseMessage, apiResponse.message());
            assertEquals(422, apiResponse.status());
            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, apiResponse.httpStatus());
            assertEquals(now, apiResponse.timestamp());

            verify(departmentService, times(1))
                    .createDepartment(createDepartmentDto, currentUserId);
        }
    }

    @Nested
    class WhenDepartmentUpdating {

        @Test
        void whenUpdateDepartmentNameThenReturnOk() {

            when(clock.getZone()).thenReturn(NOW_ZDT.getZone());
            when(clock.instant()).thenReturn(NOW_ZDT.toInstant());

            String responseMessage = "Отдел: " + createDepartmentDto.name() + ", - был успешно изменен.";

            when(departmentService.updateDepartment(department.getId(), createDepartmentDto, currentUserId))
                    .thenReturn(ApiResponse.builder()
                            .message(responseMessage)
                            .status(200)
                            .httpStatus(HttpStatus.OK)
                            .timestamp(now)
                            .build());

            ApiResponse apiResponse = departmentController.updateDepartment(
                    department.getId(), currentUserId, createDepartmentDto);

            assertEquals(responseMessage, apiResponse.message());
            assertEquals(200, apiResponse.status());
            assertEquals(HttpStatus.OK, apiResponse.httpStatus());
            assertEquals(now, apiResponse.timestamp());

            verify(departmentService, times(1))
                    .updateDepartment(department.getId(), createDepartmentDto, currentUserId);

        }

        @Test
        void whenUpdateDepartmentThenReturnUnprocessableEntity() {

            when(clock.getZone()).thenReturn(NOW_ZDT.getZone());
            when(clock.instant()).thenReturn(NOW_ZDT.toInstant());

            String responseMessage = "Отдел: " + createDepartmentDto.name() + ", - уже существует. " +
                    "Используйте другое имя !!!";

            when(departmentService.updateDepartment(department.getId(), createDepartmentDto, currentUserId))
                    .thenReturn(ApiResponse.builder()
                            .message(responseMessage)
                            .status(422)
                            .httpStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                            .timestamp(now)
                            .build());

            ApiResponse apiResponse = departmentController.updateDepartment(
                    department.getId(), currentUserId, createDepartmentDto);

            assertEquals(responseMessage, apiResponse.message());
            assertEquals(422, apiResponse.status());
            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, apiResponse.httpStatus());
            assertEquals(now, apiResponse.timestamp());

            verify(departmentService, times(1))
                    .updateDepartment(department.getId(), createDepartmentDto, currentUserId);
        }

        @Test
        void whenUpdateDepartmentThenReturnNotFound() {

            when(clock.getZone()).thenReturn(NOW_ZDT.getZone());
            when(clock.instant()).thenReturn(NOW_ZDT.toInstant());

            String responseMessage = "Данный отдел не существует !!!";

            when(departmentService.updateDepartment(department.getId(), createDepartmentDto, currentUserId))
                    .thenReturn(ApiResponse.builder()
                            .message(responseMessage)
                            .status(404)
                            .httpStatus(HttpStatus.NOT_FOUND)
                            .timestamp(now)
                            .build());

            ApiResponse apiResponse = departmentController.updateDepartment(
                    department.getId(), currentUserId, createDepartmentDto);

            assertEquals(responseMessage, apiResponse.message());
            assertEquals(404, apiResponse.status());
            assertEquals(HttpStatus.NOT_FOUND, apiResponse.httpStatus());
            assertEquals(now, apiResponse.timestamp());

            verify(departmentService, times(1))
                    .updateDepartment(department.getId(), createDepartmentDto, currentUserId);
        }
    }

    @Test
    void getAllDepartments() {
    }

    @Test
    void getDepartmentById() {
    }

    @Test
    void deleteDepartment() {
    }
}