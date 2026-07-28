package ru.kraser.technical_helper.breakage_server.service.service_impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import ru.kraser.technical_helper.breakage_server.repository.BreakageRepository;
import ru.kraser.technical_helper.breakage_server.util.mapper.BreakageMapper;
import ru.kraser.technical_helper.common_module.dto.api.ApiResponse;
import ru.kraser.technical_helper.common_module.dto.breakage.CreateBreakageFullDto;
import ru.kraser.technical_helper.common_module.enums.Priority;
import ru.kraser.technical_helper.common_module.enums.Role;
import ru.kraser.technical_helper.common_module.enums.Status;
import ru.kraser.technical_helper.common_module.exception.ForbiddenException;
import ru.kraser.technical_helper.common_module.exception.NotFoundException;
import ru.kraser.technical_helper.common_module.model.Breakage;
import ru.kraser.technical_helper.common_module.model.Department;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static ru.kraser.technical_helper.common_module.util.Constant.BREAKAGE_NOT_EXIST;
import static ru.kraser.technical_helper.common_module.util.ConstantForTests.*;

@ExtendWith(MockitoExtension.class)
// @MockitoSettings(strictness = Strictness.LENIENT)  // If "NOW_ZDT" will get problem !!!
class BreakageServiceImplTest {

    @Mock
    private Clock clock;
    @Mock
    private BreakageRepository breakageRepository;
    @InjectMocks
    private BreakageServiceImpl breakageService;

    private Department testDepartment;
    // private User user;
    private Breakage testBreakage;
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

        testDepartment = Department.builder()
                .id(DEPARTMENT_TEST_ID)
                .name(DEPARTMENT_TEST_NAME)
                .enabled(true)
                .createdBy(DEFAULT_ADMIN_USER_ID)
                .createdDate(now)
                .lastUpdatedBy(DEFAULT_ADMIN_USER_ID)
                .lastUpdatedDate(now)
                .build();

        /*user = User.builder()
                .id(USER_TEST_ID)
                .username(USER_TEST_NAME)
                .password(USER_TEST_PASSWORD)
                .enabled(true)
                .role(Role.ADMIN)
                .department(department)
                .createdBy(DEFAULT_ADMIN_USER_ID)
                .createdDate(now)
                .lastUpdatedBy(DEFAULT_ADMIN_USER_ID)
                .lastUpdatedDate(now)
                .build();*/

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
                .createdBy(DEFAULT_ADMIN_USER_ID)
                .createdDate(now)
                .lastUpdatedBy(DEFAULT_ADMIN_USER_ID)
                .lastUpdatedDate(now)
                .build();
    }

    /*@AfterEach
    void tearDown() {
    }*/

    @Nested
    class WhenBreakageCreating {

        private CreateBreakageFullDto createBreakageFullDto;

        @BeforeEach
        void setUp() {

            createBreakageFullDto = new CreateBreakageFullDto(
                    testBreakage.getRoom(),
                    testBreakage.getBreakageTopic(),
                    testBreakage.getBreakageText(),
                    testDepartment
            );
        }

        @Test
        void whenCreateBreakageThenReturnCreated() {

            String responseMessage = "Заявка о неисправности с темой: " + createBreakageFullDto.breakageTopic() +
                    ", - была успешно создана.";

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(responseMessage)
                    .status(201)
                    .httpStatus(HttpStatus.CREATED)
                    .timestamp(now)
                    .build();

            when(breakageRepository.saveAndFlush(BreakageMapper.toBreakage(createBreakageFullDto, DEFAULT_ADMIN_USER_ID, now)))
                    .thenReturn(testBreakage);

            ApiResponse returnedApiResponse = breakageService.createBreakage(createBreakageFullDto, DEFAULT_ADMIN_USER_ID);

            assertEquals(apiResponse, returnedApiResponse);

            verify(breakageRepository, times(1))
                    .saveAndFlush(BreakageMapper.toBreakage(createBreakageFullDto, DEFAULT_ADMIN_USER_ID, now));
        }

        @Test
        void whenCreateBreakageThenReturnNotFoundException() {

            String responseMessage = "Отдел, за которым числится неисправность не существует !!!";

            when(breakageRepository.saveAndFlush(BreakageMapper.toBreakage(createBreakageFullDto, DEFAULT_ADMIN_USER_ID, now)))
                    .thenThrow(new NotFoundException(
                            "ОШИБКА: повторяющееся значение ключа нарушает ограничение уникальности " +
                                    "\"fk_breakage_department\""
                            )
                    );

            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> breakageService.createBreakage(createBreakageFullDto, DEFAULT_ADMIN_USER_ID)
            );

            assertEquals(responseMessage, exception.getMessage());

            verify(breakageRepository, times(1))
                    .saveAndFlush(BreakageMapper.toBreakage(createBreakageFullDto, DEFAULT_ADMIN_USER_ID, now));
        }
    }

    @Nested
    class WhenBreakageCancelling {

        @Test
        void whenCancelBreakageByTechnicianThenReturnOk() {

            String responseMessage = "Заявка на неисправность была успешно отменена.";

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(responseMessage)
                    .status(200)
                    .httpStatus(HttpStatus.OK)
                    .timestamp(now)
                    .data(USER_TEST_NAME)
                    .build();

            when(breakageRepository.updateBreakageStatus(
                    testBreakage.getId(), Status.CANCELLED, DEFAULT_ADMIN_USER_ID, now)
            ).thenReturn(1);

            ApiResponse returnedApiResponse = breakageService.cancelBreakage(
                    testBreakage.getId(), testBreakage.getDepartment().getId(), DEFAULT_ADMIN_USER_ID,
                    Role.TECHNICIAN, DEFAULT_ADMIN_DEPARTMENT_ID, USER_TEST_NAME
            );

            assertEquals(apiResponse, returnedApiResponse);

            verify(breakageRepository, times(1))
                    .updateBreakageStatus(testBreakage.getId(), Status.CANCELLED, DEFAULT_ADMIN_USER_ID, now);
        }

        @Test
        void whenCancelBreakageByEmployeeFromSameDepartmentThenReturnOk() {

            String responseMessage = "Заявка на неисправность была успешно отменена.";

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(responseMessage)
                    .status(200)
                    .httpStatus(HttpStatus.OK)
                    .timestamp(now)
                    .data(USER_TEST_NAME)
                    .build();

            when(breakageRepository.updateBreakageStatus(
                    testBreakage.getId(), Status.CANCELLED, DEFAULT_ADMIN_USER_ID, now)
            ).thenReturn(1);

            ApiResponse returnedApiResponse = breakageService.cancelBreakage(
                    testBreakage.getId(), testDepartment.getId(), DEFAULT_ADMIN_USER_ID,
                    Role.EMPLOYEE, testDepartment.getId(), USER_TEST_NAME
            );

            assertEquals(apiResponse, returnedApiResponse);

            verify(breakageRepository, times(1))
                    .updateBreakageStatus(testBreakage.getId(), Status.CANCELLED, DEFAULT_ADMIN_USER_ID, now);
        }

        @Test
        void whenCancelBreakageWhichNotExistThenReturnNotFoundException() {

            when(breakageRepository.updateBreakageStatus(
                    SOME_NOT_EXIST_ID, Status.CANCELLED, DEFAULT_ADMIN_USER_ID, now)
            ).thenThrow(new NotFoundException(BREAKAGE_NOT_EXIST));

            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> breakageService.cancelBreakage(SOME_NOT_EXIST_ID, testDepartment.getId(), DEFAULT_ADMIN_USER_ID,
                            Role.TECHNICIAN, DEFAULT_ADMIN_DEPARTMENT_ID,USER_TEST_NAME)
            );

            assertEquals(BREAKAGE_NOT_EXIST, exception.getMessage());

            verify(breakageRepository, times(1))
                    .updateBreakageStatus(SOME_NOT_EXIST_ID, Status.CANCELLED, DEFAULT_ADMIN_USER_ID, now);
        }

        @Test
        void whenCancelBreakageByEmployeeFromOtherDepartmentThenReturnForbiddenException() {

            String responseMessage = "Только технический специалист или сотрудник отдела, " +
                    "в котором произошла неисправность, могут отменить заявку !!!";

            ForbiddenException exception = assertThrows(
                    ForbiddenException.class,
                    () -> breakageService.cancelBreakage(testBreakage.getId(), testDepartment.getId(), DEFAULT_ADMIN_USER_ID,
                            Role.EMPLOYEE, SOME_NOT_EXIST_ID, USER_TEST_NAME)
            );

            assertEquals(responseMessage, exception.getMessage());

            verify(breakageRepository, times(0))
                    .updateBreakageStatus(testBreakage.getId(), Status.CANCELLED, DEFAULT_ADMIN_USER_ID, now);
        }
    }


//
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