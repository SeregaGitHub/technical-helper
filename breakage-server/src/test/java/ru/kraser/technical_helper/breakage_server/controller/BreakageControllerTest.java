package ru.kraser.technical_helper.breakage_server.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import ru.kraser.technical_helper.breakage_server.service.BreakageService;
import ru.kraser.technical_helper.common_module.dto.api.ApiResponse;
import ru.kraser.technical_helper.common_module.dto.breakage.AppointBreakageExecutorDto;
import ru.kraser.technical_helper.common_module.dto.breakage.CreateBreakageFullDto;
import ru.kraser.technical_helper.common_module.dto.breakage.UpdateBreakagePriorityDto;
import ru.kraser.technical_helper.common_module.dto.breakage.UpdateBreakageStatusDto;
import ru.kraser.technical_helper.common_module.enums.Priority;
import ru.kraser.technical_helper.common_module.enums.Role;
import ru.kraser.technical_helper.common_module.enums.Status;
import ru.kraser.technical_helper.common_module.exception.NotFoundException;
import ru.kraser.technical_helper.common_module.model.Breakage;
import ru.kraser.technical_helper.common_module.model.Department;

import java.time.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ru.kraser.technical_helper.common_module.util.Constant.BREAKAGE_NOT_EXIST;
import static ru.kraser.technical_helper.common_module.util.ConstantForTests.*;

@ExtendWith(MockitoExtension.class)
class BreakageControllerTest {

    //    @Mock
//    private Clock clock;
    @Mock
    private BreakageService breakageService;
    @InjectMocks
    private BreakageController breakageController;

    private LocalDateTime now;
    private Department testDepartment;
    private Breakage testBreakage;

    /*private static final ZonedDateTime NOW_ZDT = ZonedDateTime.of(
            2025,
            9,
            29,
            13,
            0,
            0,
            0,
            ZoneId.of("UTC")
    );*/

    @BeforeEach
    void setUp() {

        now = LocalDateTime.of(
                2025,
                9,
                29,
                13,
                0,
                0);

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

//        when(clock.getZone()).thenReturn(NOW_ZDT.getZone());
//        when(clock.instant()).thenReturn(NOW_ZDT.toInstant());
    }


//
//    @AfterEach
//    void tearDown() {
//    }

    @Nested
    class WhenBreakageCreating {

        private CreateBreakageFullDto createBreakageFullDto;

        @BeforeEach
        void setUp() {

            createBreakageFullDto = CreateBreakageFullDto.builder()
                    .department(testDepartment)
                    .room("some_room")
                    .breakageTopic("test_breakage_topic")
                    .breakageText("test_breakage_text")
                    .build();
        }

        @Test
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

            ApiResponse apiResponse = breakageController.createBreakage(USER_TEST_ID, createBreakageFullDto);

            assertEquals(responseMessage, apiResponse.message());
            assertEquals(201, apiResponse.status());
            assertEquals(HttpStatus.CREATED, apiResponse.httpStatus());
            assertEquals(now, apiResponse.timestamp());

            verify(breakageService, times(1))
                    .createBreakage(createBreakageFullDto, USER_TEST_ID);
        }

        @Test
        void whenCreateBreakageThenReturnNotFoundException() {

            String responseMessage = "Отдел, за которым числится неисправность не существует !!!";

            ApiResponse response = ApiResponse.builder()
                    .message(responseMessage)
                    .status(404)
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .timestamp(now)
                    .build();

            when(breakageService.createBreakage(createBreakageFullDto, USER_TEST_ID)).thenReturn(response);

            ApiResponse apiResponse = breakageController.createBreakage(USER_TEST_ID, createBreakageFullDto);

            assertEquals(responseMessage, apiResponse.message());
            assertEquals(404, apiResponse.status());
            assertEquals(HttpStatus.NOT_FOUND, apiResponse.httpStatus());
            assertEquals(now, apiResponse.timestamp());

            verify(breakageService, times(1))
                    .createBreakage(createBreakageFullDto, USER_TEST_ID);
        }
    }

    @Nested
    class WhenBreakageCancelling {

        @Test
        void whenCancelBreakageByTechnicianThenReturnOk() {

            String responseMessage = "Заявка на неисправность была успешно отменена.";

            ApiResponse response = ApiResponse.builder()
                    .message(responseMessage)
                    .status(200)
                    .httpStatus(HttpStatus.OK)
                    .timestamp(now)
                    .data(DEFAULT_ADMIN_USERNAME)
                    .build();

            when(breakageService.cancelBreakage(
                            testBreakage.getId(), testBreakage.getDepartment().getId(), DEFAULT_ADMIN_USER_ID,
                            Role.ADMIN, DEFAULT_ADMIN_DEPARTMENT_ID, DEFAULT_ADMIN_USERNAME
                    )
            ).thenReturn(response);

            ApiResponse apiResponse = breakageController.cancelBreakage(
                    DEFAULT_ADMIN_USER_ID, testBreakage.getId(), testBreakage.getDepartment().getId(),
                    Role.ADMIN, DEFAULT_ADMIN_DEPARTMENT_ID, DEFAULT_ADMIN_USERNAME
            );

            assertEquals(responseMessage, apiResponse.message());
            assertEquals(200, apiResponse.status());
            assertEquals(HttpStatus.OK, apiResponse.httpStatus());
            assertEquals(now, apiResponse.timestamp());
            assertEquals(DEFAULT_ADMIN_USERNAME, apiResponse.data());

            verify(breakageService, times(1))
                    .cancelBreakage(
                            testBreakage.getId(), testBreakage.getDepartment().getId(), DEFAULT_ADMIN_USER_ID,
                            Role.ADMIN, DEFAULT_ADMIN_DEPARTMENT_ID, DEFAULT_ADMIN_USERNAME
                    );
        }

        @Test
        void whenCancelBreakageByEmployeeFromSameDepartmentThenReturnOk() {

            String responseMessage = "Заявка на неисправность была успешно отменена.";

            ApiResponse response = ApiResponse.builder()
                    .message(responseMessage)
                    .status(200)
                    .httpStatus(HttpStatus.OK)
                    .timestamp(now)
                    .data(USER_TEST_NAME)
                    .build();

            when(breakageService.cancelBreakage(
                            testBreakage.getId(), testBreakage.getDepartment().getId(), USER_TEST_ID,
                            Role.EMPLOYEE, testBreakage.getDepartment().getId(), USER_TEST_NAME
                    )
            ).thenReturn(response);

            ApiResponse apiResponse = breakageController.cancelBreakage(
                    USER_TEST_ID, testBreakage.getId(), testBreakage.getDepartment().getId(),
                    Role.EMPLOYEE, testBreakage.getDepartment().getId(), USER_TEST_NAME
            );

            assertEquals(responseMessage, apiResponse.message());
            assertEquals(200, apiResponse.status());
            assertEquals(HttpStatus.OK, apiResponse.httpStatus());
            assertEquals(now, apiResponse.timestamp());
            assertEquals(USER_TEST_NAME, apiResponse.data());

            verify(breakageService, times(1))
                    .cancelBreakage(
                            testBreakage.getId(), testBreakage.getDepartment().getId(), USER_TEST_ID,
                            Role.EMPLOYEE, testBreakage.getDepartment().getId(), USER_TEST_NAME
                    );
        }

        @Test
        void whenCancelBreakageWhichNotExistThenReturnNotFoundException() {

            ApiResponse response = ApiResponse.builder()
                    .message(BREAKAGE_NOT_EXIST)
                    .status(404)
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .timestamp(now)
                    .data(DEFAULT_ADMIN_USERNAME)
                    .build();

            when(breakageService.cancelBreakage(
                            SOME_NOT_EXIST_ID, testBreakage.getDepartment().getId(), DEFAULT_ADMIN_USER_ID,
                            Role.ADMIN, DEFAULT_ADMIN_DEPARTMENT_ID, DEFAULT_ADMIN_USERNAME
                    )
            ).thenReturn(response);

            ApiResponse apiResponse = breakageController.cancelBreakage(
                    DEFAULT_ADMIN_USER_ID, SOME_NOT_EXIST_ID, testBreakage.getDepartment().getId(),
                    Role.ADMIN, DEFAULT_ADMIN_DEPARTMENT_ID, DEFAULT_ADMIN_USERNAME
            );

            assertEquals(BREAKAGE_NOT_EXIST, apiResponse.message());
            assertEquals(404, apiResponse.status());
            assertEquals(HttpStatus.NOT_FOUND, apiResponse.httpStatus());
            assertEquals(now, apiResponse.timestamp());
            assertEquals(DEFAULT_ADMIN_USERNAME, apiResponse.data());

            verify(breakageService, times(1))
                    .cancelBreakage(
                            SOME_NOT_EXIST_ID, testBreakage.getDepartment().getId(), DEFAULT_ADMIN_USER_ID,
                            Role.ADMIN, DEFAULT_ADMIN_DEPARTMENT_ID, DEFAULT_ADMIN_USERNAME
                    );
        }

        @Test
        void whenCancelBreakageByEmployeeFromOtherDepartmentThenReturnForbiddenException() {

            String responseMessage = "Только технический специалист или сотрудник отдела, " +
                    "в котором произошла неисправность, могут отменить заявку !!!";

            ApiResponse response = ApiResponse.builder()
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
            ).thenReturn(response);

            ApiResponse apiResponse = breakageController.cancelBreakage(
                    USER_TEST_ID, testBreakage.getId(), testBreakage.getDepartment().getId(),
                    Role.EMPLOYEE, SOME_NOT_EXIST_ID, USER_TEST_NAME
            );

            assertEquals(responseMessage, apiResponse.message());
            assertEquals(422, apiResponse.status());
            assertEquals(HttpStatus.FORBIDDEN, apiResponse.httpStatus());
            assertEquals(now, apiResponse.timestamp());
            assertEquals(USER_TEST_NAME, apiResponse.data());

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
        void whenUpdateBreakageStatusThenReturnOk() {

            updateBreakageStatusDto = new UpdateBreakageStatusDto(Status.IN_PROGRESS);

            String responseMessage = "Статус заявки на неисправность был успешно изменен";

            ApiResponse response = ApiResponse.builder()
                    .message(responseMessage)
                    .status(200)
                    .httpStatus(HttpStatus.OK)
                    .timestamp(now)
                    .data(DEFAULT_ADMIN_USERNAME)
                    .build();

            when(breakageService.updateBreakageStatus(
                            testBreakage.getId(), updateBreakageStatusDto,
                            DEFAULT_ADMIN_USER_ID, DEFAULT_ADMIN_USERNAME
                    )
            ).thenReturn(response);

            ApiResponse apiResponse = breakageController.updateBreakageStatus(
                    DEFAULT_ADMIN_USER_ID, testBreakage.getId(), updateBreakageStatusDto, DEFAULT_ADMIN_USERNAME
            );

            assertEquals(responseMessage, apiResponse.message());
            assertEquals(200, apiResponse.status());
            assertEquals(HttpStatus.OK, apiResponse.httpStatus());
            assertEquals(now, apiResponse.timestamp());
            assertEquals(DEFAULT_ADMIN_USERNAME, apiResponse.data());

            verify(breakageService, times(1))
                    .updateBreakageStatus(
                            testBreakage.getId(), updateBreakageStatusDto,
                            DEFAULT_ADMIN_USER_ID, DEFAULT_ADMIN_USERNAME
                    );
        }

        @Test
        void whenUpdateBreakageStatusThenResetExecutorAndReturnOk() {

            updateBreakageStatusDto = new UpdateBreakageStatusDto(Status.PAUSED);

            String responseMessage = "Статус заявки на неисправность был успешно изменен";

            ApiResponse response = ApiResponse.builder()
                    .message(responseMessage)
                    .status(200)
                    .httpStatus(HttpStatus.OK)
                    .timestamp(now)
                    .data(DEFAULT_ADMIN_USERNAME)
                    .build();

            when(breakageService.updateBreakageStatus(
                            testBreakage.getId(), updateBreakageStatusDto,
                            DEFAULT_ADMIN_USER_ID, DEFAULT_ADMIN_USERNAME
                    )
            ).thenReturn(response);

            ApiResponse apiResponse = breakageController.updateBreakageStatus(
                    DEFAULT_ADMIN_USER_ID, testBreakage.getId(), updateBreakageStatusDto, DEFAULT_ADMIN_USERNAME
            );

            assertEquals(responseMessage, apiResponse.message());
            assertEquals(200, apiResponse.status());
            assertEquals(HttpStatus.OK, apiResponse.httpStatus());
            assertEquals(now, apiResponse.timestamp());
            assertEquals(DEFAULT_ADMIN_USERNAME, apiResponse.data());

            verify(breakageService, times(1))
                    .updateBreakageStatus(
                            testBreakage.getId(), updateBreakageStatusDto,
                            DEFAULT_ADMIN_USER_ID, DEFAULT_ADMIN_USERNAME
                    );
        }

        @Test
        void whenUpdateBreakageWhichNotExistThenReturnNotFoundException() {

            updateBreakageStatusDto = new UpdateBreakageStatusDto(Status.IN_PROGRESS);

            ApiResponse response = ApiResponse.builder()
                    .message(BREAKAGE_NOT_EXIST)
                    .status(404)
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .timestamp(now)
                    .data(DEFAULT_ADMIN_USERNAME)
                    .build();

            when(breakageService.updateBreakageStatus(
                            SOME_NOT_EXIST_ID, updateBreakageStatusDto,
                            DEFAULT_ADMIN_USER_ID, DEFAULT_ADMIN_USERNAME
                    )
            ).thenReturn(response);

            ApiResponse apiResponse = breakageController.updateBreakageStatus(
                    DEFAULT_ADMIN_USER_ID, SOME_NOT_EXIST_ID, updateBreakageStatusDto, DEFAULT_ADMIN_USERNAME
            );

            assertEquals(BREAKAGE_NOT_EXIST, apiResponse.message());
            assertEquals(404, apiResponse.status());
            assertEquals(HttpStatus.NOT_FOUND, apiResponse.httpStatus());
            assertEquals(now, apiResponse.timestamp());
            assertEquals(DEFAULT_ADMIN_USERNAME, apiResponse.data());

            verify(breakageService, times(1))
                    .updateBreakageStatus(
                            SOME_NOT_EXIST_ID, updateBreakageStatusDto,
                            DEFAULT_ADMIN_USER_ID, DEFAULT_ADMIN_USERNAME
                    );
        }

        @Test
        void whenUpdateBreakageStatusIfStatusIsNewThenReturnNotCorrectParameter() {

            updateBreakageStatusDto = new UpdateBreakageStatusDto(Status.NEW);

            String responseMessage = "Заявка на неисправность не может изменить статус на - \"Новая\" !!!";

            ApiResponse response = ApiResponse.builder()
                    .message(responseMessage)
                    .status(400)
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .timestamp(now)
                    .data(DEFAULT_ADMIN_USERNAME)
                    .build();

            when(breakageService.updateBreakageStatus(
                            testBreakage.getId(), updateBreakageStatusDto,
                            DEFAULT_ADMIN_USER_ID, DEFAULT_ADMIN_USERNAME
                    )
            ).thenReturn(response);

            ApiResponse apiResponse = breakageController.updateBreakageStatus(
                    DEFAULT_ADMIN_USER_ID, testBreakage.getId(), updateBreakageStatusDto, DEFAULT_ADMIN_USERNAME
            );

            assertEquals(responseMessage, apiResponse.message());
            assertEquals(400, apiResponse.status());
            assertEquals(HttpStatus.BAD_REQUEST, apiResponse.httpStatus());
            assertEquals(now, apiResponse.timestamp());
            assertEquals(DEFAULT_ADMIN_USERNAME, apiResponse.data());

            verify(breakageService, times(1))
                    .updateBreakageStatus(
                            testBreakage.getId(), updateBreakageStatusDto,
                            DEFAULT_ADMIN_USER_ID, DEFAULT_ADMIN_USERNAME
                    );
        }
    }

    @Nested
    class WhenBreakagePriorityUpdating {

        private UpdateBreakagePriorityDto updateBreakagePriorityDto;

        @Test
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

            ApiResponse returnedApiResponse = breakageController.updateBreakagePriority(
                    DEFAULT_ADMIN_USER_ID, testBreakage.getId(), updateBreakagePriorityDto, DEFAULT_ADMIN_USERNAME
            );

            assertEquals(responseMessage, returnedApiResponse.message());
            assertEquals(200, returnedApiResponse.status());
            assertEquals(HttpStatus.OK, returnedApiResponse.httpStatus());
            assertEquals(now, returnedApiResponse.timestamp());
            assertEquals(DEFAULT_ADMIN_USERNAME, returnedApiResponse.data());

            verify(breakageService, times(1))
                    .updateBreakagePriority(
                            testBreakage.getId(), updateBreakagePriorityDto,
                            DEFAULT_ADMIN_USER_ID, DEFAULT_ADMIN_USERNAME
                    );
        }

        @Test
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

            ApiResponse returnedApiResponse = breakageController.updateBreakagePriority(
                    DEFAULT_ADMIN_USER_ID, SOME_NOT_EXIST_ID, updateBreakagePriorityDto, DEFAULT_ADMIN_USERNAME
            );

            assertEquals(BREAKAGE_NOT_EXIST, returnedApiResponse.message());
            assertEquals(404, returnedApiResponse.status());
            assertEquals(HttpStatus.NOT_FOUND, returnedApiResponse.httpStatus());
            assertEquals(now, returnedApiResponse.timestamp());
            assertEquals(DEFAULT_ADMIN_USERNAME, returnedApiResponse.data());

            verify(breakageService, times(1))
                    .updateBreakagePriority(
                            SOME_NOT_EXIST_ID, updateBreakagePriorityDto,
                            DEFAULT_ADMIN_USER_ID, DEFAULT_ADMIN_USERNAME
                    );
        }

        @Test
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

            ApiResponse returnedApiResponse = breakageController.updateBreakagePriority(
                    DEFAULT_ADMIN_USER_ID, testBreakage.getId(), updateBreakagePriorityDto, DEFAULT_ADMIN_USERNAME
            );

            assertEquals(responseMessage, returnedApiResponse.message());
            assertEquals(400, returnedApiResponse.status());
            assertEquals(HttpStatus.BAD_REQUEST, returnedApiResponse.httpStatus());
            assertEquals(now, returnedApiResponse.timestamp());
            assertEquals(DEFAULT_ADMIN_USERNAME, returnedApiResponse.data());

            verify(breakageService, times(1))
                    .updateBreakagePriority(
                            testBreakage.getId(), updateBreakagePriorityDto,
                            DEFAULT_ADMIN_USER_ID, DEFAULT_ADMIN_USERNAME
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

            ApiResponse returnedApiResponse = breakageController.addBreakageExecutor(
                    DEFAULT_ADMIN_USER_ID, testBreakage.getId(), appointBreakageExecutorDto, DEFAULT_ADMIN_USERNAME
            );

            assertEquals(responseMessage, returnedApiResponse.message());
            assertEquals(200, returnedApiResponse.status());
            assertEquals(HttpStatus.OK, returnedApiResponse.httpStatus());
            assertEquals(now, returnedApiResponse.timestamp());
            assertEquals(DEFAULT_ADMIN_USERNAME, returnedApiResponse.data());

            verify(breakageService, times(1))
                    .addBreakageExecutor(
                            testBreakage.getId(), appointBreakageExecutorDto,
                            DEFAULT_ADMIN_USER_ID, DEFAULT_ADMIN_USERNAME
                    );
        }

        @Test
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

            ApiResponse returnedApiResponse = breakageController.addBreakageExecutor(
                    DEFAULT_ADMIN_USER_ID, SOME_NOT_EXIST_ID, appointBreakageExecutorDto, DEFAULT_ADMIN_USERNAME
            );

            assertEquals(BREAKAGE_NOT_EXIST, returnedApiResponse.message());
            assertEquals(404, returnedApiResponse.status());
            assertEquals(HttpStatus.NOT_FOUND, returnedApiResponse.httpStatus());
            assertEquals(now, returnedApiResponse.timestamp());
            assertEquals(DEFAULT_ADMIN_USERNAME, returnedApiResponse.data());

            verify(breakageService, times(1))
                    .addBreakageExecutor(
                            SOME_NOT_EXIST_ID, appointBreakageExecutorDto,
                            DEFAULT_ADMIN_USER_ID, DEFAULT_ADMIN_USERNAME
                    );
        }

        @Test
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

            ApiResponse returnedApiResponse = breakageController.addBreakageExecutor(
                    DEFAULT_ADMIN_USER_ID, testBreakage.getId(), appointBreakageExecutorDto, DEFAULT_ADMIN_USERNAME
            );

            assertEquals(responseMessage, returnedApiResponse.message());
            assertEquals(404, returnedApiResponse.status());
            assertEquals(HttpStatus.NOT_FOUND, returnedApiResponse.httpStatus());
            assertEquals(now, returnedApiResponse.timestamp());

            verify(breakageService, times(1))
                    .addBreakageExecutor(
                            testBreakage.getId(), appointBreakageExecutorDto,
                            DEFAULT_ADMIN_USER_ID, DEFAULT_ADMIN_USERNAME
                    );
        }

        @Test
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

            ApiResponse returnedApiResponse = breakageController.addBreakageExecutor(
                    DEFAULT_ADMIN_USER_ID, testBreakage.getId(), appointBreakageExecutorDto, DEFAULT_ADMIN_USERNAME
            );

            assertEquals(responseMessage, returnedApiResponse.message());
            assertEquals(400, returnedApiResponse.status());
            assertEquals(HttpStatus.BAD_REQUEST, returnedApiResponse.httpStatus());
            assertEquals(now, returnedApiResponse.timestamp());

            verify(breakageService, times(1))
                    .addBreakageExecutor(
                            testBreakage.getId(), appointBreakageExecutorDto,
                            DEFAULT_ADMIN_USER_ID, DEFAULT_ADMIN_USERNAME
                    );
        }

        @Test
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

            ApiResponse returnedApiResponse = breakageController.addBreakageExecutor(
                    DEFAULT_ADMIN_USER_ID, testBreakage.getId(), appointBreakageExecutorDto, DEFAULT_ADMIN_USERNAME
            );

            assertEquals(responseMessage, returnedApiResponse.message());
            assertEquals(400, returnedApiResponse.status());
            assertEquals(HttpStatus.BAD_REQUEST, returnedApiResponse.httpStatus());
            assertEquals(now, returnedApiResponse.timestamp());

            verify(breakageService, times(1))
                    .addBreakageExecutor(
                            testBreakage.getId(), appointBreakageExecutorDto,
                            DEFAULT_ADMIN_USER_ID, DEFAULT_ADMIN_USERNAME
                    );
        }
    }


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