package ru.kraser.technical_helper.main_server.service.service_impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import ru.kraser.technical_helper.common_module.dto.api.ApiResponse;
import ru.kraser.technical_helper.common_module.dto.department.CreateDepartmentDto;
import ru.kraser.technical_helper.common_module.dto.department.DepartmentDto;
import ru.kraser.technical_helper.common_module.exception.AlreadyExistsException;
import ru.kraser.technical_helper.common_module.exception.NotFoundException;
import ru.kraser.technical_helper.common_module.model.Department;
import ru.kraser.technical_helper.main_server.repository.DepartmentRepository;
import ru.kraser.technical_helper.main_server.util.mapper.DepartmentMapper;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static ru.kraser.technical_helper.common_module.util.Constant.DEPARTMENT_ID_HEADER;
import static ru.kraser.technical_helper.common_module.util.Constant.DEPARTMENT_NOT_EXIST;
import static ru.kraser.technical_helper.main_server.util.Constant.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DepartmentServiceImplTest {

    @Mock
    private Clock clock;
    @Mock
    private DepartmentRepository departmentRepository;
    @InjectMocks
    private DepartmentServiceImpl departmentService;

    private Department department;
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
                .id(DEPARTMENT_TEST_ID)
                .name(DEPARTMENT_TEST_NAME)
                .enabled(true)
                .createdBy(DEFAULT_ADMIN_USER_ID)
                .createdDate(now)
                .lastUpdatedBy(DEFAULT_ADMIN_USER_ID)
                .lastUpdatedDate(now)
                .build();
    }

    @Nested
    class WhenDepartmentCreating {

        private CreateDepartmentDto createDepartmentDto;

        @BeforeEach
        void setUp() {

            createDepartmentDto = new CreateDepartmentDto(department.getName());
        }

        @Test
        void whenCreateDepartmentThenReturnCreated() {

            String responseMessage = "Отдел: " + department.getName() + ", - был успешно создан.";

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(responseMessage)
                    .status(201)
                    .httpStatus(HttpStatus.CREATED)
                    .timestamp(now)
                    .build();

            when(departmentRepository.saveAndFlush(DepartmentMapper.toDepartment(createDepartmentDto, DEFAULT_ADMIN_USER_ID, now)))
                    .thenReturn(department);

            ApiResponse returnedApiResponse = departmentService.createDepartment(createDepartmentDto, DEFAULT_ADMIN_USER_ID);

            assertEquals(apiResponse, returnedApiResponse);

            verify(departmentRepository, times(1))
                    .saveAndFlush(DepartmentMapper.toDepartment(createDepartmentDto, DEFAULT_ADMIN_USER_ID, now));
        }

        @Test
        void whenCreateDepartmentWithNotUniqueNameThenThrowAlreadyExistsException() {

            String responseMessage = "Отдел: " + createDepartmentDto.name() + ", - уже существует. " +
                    "Используйте другое имя !!!";

            when(departmentRepository.saveAndFlush(
                    DepartmentMapper.toDepartment(createDepartmentDto, DEFAULT_ADMIN_USER_ID, now))
            ).thenThrow(new DataIntegrityViolationException(
                            "ОШИБКА: повторяющееся значение ключа нарушает ограничение уникальности \"uk_department_name\""
                    )
            );

            AlreadyExistsException exception = assertThrows(
                    AlreadyExistsException.class,
                    () -> departmentService.createDepartment(createDepartmentDto, DEFAULT_ADMIN_USER_ID)
            );

            assertEquals(responseMessage, exception.getMessage());

            verify(departmentRepository, times(1))
                    .saveAndFlush(DepartmentMapper.toDepartment(createDepartmentDto, DEFAULT_ADMIN_USER_ID, now));
        }
    }

    @Nested
    class WhenDepartmentUpdating {

        CreateDepartmentDto updateDepartmentDto;

        @BeforeEach
        void setUp() {

            updateDepartmentDto = new CreateDepartmentDto("updated_department_name");
        }

        @Test
        void whenUpdateDepartmentThenReturnOk() {

            String responseMessage = "Отдел: " + updateDepartmentDto.name() + " - был успешно изменен.";

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(responseMessage)
                    .status(200)
                    .httpStatus(HttpStatus.OK)
                    .timestamp(now)
                    .build();

            when(departmentRepository.updateDepartment(
                    department.getId(), updateDepartmentDto.name(), DEFAULT_ADMIN_USER_ID, now)
            ).thenReturn(1);

            ApiResponse returnedApiResponse = departmentService.updateDepartment(
                    department.getId(), updateDepartmentDto, DEFAULT_ADMIN_USER_ID);

            assertEquals(apiResponse, returnedApiResponse);

            verify(departmentRepository, times(1))
                    .updateDepartment(department.getId(), updateDepartmentDto.name(), DEFAULT_ADMIN_USER_ID, now);
        }

        @Test
        void whenUpdateDepartmentIfThisDepartmentNotExistThenThrowNotFoundException() {

            String responseMessage = "Данный отдел не существует !!!";

            when(departmentRepository.updateDepartment(
                    department.getId(), updateDepartmentDto.name(), DEFAULT_ADMIN_USER_ID, now)
            ).thenThrow(new NotFoundException(responseMessage));

            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> departmentService.updateDepartment(department.getId(), updateDepartmentDto, DEFAULT_ADMIN_USER_ID)
            );

            assertEquals(responseMessage, exception.getMessage());

            verify(departmentRepository, times(1))
                    .updateDepartment(department.getId(), updateDepartmentDto.name(), DEFAULT_ADMIN_USER_ID, now);
        }

        @Test
        void whenUpdateDepartmentIfNotUniqueNameThenThrowAlreadyExistsException() {

            String responseMessage = "Отдел: " + updateDepartmentDto.name() + ", - уже существует. " +
                    "Используйте другое имя !!!";

            when(departmentRepository.updateDepartment(
                    department.getId(), updateDepartmentDto.name(), DEFAULT_ADMIN_USER_ID, now)
            ).thenThrow(new DataIntegrityViolationException(
                            "ОШИБКА: повторяющееся значение ключа нарушает ограничение уникальности \"uk_department_name\""
                    )
            );

            AlreadyExistsException exception = assertThrows(
                    AlreadyExistsException.class,
                    () -> departmentService.updateDepartment(department.getId(), updateDepartmentDto, DEFAULT_ADMIN_USER_ID)
            );

            assertEquals(responseMessage, exception.getMessage());

            verify(departmentRepository, times(1))
                    .updateDepartment(department.getId(), updateDepartmentDto.name(), DEFAULT_ADMIN_USER_ID, now);
        }
    }

    @Nested
    class WhenGetAllDepartments {

        private DepartmentDto departmentDto;

        @BeforeEach
        void setUp() {

            departmentDto = DepartmentDto.builder()
                    .id(department.getId())
                    .name(department.getName())
                    .createdBy(department.createdBy)
                    .createdDate(department.createdDate)
                    .lastUpdatedBy(department.lastUpdatedBy)
                    .lastUpdatedDate(department.lastUpdatedDate)
                    .build();
        }

        @Test
        void whenGetAllDepartmentsThenReturnListOfDepartments() {

            when(departmentRepository.getAllDepartments()).thenReturn(List.of(departmentDto));

            List<DepartmentDto> returnedList = departmentRepository.getAllDepartments();

            assertEquals(1, returnedList.size());
            assertEquals(departmentDto.id(), returnedList.getFirst().id());
            assertEquals(departmentDto.name(), returnedList.getFirst().name());
            assertEquals(departmentDto.createdBy(), returnedList.getFirst().createdBy());
            assertEquals(departmentDto.createdDate(), returnedList.getFirst().createdDate());
            assertEquals(departmentDto.lastUpdatedBy(), returnedList.getFirst().lastUpdatedBy());
            assertEquals(departmentDto.lastUpdatedDate(), returnedList.getFirst().lastUpdatedDate());

            verify(departmentRepository, times(1)).getAllDepartments();
        }

        @Test
        void whenGetAllDepartmentsThenReturnEmptyList() {

            when(departmentRepository.getAllDepartments()).thenReturn(new ArrayList<>());

            List<DepartmentDto> returnedList = departmentRepository.getAllDepartments();

            assertEquals(0, returnedList.size());

            verify(departmentRepository, times(1)).getAllDepartments();
        }
    }

    @Nested
    class WhenGetDepartment {

        private DepartmentDto departmentDto;

        @BeforeEach
        void setUp() {

            departmentDto = DepartmentDto.builder()
                    .id(department.getId())
                    .name(department.getName())
                    .createdBy(department.createdBy)
                    .createdDate(department.createdDate)
                    .lastUpdatedBy(department.lastUpdatedBy)
                    .lastUpdatedDate(department.lastUpdatedDate)
                    .build();
        }

        @Test
        void whenGetDepartmentThenReturnDepartmentDto() {

            when(departmentRepository.getDepartmentById(department.getId())).thenReturn(Optional.of(departmentDto));

            DepartmentDto returnedDepartment = departmentService.getDepartment(DEPARTMENT_ID_HEADER, department.getId());

            assertEquals(departmentDto, returnedDepartment);

            verify(departmentRepository, times(1)).getDepartmentById(department.getId());
        }

        @Test
        void whenGetDepartmentIfThisDepartmentNotExistThenThrowNotFoundException() {

            when(departmentRepository.getDepartmentById(department.getId()))
                    .thenThrow(new NotFoundException(DEPARTMENT_NOT_EXIST));

            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> departmentService.getDepartment(DEPARTMENT_ID_HEADER, department.getId())
            );

            assertEquals(DEPARTMENT_NOT_EXIST, exception.getMessage());

            verify(departmentRepository, times(1))
                    .getDepartmentById(department.getId());
        }
    }

    @Nested
    class WhenDepartmentDeleting {

        @Test
        void whenDeleteDepartmentThenReturnOk() {

            String responseMessage = "Отдел - был успешно удалён.";

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(responseMessage)
                    .status(200)
                    .httpStatus(HttpStatus.OK)
                    .timestamp(now)
                    .build();

            when(departmentRepository.deleteDepartment(
                    department.getId(), DEFAULT_ADMIN_USER_ID, now)
            ).thenReturn(1);

            ApiResponse returnedApiResponse = departmentService.deleteDepartment(
                    department.getId(), DEFAULT_ADMIN_USER_ID);

            assertEquals(apiResponse, returnedApiResponse);

            verify(departmentRepository, times(1))
                    .deleteDepartment(department.getId(), DEFAULT_ADMIN_USER_ID, now);
        }

        @Test
        void whenDeleteDepartmentIfThisDepartmentNotExistThenThrowNotFoundException() {

            when(departmentRepository.deleteDepartment(
                    department.getId(), DEFAULT_ADMIN_USER_ID, now)
            ).thenThrow(new NotFoundException(DEPARTMENT_NOT_EXIST));

            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> departmentService.deleteDepartment(department.getId(), DEFAULT_ADMIN_USER_ID)
            );

            assertEquals(DEPARTMENT_NOT_EXIST, exception.getMessage());

            verify(departmentRepository, times(1))
                    .deleteDepartment(department.getId(), DEFAULT_ADMIN_USER_ID, now);
        }
    }
}