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
import static ru.kraser.technical_helper.main_server.util.Constant.*;

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
                .id(DEPARTMENT_ID)
                .name(DEPARTMENT_TEST_NAME)
                .enabled(true)
                .createdBy(USER_ID)
                .createdDate(now)
                .lastUpdatedBy(USER_ID)
                .lastUpdatedDate(now)
                .build();

        createDepartmentDto = new CreateDepartmentDto(department.getName());
    }


    @Nested
    class WhenDepartmentCreating {

        @Test
        void whenCreateDepartmentThenReturnCreated() {

            String responseMessage = "Отдел: " + createDepartmentDto.name() + ", - был успешно создан.";

            ApiResponse response = ApiResponse.builder()
                    .message(responseMessage)
                    .status(201)
                    .httpStatus(HttpStatus.CREATED)
                    .timestamp(now)
                    .build();

            when(departmentService.createDepartment(createDepartmentDto, USER_ID)).thenReturn(response);

            ApiResponse apiResponse = departmentController.createDepartment(
                    USER_ID, createDepartmentDto);

            assertEquals(responseMessage, apiResponse.message());
            assertEquals(201, apiResponse.status());
            assertEquals(HttpStatus.CREATED, apiResponse.httpStatus());
            assertEquals(now, apiResponse.timestamp());

            verify(departmentService, times(1))
                    .createDepartment(createDepartmentDto, USER_ID);
        }

        @Test
        void whenCreateDepartmentThenReturnUnprocessableEntity() {

            String responseMessage = "Отдел: " + createDepartmentDto.name() + ", - уже существует. " +
                    "Используйте другое имя !!!";

            ApiResponse response = ApiResponse.builder()
                    .message(responseMessage)
                    .status(422)
                    .httpStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                    .timestamp(now)
                    .build();

            when(departmentService.createDepartment(createDepartmentDto, USER_ID)).thenReturn(response);

            ApiResponse apiResponse = departmentController.createDepartment(
                    USER_ID, createDepartmentDto);

            assertEquals(responseMessage, apiResponse.message());
            assertEquals(422, apiResponse.status());
            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, apiResponse.httpStatus());
            assertEquals(now, apiResponse.timestamp());

            verify(departmentService, times(1))
                    .createDepartment(createDepartmentDto, USER_ID);
        }
    }

    @Nested
    class WhenDepartmentUpdating {

        @Test
        void whenUpdateDepartmentNameThenReturnOk() {

            String responseMessage = "Отдел: " + createDepartmentDto.name() + ", - был успешно изменен.";

            when(departmentService.updateDepartment(department.getId(), createDepartmentDto, USER_ID))
                    .thenReturn(ApiResponse.builder()
                            .message(responseMessage)
                            .status(200)
                            .httpStatus(HttpStatus.OK)
                            .timestamp(now)
                            .build());

            ApiResponse apiResponse = departmentController.updateDepartment(
                    department.getId(), USER_ID, createDepartmentDto);

            assertEquals(responseMessage, apiResponse.message());
            assertEquals(200, apiResponse.status());
            assertEquals(HttpStatus.OK, apiResponse.httpStatus());
            assertEquals(now, apiResponse.timestamp());

            verify(departmentService, times(1))
                    .updateDepartment(department.getId(), createDepartmentDto, USER_ID);

        }

        @Test
        void whenUpdateDepartmentThenReturnUnprocessableEntity() {

            String responseMessage = "Отдел: " + createDepartmentDto.name() + ", - уже существует. " +
                    "Используйте другое имя !!!";

            when(departmentService.updateDepartment(department.getId(), createDepartmentDto, USER_ID))
                    .thenReturn(ApiResponse.builder()
                            .message(responseMessage)
                            .status(422)
                            .httpStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                            .timestamp(now)
                            .build());

            ApiResponse apiResponse = departmentController.updateDepartment(
                    department.getId(), USER_ID, createDepartmentDto);

            assertEquals(responseMessage, apiResponse.message());
            assertEquals(422, apiResponse.status());
            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, apiResponse.httpStatus());
            assertEquals(now, apiResponse.timestamp());

            verify(departmentService, times(1))
                    .updateDepartment(department.getId(), createDepartmentDto, USER_ID);
        }

        @Test
        void whenUpdateDepartmentThenReturnNotFound() {

            String responseMessage = "Данный отдел не существует !!!";

            when(departmentService.updateDepartment(department.getId(), createDepartmentDto, USER_ID))
                    .thenReturn(ApiResponse.builder()
                            .message(responseMessage)
                            .status(404)
                            .httpStatus(HttpStatus.NOT_FOUND)
                            .timestamp(now)
                            .build());

            ApiResponse apiResponse = departmentController.updateDepartment(
                    department.getId(), USER_ID, createDepartmentDto);

            assertEquals(responseMessage, apiResponse.message());
            assertEquals(404, apiResponse.status());
            assertEquals(HttpStatus.NOT_FOUND, apiResponse.httpStatus());
            assertEquals(now, apiResponse.timestamp());

            verify(departmentService, times(1))
                    .updateDepartment(department.getId(), createDepartmentDto, USER_ID);
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

        @Test
        void whenGetAllDepartmentsThenReturnDepartmentDtoList() {

            List<DepartmentDto> expectedDepartmentDtoList = List.of(expectedDepartmentDto);

            when(departmentService.getAllDepartments()).thenReturn(expectedDepartmentDtoList);

            List<DepartmentDto> departmentDtoList = departmentService.getAllDepartments();

            assertEquals(expectedDepartmentDtoList, departmentDtoList);

            verify(departmentService, times(1)).getAllDepartments();
        }

        @Test
        void whenGetDepartmentByIdThenReturnDepartmentDto() {

            when(departmentService.getDepartment(
                    DEPARTMENT_ID_HEADER,
                    department.getId())
            ).thenReturn(expectedDepartmentDto);

            DepartmentDto departmentDto = departmentService.getDepartment(
                    DEPARTMENT_ID_HEADER, department.getId()
            );

            assertEquals(expectedDepartmentDto, departmentDto);

            verify(departmentService, times(1)).getDepartment(
                    DEPARTMENT_ID_HEADER, department.getId()
            );
        }
    }

    @Test
    void whenDeleteDepartmentThenReturnOk() {

        String responseMessage = "Отдел - был успешно удалён.";

        when(departmentService.deleteDepartment(department.getId(), USER_ID))
                .thenReturn(ApiResponse.builder()
                        .message(responseMessage)
                        .status(200)
                        .httpStatus(HttpStatus.OK)
                        .timestamp(now)
                        .build());

        ApiResponse apiResponse = departmentController.deleteDepartment(
                USER_ID, department.getId());

        assertEquals(responseMessage, apiResponse.message());
        assertEquals(200, apiResponse.status());
        assertEquals(HttpStatus.OK, apiResponse.httpStatus());
        assertEquals(now, apiResponse.timestamp());

        verify(departmentService, times(1))
                .deleteDepartment(department.getId(), USER_ID);
    }
}