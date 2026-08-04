package ru.kraser.technical_helper.breakage_server.controller;

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
import ru.kraser.technical_helper.common_module.dto.api.AppPage;
import ru.kraser.technical_helper.common_module.dto.breakage.*;
import ru.kraser.technical_helper.common_module.dto.breakage_comment.BreakageCommentFrontDto;
import ru.kraser.technical_helper.common_module.dto.breakage_comment.CreateBreakageCommentDto;
import ru.kraser.technical_helper.common_module.enums.Executor;
import ru.kraser.technical_helper.common_module.enums.Priority;
import ru.kraser.technical_helper.common_module.enums.Role;
import ru.kraser.technical_helper.common_module.enums.Status;
import ru.kraser.technical_helper.common_module.exception.ForbiddenException;
import ru.kraser.technical_helper.common_module.exception.NotFoundException;
import ru.kraser.technical_helper.common_module.model.Breakage;
import ru.kraser.technical_helper.common_module.model.Department;

import java.time.*;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.kraser.technical_helper.common_module.util.Constant.BREAKAGE_COMMENT_NOT_EXIST;
import static ru.kraser.technical_helper.common_module.util.Constant.BREAKAGE_NOT_EXIST;
import static ru.kraser.technical_helper.common_module.util.ConstantForTests.*;

@ExtendWith(MockitoExtension.class)
class BreakageControllerTest {

    @Mock
    private BreakageService breakageService;
    @InjectMocks
    private BreakageController breakageController;

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

    @Nested
    class WhenBreakageExecutorDropping {

        @Test
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

            ApiResponse returnedApiResponse = breakageController.dropBreakageExecutor(
                    DEFAULT_ADMIN_USER_ID, testBreakage.getId(), DEFAULT_ADMIN_USERNAME
            );

            assertEquals(responseMessage, returnedApiResponse.message());
            assertEquals(200, returnedApiResponse.status());
            assertEquals(HttpStatus.OK, returnedApiResponse.httpStatus());
            assertEquals(now, returnedApiResponse.timestamp());
            assertEquals(DEFAULT_ADMIN_USERNAME, returnedApiResponse.data());

            verify(breakageService, times(1))
                    .dropBreakageExecutor(
                            testBreakage.getId(), DEFAULT_ADMIN_USER_ID, DEFAULT_ADMIN_USERNAME
                    );
        }

        @Test
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

            ApiResponse returnedApiResponse = breakageController.dropBreakageExecutor(
                    DEFAULT_ADMIN_USER_ID, SOME_NOT_EXIST_ID, DEFAULT_ADMIN_USERNAME
            );

            assertEquals(BREAKAGE_NOT_EXIST, returnedApiResponse.message());
            assertEquals(404, returnedApiResponse.status());
            assertEquals(HttpStatus.NOT_FOUND, returnedApiResponse.httpStatus());
            assertEquals(now, returnedApiResponse.timestamp());

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
            void whenGetAllEmployeeBreakagesThenReturnAppPage() {

                when(breakageService.getAllBreakages(
                                pageSize, pageIndex, defaultSortBy, defaultDirection,
                                true, true, true, true, true,
                                true, true, true, true, true,
                                defaultExecutor, false, null,
                                Role.EMPLOYEE, DEPARTMENT_TEST_ID, USER_TEST_ID
                        )
                ).thenReturn(employeeAppPage);

                AppPage returnedAppPage = breakageController.getAllBreakages(
                        pageSize, pageIndex, defaultSortBy, defaultDirection,
                        true, true, true, true, true,
                        true, true, true, true, true,
                        defaultExecutor, false, null,
                        Role.EMPLOYEE, DEPARTMENT_TEST_ID, USER_TEST_ID
                );

                assertEquals(employeeAppPage, returnedAppPage);

                verify(breakageService, times(1)).getAllBreakages(
                        pageSize, pageIndex, defaultSortBy, defaultDirection,
                        true, true, true, true, true,
                        true, true, true, true, true,
                        defaultExecutor, false, null,
                        Role.EMPLOYEE, DEPARTMENT_TEST_ID, USER_TEST_ID
                );
            }

            @Test
            void whenGetAllEmployeeBreakagesByTextThenReturnAppPage() {

                when(breakageService.getAllBreakages(
                                pageSize, pageIndex, defaultSortBy, defaultDirection,
                                true, true, true, true, true,
                                true, true, true, true, true,
                                defaultExecutor, false, defaultSearchText,
                                Role.EMPLOYEE, DEPARTMENT_TEST_ID, USER_TEST_ID
                        )
                ).thenReturn(employeeAppPage);

                AppPage returnedAppPage = breakageController.getAllBreakages(
                        pageSize, pageIndex, defaultSortBy, defaultDirection,
                        true, true, true, true, true,
                        true, true, true, true, true,
                        defaultExecutor, false, defaultSearchText,
                        Role.EMPLOYEE, DEPARTMENT_TEST_ID, USER_TEST_ID
                );

                assertEquals(employeeAppPage, returnedAppPage);

                verify(breakageService, times(1)).getAllBreakages(
                        pageSize, pageIndex, defaultSortBy, defaultDirection,
                        true, true, true, true, true,
                        true, true, true, true, true,
                        defaultExecutor, false, defaultSearchText,
                        Role.EMPLOYEE, DEPARTMENT_TEST_ID, USER_TEST_ID
                );
            }

            @Test
            void whenGetAllEmployeeBreakagesThenReturnEmptyAppPageContent() {

                AppPage employeeEmptyAppPage = AppPage.builder()
                        .content(Collections.emptyList())
                        .totalElements(0L)
                        .totalPages(0)
                        .numberOfElements(0)
                        .pageNumber(0)
                        .pageSize(10)
                        .offset(0L)
                        .first(true)
                        .last(true)
                        .isForEmployee(true)
                        .now(null)
                        .build();

                when(breakageService.getAllBreakages(
                                pageSize, pageIndex, defaultSortBy, defaultDirection,
                                false, true, true, true, true,
                                true, true, true, true, true,
                                defaultExecutor, false, null,
                                Role.EMPLOYEE, DEPARTMENT_TEST_ID, USER_TEST_ID
                        )
                ).thenReturn(employeeEmptyAppPage);

                AppPage returnedAppPage = breakageController.getAllBreakages(
                        pageSize, pageIndex, defaultSortBy, defaultDirection,
                        false, true, true, true, true,
                        true, true, true, true, true,
                        defaultExecutor, false, null,
                        Role.EMPLOYEE, DEPARTMENT_TEST_ID, USER_TEST_ID
                );

                assertEquals(employeeEmptyAppPage, returnedAppPage);

                verify(breakageService, times(1)).getAllBreakages(
                        pageSize, pageIndex, defaultSortBy, defaultDirection,
                        false, true, true, true, true,
                        true, true, true, true, true,
                        defaultExecutor, false, null,
                        Role.EMPLOYEE, DEPARTMENT_TEST_ID, USER_TEST_ID
                );
            }
        }

        @Nested
        class WhenAllBreakagesGettingByTechnician {

            private BreakageTechDto breakageTechDto;
            private List<BreakageTechDto> content;
            private AppPage technicianAppPage;
            private LocalDateTime testDeadlineBeforeNow;
            private LocalDateTime testDeadlineAfterNow;

            @Nested
            class WhenAllBreakagesAppointedToMeGetting {

                @Nested
                class WhenAllBreakagesWithDeadlineAppointedToMeGetting {

                    @BeforeEach
                    void setUp() {

                        testDeadlineBeforeNow = now.minusDays(1);

                        breakageTechDto = BreakageTechDto.builder()
                                .id(testBreakage.getId())
                                .departmentId(testBreakage.getDepartment().getId())
                                .departmentName(testBreakage.getDepartment().getName())
                                .room(testBreakage.getRoom())
                                .breakageTopic(testBreakage.getBreakageTopic())
                                .breakageText(testBreakage.getBreakageText())
                                .status(testBreakage.getStatus())
                                .priority(Priority.MEDIUM)
                                .breakageExecutor(USER_TEST_NAME)
                                .createdBy(testBreakage.getCreatedBy())
                                .createdDate(testBreakage.getCreatedDate())
                                .deadline(testDeadlineBeforeNow)
                                .build();

                        content = List.of(breakageTechDto);

                        technicianAppPage = AppPage.builder()
                                .content(content)
                                .totalElements(1L)
                                .totalPages(1)
                                .numberOfElements(1)
                                .pageNumber(0)
                                .pageSize(10)
                                .offset(0L)
                                .first(true)
                                .last(true)
                                .isForEmployee(false)
                                .now(now)
                                .build();
                    }

                    @Test
                    void whenGetAllBreakagesAppointedToMeWithDeadlineThenReturnAppPage() {

                        when(breakageService.getAllBreakages(
                                        pageSize, pageIndex, defaultSortBy, defaultDirection,
                                        true, true, true, true, true,
                                        true, true, true, true, true,
                                        Executor.APPOINTED_TO_ME.name(), true, null,
                                        Role.TECHNICIAN, DEPARTMENT_TEST_ID, USER_TEST_ID
                                )
                        ).thenReturn(technicianAppPage);

                        AppPage returnedAppPage = breakageController.getAllBreakages(
                                pageSize, pageIndex, defaultSortBy, defaultDirection,
                                true, true, true, true, true,
                                true, true, true, true, true,
                                Executor.APPOINTED_TO_ME.name(), true, null,
                                Role.TECHNICIAN, DEPARTMENT_TEST_ID, USER_TEST_ID
                        );

                        assertEquals(technicianAppPage, returnedAppPage);

                        verify(breakageService, times(1)).getAllBreakages(
                                pageSize, pageIndex, defaultSortBy, defaultDirection,
                                true, true, true, true, true,
                                true, true, true, true, true,
                                Executor.APPOINTED_TO_ME.name(), true, null,
                                Role.TECHNICIAN, DEPARTMENT_TEST_ID, USER_TEST_ID
                        );
                    }

                    @Test
                    void whenGetAllBreakagesByTextAppointedToMeWithDeadlineThenReturnAppPage() {

                        when(breakageService.getAllBreakages(
                                        pageSize, pageIndex, defaultSortBy, defaultDirection,
                                        true, true, true, true, true,
                                        true, true, true, true, true,
                                        Executor.APPOINTED_TO_ME.name(), true, defaultSearchText,
                                        Role.TECHNICIAN, DEPARTMENT_TEST_ID, USER_TEST_ID
                                )
                        ).thenReturn(technicianAppPage);

                        AppPage returnedAppPage = breakageController.getAllBreakages(
                                pageSize, pageIndex, defaultSortBy, defaultDirection,
                                true, true, true, true, true,
                                true, true, true, true, true,
                                Executor.APPOINTED_TO_ME.name(), true, defaultSearchText,
                                Role.TECHNICIAN, DEPARTMENT_TEST_ID, USER_TEST_ID
                        );

                        assertEquals(technicianAppPage, returnedAppPage);

                        verify(breakageService, times(1)).getAllBreakages(
                                pageSize, pageIndex, defaultSortBy, defaultDirection,
                                true, true, true, true, true,
                                true, true, true, true, true,
                                Executor.APPOINTED_TO_ME.name(), true, defaultSearchText,
                                Role.TECHNICIAN, DEPARTMENT_TEST_ID, USER_TEST_ID
                        );
                    }
                }

                @Nested
                class WhenAllBreakagesWithNoDeadlineAppointedToMeGetting {

                    @BeforeEach
                    void setUp() {

                        testDeadlineAfterNow = now.plusDays(1);

                        breakageTechDto = BreakageTechDto.builder()
                                .id(testBreakage.getId())
                                .departmentId(testBreakage.getDepartment().getId())
                                .departmentName(testBreakage.getDepartment().getName())
                                .room(testBreakage.getRoom())
                                .breakageTopic(testBreakage.getBreakageTopic())
                                .breakageText(testBreakage.getBreakageText())
                                .status(testBreakage.getStatus())
                                .priority(Priority.MEDIUM)
                                .breakageExecutor(USER_TEST_NAME)
                                .createdBy(testBreakage.getCreatedBy())
                                .createdDate(testBreakage.getCreatedDate())
                                .deadline(testDeadlineAfterNow)
                                .build();

                        content = List.of(breakageTechDto);

                        technicianAppPage = AppPage.builder()
                                .content(content)
                                .totalElements(1L)
                                .totalPages(1)
                                .numberOfElements(1)
                                .pageNumber(0)
                                .pageSize(10)
                                .offset(0L)
                                .first(true)
                                .last(true)
                                .isForEmployee(false)
                                .now(now)
                                .build();
                    }

                    @Test
                    void whenGetAllBreakagesAppointedToMeWithNoDeadlineThenReturnAppPage() {

                        when(breakageService.getAllBreakages(
                                        pageSize, pageIndex, defaultSortBy, defaultDirection,
                                        true, true, true, true, true,
                                        true, true, true, true, true,
                                        Executor.APPOINTED_TO_ME.name(), false, null,
                                        Role.TECHNICIAN, DEPARTMENT_TEST_ID, USER_TEST_ID
                                )
                        ).thenReturn(technicianAppPage);

                        AppPage returnedAppPage = breakageController.getAllBreakages(
                                pageSize, pageIndex, defaultSortBy, defaultDirection,
                                true, true, true, true, true,
                                true, true, true, true, true,
                                Executor.APPOINTED_TO_ME.name(), false, null,
                                Role.TECHNICIAN, DEPARTMENT_TEST_ID, USER_TEST_ID
                        );

                        assertEquals(technicianAppPage, returnedAppPage);

                        verify(breakageService, times(1)).getAllBreakages(
                                pageSize, pageIndex, defaultSortBy, defaultDirection,
                                true, true, true, true, true,
                                true, true, true, true, true,
                                Executor.APPOINTED_TO_ME.name(), false, null,
                                Role.TECHNICIAN, DEPARTMENT_TEST_ID, USER_TEST_ID
                        );
                    }

                    @Test
                    void whenGetAllBreakagesByTextAppointedToMeWithNoDeadlineThenReturnAppPage() {

                        when(breakageService.getAllBreakages(
                                        pageSize, pageIndex, defaultSortBy, defaultDirection,
                                        true, true, true, true, true,
                                        true, true, true, true, true,
                                        Executor.APPOINTED_TO_ME.name(), false, defaultSearchText,
                                        Role.TECHNICIAN, DEPARTMENT_TEST_ID, USER_TEST_ID
                                )
                        ).thenReturn(technicianAppPage);

                        AppPage returnedAppPage = breakageController.getAllBreakages(
                                pageSize, pageIndex, defaultSortBy, defaultDirection,
                                true, true, true, true, true,
                                true, true, true, true, true,
                                Executor.APPOINTED_TO_ME.name(), false, defaultSearchText,
                                Role.TECHNICIAN, DEPARTMENT_TEST_ID, USER_TEST_ID
                        );

                        assertEquals(technicianAppPage, returnedAppPage);

                        verify(breakageService, times(1)).getAllBreakages(
                                pageSize, pageIndex, defaultSortBy, defaultDirection,
                                true, true, true, true, true,
                                true, true, true, true, true,
                                Executor.APPOINTED_TO_ME.name(), false, defaultSearchText,
                                Role.TECHNICIAN, DEPARTMENT_TEST_ID, USER_TEST_ID
                        );
                    }
                }
            }

            @Nested
            class WhenAllBreakagesAppointedToOthersGetting {

                @Nested
                class WhenAllBreakagesWithDeadlineAppointedToOthersGetting {

                    @BeforeEach
                    void setUp() {

                        testDeadlineBeforeNow = now.minusDays(1);

                        breakageTechDto = BreakageTechDto.builder()
                                .id(testBreakage.getId())
                                .departmentId(testBreakage.getDepartment().getId())
                                .departmentName(testBreakage.getDepartment().getName())
                                .room(testBreakage.getRoom())
                                .breakageTopic(testBreakage.getBreakageTopic())
                                .breakageText(testBreakage.getBreakageText())
                                .status(testBreakage.getStatus())
                                .priority(Priority.MEDIUM)
                                .breakageExecutor(DEFAULT_ADMIN_USERNAME)
                                .createdBy(testBreakage.getCreatedBy())
                                .createdDate(testBreakage.getCreatedDate())
                                .deadline(testDeadlineBeforeNow)
                                .build();

                        content = List.of(breakageTechDto);

                        technicianAppPage = AppPage.builder()
                                .content(content)
                                .totalElements(1L)
                                .totalPages(1)
                                .numberOfElements(1)
                                .pageNumber(0)
                                .pageSize(10)
                                .offset(0L)
                                .first(true)
                                .last(true)
                                .isForEmployee(false)
                                .now(now)
                                .build();
                    }

                    @Test
                    void whenGetAllBreakagesAppointedToOthersWithDeadlineThenReturnAppPage() {

                        when(breakageService.getAllBreakages(
                                        pageSize, pageIndex, defaultSortBy, defaultDirection,
                                        true, true, true, true, true,
                                        true, true, true, true, true,
                                        Executor.APPOINTED_TO_OTHERS.name(), true, null,
                                        Role.TECHNICIAN, DEPARTMENT_TEST_ID, USER_TEST_ID
                                )
                        ).thenReturn(technicianAppPage);

                        AppPage returnedAppPage = breakageController.getAllBreakages(
                                pageSize, pageIndex, defaultSortBy, defaultDirection,
                                true, true, true, true, true,
                                true, true, true, true, true,
                                Executor.APPOINTED_TO_OTHERS.name(), true, null,
                                Role.TECHNICIAN, DEPARTMENT_TEST_ID, USER_TEST_ID
                        );

                        assertEquals(technicianAppPage, returnedAppPage);

                        verify(breakageService, times(1)).getAllBreakages(
                                pageSize, pageIndex, defaultSortBy, defaultDirection,
                                true, true, true, true, true,
                                true, true, true, true, true,
                                Executor.APPOINTED_TO_OTHERS.name(), true, null,
                                Role.TECHNICIAN, DEPARTMENT_TEST_ID, USER_TEST_ID
                        );
                    }

                    @Test
                    void whenGetAllBreakagesByTextAppointedToOthersWithDeadlineThenReturnAppPage() {

                        when(breakageService.getAllBreakages(
                                        pageSize, pageIndex, defaultSortBy, defaultDirection,
                                        true, true, true, true, true,
                                        true, true, true, true, true,
                                        Executor.APPOINTED_TO_OTHERS.name(), true, defaultSearchText,
                                        Role.TECHNICIAN, DEPARTMENT_TEST_ID, USER_TEST_ID
                                )
                        ).thenReturn(technicianAppPage);

                        AppPage returnedAppPage = breakageController.getAllBreakages(
                                pageSize, pageIndex, defaultSortBy, defaultDirection,
                                true, true, true, true, true,
                                true, true, true, true, true,
                                Executor.APPOINTED_TO_OTHERS.name(), true, defaultSearchText,
                                Role.TECHNICIAN, DEPARTMENT_TEST_ID, USER_TEST_ID
                        );

                        assertEquals(technicianAppPage, returnedAppPage);

                        verify(breakageService, times(1)).getAllBreakages(
                                pageSize, pageIndex, defaultSortBy, defaultDirection,
                                true, true, true, true, true,
                                true, true, true, true, true,
                                Executor.APPOINTED_TO_OTHERS.name(), true, defaultSearchText,
                                Role.TECHNICIAN, DEPARTMENT_TEST_ID, USER_TEST_ID
                        );
                    }
                }

                @Nested
                class WhenAllBreakagesWithNoDeadlineAppointedToOthersGetting {

                    @BeforeEach
                    void setUp() {

                        testDeadlineAfterNow = now.plusDays(1);

                        breakageTechDto = BreakageTechDto.builder()
                                .id(testBreakage.getId())
                                .departmentId(testBreakage.getDepartment().getId())
                                .departmentName(testBreakage.getDepartment().getName())
                                .room(testBreakage.getRoom())
                                .breakageTopic(testBreakage.getBreakageTopic())
                                .breakageText(testBreakage.getBreakageText())
                                .status(testBreakage.getStatus())
                                .priority(Priority.MEDIUM)
                                .breakageExecutor(DEFAULT_ADMIN_USERNAME)
                                .createdBy(testBreakage.getCreatedBy())
                                .createdDate(testBreakage.getCreatedDate())
                                .deadline(testDeadlineAfterNow)
                                .build();

                        content = List.of(breakageTechDto);

                        technicianAppPage = AppPage.builder()
                                .content(content)
                                .totalElements(1L)
                                .totalPages(1)
                                .numberOfElements(1)
                                .pageNumber(0)
                                .pageSize(10)
                                .offset(0L)
                                .first(true)
                                .last(true)
                                .isForEmployee(false)
                                .now(now)
                                .build();
                    }

                    @Test
                    void whenGetAllBreakagesAppointedToOthersWithNoDeadlineThenReturnAppPage() {

                        when(breakageService.getAllBreakages(
                                        pageSize, pageIndex, defaultSortBy, defaultDirection,
                                        true, true, true, true, true,
                                        true, true, true, true, true,
                                        Executor.APPOINTED_TO_OTHERS.name(), false, null,
                                        Role.TECHNICIAN, DEPARTMENT_TEST_ID, USER_TEST_ID
                                )
                        ).thenReturn(technicianAppPage);

                        AppPage returnedAppPage = breakageController.getAllBreakages(
                                pageSize, pageIndex, defaultSortBy, defaultDirection,
                                true, true, true, true, true,
                                true, true, true, true, true,
                                Executor.APPOINTED_TO_OTHERS.name(), false, null,
                                Role.TECHNICIAN, DEPARTMENT_TEST_ID, USER_TEST_ID
                        );

                        assertEquals(technicianAppPage, returnedAppPage);

                        verify(breakageService, times(1)).getAllBreakages(
                                pageSize, pageIndex, defaultSortBy, defaultDirection,
                                true, true, true, true, true,
                                true, true, true, true, true,
                                Executor.APPOINTED_TO_OTHERS.name(), false, null,
                                Role.TECHNICIAN, DEPARTMENT_TEST_ID, USER_TEST_ID
                        );
                    }

                    @Test
                    void whenGetAllBreakagesByTextAppointedToOthersWithNoDeadlineThenReturnAppPage() {

                        when(breakageService.getAllBreakages(
                                        pageSize, pageIndex, defaultSortBy, defaultDirection,
                                        true, true, true, true, true,
                                        true, true, true, true, true,
                                        Executor.APPOINTED_TO_OTHERS.name(), false, defaultSearchText,
                                        Role.TECHNICIAN, DEPARTMENT_TEST_ID, USER_TEST_ID
                                )
                        ).thenReturn(technicianAppPage);

                        AppPage returnedAppPage = breakageController.getAllBreakages(
                                pageSize, pageIndex, defaultSortBy, defaultDirection,
                                true, true, true, true, true,
                                true, true, true, true, true,
                                Executor.APPOINTED_TO_OTHERS.name(), false, defaultSearchText,
                                Role.TECHNICIAN, DEPARTMENT_TEST_ID, USER_TEST_ID
                        );

                        assertEquals(technicianAppPage, returnedAppPage);

                        verify(breakageService, times(1)).getAllBreakages(
                                pageSize, pageIndex, defaultSortBy, defaultDirection,
                                true, true, true, true, true,
                                true, true, true, true, true,
                                Executor.APPOINTED_TO_OTHERS.name(), false, defaultSearchText,
                                Role.TECHNICIAN, DEPARTMENT_TEST_ID, USER_TEST_ID
                        );
                    }
                }
            }

            @Nested
            class WhenAllNoAppointedBreakagesGetting {

                @BeforeEach
                void setUp() {

                    breakageTechDto = BreakageTechDto.builder()
                            .id(testBreakage.getId())
                            .departmentId(testBreakage.getDepartment().getId())
                            .departmentName(testBreakage.getDepartment().getName())
                            .room(testBreakage.getRoom())
                            .breakageTopic(testBreakage.getBreakageTopic())
                            .breakageText(testBreakage.getBreakageText())
                            .status(testBreakage.getStatus())
                            .priority(Priority.MEDIUM)
                            .breakageExecutor(null)
                            .createdBy(testBreakage.getCreatedBy())
                            .createdDate(testBreakage.getCreatedDate())
                            .deadline(null)
                            .build();

                    content = List.of(breakageTechDto);

                    technicianAppPage = AppPage.builder()
                            .content(content)
                            .totalElements(1L)
                            .totalPages(1)
                            .numberOfElements(1)
                            .pageNumber(0)
                            .pageSize(10)
                            .offset(0L)
                            .first(true)
                            .last(true)
                            .isForEmployee(false)
                            .now(now)
                            .build();
                }

                @Test
                void whenGetAllNoAppointedBreakagesThenReturnAppPage() {

                    when(breakageService.getAllBreakages(
                                    pageSize, pageIndex, defaultSortBy, defaultDirection,
                                    true, true, true, true, true,
                                    true, true, true, true, true,
                                    Executor.NO_APPOINTED.name(), false, null,
                                    Role.TECHNICIAN, DEPARTMENT_TEST_ID, USER_TEST_ID
                            )
                    ).thenReturn(technicianAppPage);

                    AppPage returnedAppPage = breakageController.getAllBreakages(
                            pageSize, pageIndex, defaultSortBy, defaultDirection,
                            true, true, true, true, true,
                            true, true, true, true, true,
                            Executor.NO_APPOINTED.name(), false, null,
                            Role.TECHNICIAN, DEPARTMENT_TEST_ID, USER_TEST_ID
                    );

                    assertEquals(technicianAppPage, returnedAppPage);

                    verify(breakageService, times(1)).getAllBreakages(
                            pageSize, pageIndex, defaultSortBy, defaultDirection,
                            true, true, true, true, true,
                            true, true, true, true, true,
                            Executor.NO_APPOINTED.name(), false, null,
                            Role.TECHNICIAN, DEPARTMENT_TEST_ID, USER_TEST_ID
                    );
                }

                @Test
                void whenGetAllNoAppointedBreakagesByTextThenReturnAppPage() {

                    when(breakageService.getAllBreakages(
                                    pageSize, pageIndex, defaultSortBy, defaultDirection,
                                    true, true, true, true, true,
                                    true, true, true, true, true,
                                    Executor.NO_APPOINTED.name(), false, defaultSearchText,
                                    Role.TECHNICIAN, DEPARTMENT_TEST_ID, USER_TEST_ID
                            )
                    ).thenReturn(technicianAppPage);

                    AppPage returnedAppPage = breakageController.getAllBreakages(
                            pageSize, pageIndex, defaultSortBy, defaultDirection,
                            true, true, true, true, true,
                            true, true, true, true, true,
                            Executor.NO_APPOINTED.name(), false, defaultSearchText,
                            Role.TECHNICIAN, DEPARTMENT_TEST_ID, USER_TEST_ID
                    );

                    assertEquals(technicianAppPage, returnedAppPage);

                    verify(breakageService, times(1)).getAllBreakages(
                            pageSize, pageIndex, defaultSortBy, defaultDirection,
                            true, true, true, true, true,
                            true, true, true, true, true,
                            Executor.NO_APPOINTED.name(), false, defaultSearchText,
                            Role.TECHNICIAN, DEPARTMENT_TEST_ID, USER_TEST_ID
                    );
                }
            }

            @Nested
            class WhenAllBreakagesGetting {

                @Nested
                class WhenAllBreakagesWithDeadlineGetting {

                    @BeforeEach
                    void setUp() {

                        testDeadlineBeforeNow = now.minusDays(1);

                        breakageTechDto = BreakageTechDto.builder()
                                .id(testBreakage.getId())
                                .departmentId(testBreakage.getDepartment().getId())
                                .departmentName(testBreakage.getDepartment().getName())
                                .room(testBreakage.getRoom())
                                .breakageTopic(testBreakage.getBreakageTopic())
                                .breakageText(testBreakage.getBreakageText())
                                .status(testBreakage.getStatus())
                                .priority(Priority.MEDIUM)
                                .breakageExecutor(DEFAULT_ADMIN_USERNAME)
                                .createdBy(testBreakage.getCreatedBy())
                                .createdDate(testBreakage.getCreatedDate())
                                .deadline(testDeadlineBeforeNow)
                                .build();

                        content = List.of(breakageTechDto);

                        technicianAppPage = AppPage.builder()
                                .content(content)
                                .totalElements(1L)
                                .totalPages(1)
                                .numberOfElements(1)
                                .pageNumber(0)
                                .pageSize(10)
                                .offset(0L)
                                .first(true)
                                .last(true)
                                .isForEmployee(false)
                                .now(now)
                                .build();
                    }

                    @Test
                    void whenGetAllBreakagesWithDeadlineThenReturnAppPage() {

                        when(breakageService.getAllBreakages(
                                        pageSize, pageIndex, defaultSortBy, defaultDirection,
                                        true, true, true, true, true,
                                        true, true, true, true, true,
                                        defaultExecutor, true, null,
                                        Role.TECHNICIAN, DEPARTMENT_TEST_ID, USER_TEST_ID
                                )
                        ).thenReturn(technicianAppPage);

                        AppPage returnedAppPage = breakageController.getAllBreakages(
                                pageSize, pageIndex, defaultSortBy, defaultDirection,
                                true, true, true, true, true,
                                true, true, true, true, true,
                                defaultExecutor, true, null,
                                Role.TECHNICIAN, DEPARTMENT_TEST_ID, USER_TEST_ID
                        );

                        assertEquals(technicianAppPage, returnedAppPage);

                        verify(breakageService, times(1)).getAllBreakages(
                                pageSize, pageIndex, defaultSortBy, defaultDirection,
                                true, true, true, true, true,
                                true, true, true, true, true,
                                defaultExecutor, true, null,
                                Role.TECHNICIAN, DEPARTMENT_TEST_ID, USER_TEST_ID
                        );
                    }

                    @Test
                    void whenGetAllBreakagesByTextWithDeadlineThenReturnAppPage() {

                        when(breakageService.getAllBreakages(
                                        pageSize, pageIndex, defaultSortBy, defaultDirection,
                                        true, true, true, true, true,
                                        true, true, true, true, true,
                                        defaultExecutor, true, defaultSearchText,
                                        Role.TECHNICIAN, DEPARTMENT_TEST_ID, USER_TEST_ID
                                )
                        ).thenReturn(technicianAppPage);

                        AppPage returnedAppPage = breakageController.getAllBreakages(
                                pageSize, pageIndex, defaultSortBy, defaultDirection,
                                true, true, true, true, true,
                                true, true, true, true, true,
                                defaultExecutor, true, defaultSearchText,
                                Role.TECHNICIAN, DEPARTMENT_TEST_ID, USER_TEST_ID
                        );

                        assertEquals(technicianAppPage, returnedAppPage);

                        verify(breakageService, times(1)).getAllBreakages(
                                pageSize, pageIndex, defaultSortBy, defaultDirection,
                                true, true, true, true, true,
                                true, true, true, true, true,
                                defaultExecutor, true, defaultSearchText,
                                Role.TECHNICIAN, DEPARTMENT_TEST_ID, USER_TEST_ID
                        );
                    }
                }

                @Nested
                class WhenAllBreakagesWithNoDeadlineGetting {

                    @BeforeEach
                    void setUp() {

                        breakageTechDto = BreakageTechDto.builder()
                                .id(testBreakage.getId())
                                .departmentId(testBreakage.getDepartment().getId())
                                .departmentName(testBreakage.getDepartment().getName())
                                .room(testBreakage.getRoom())
                                .breakageTopic(testBreakage.getBreakageTopic())
                                .breakageText(testBreakage.getBreakageText())
                                .status(testBreakage.getStatus())
                                .priority(Priority.MEDIUM)
                                .breakageExecutor(null)
                                .createdBy(testBreakage.getCreatedBy())
                                .createdDate(testBreakage.getCreatedDate())
                                .deadline(null)
                                .build();

                        content = List.of(breakageTechDto);

                        technicianAppPage = AppPage.builder()
                                .content(content)
                                .totalElements(1L)
                                .totalPages(1)
                                .numberOfElements(1)
                                .pageNumber(0)
                                .pageSize(10)
                                .offset(0L)
                                .first(true)
                                .last(true)
                                .isForEmployee(false)
                                .now(now)
                                .build();
                    }

                    @Test
                    void whenGetAllBreakagesWithNoDeadlineThenReturnAppPage() {

                        when(breakageService.getAllBreakages(
                                        pageSize, pageIndex, defaultSortBy, defaultDirection,
                                        true, true, true, true, true,
                                        true, true, true, true, true,
                                        defaultExecutor, false, null,
                                        Role.TECHNICIAN, DEPARTMENT_TEST_ID, USER_TEST_ID
                                )
                        ).thenReturn(technicianAppPage);

                        AppPage returnedAppPage = breakageController.getAllBreakages(
                                pageSize, pageIndex, defaultSortBy, defaultDirection,
                                true, true, true, true, true,
                                true, true, true, true, true,
                                defaultExecutor, false, null,
                                Role.TECHNICIAN, DEPARTMENT_TEST_ID, USER_TEST_ID
                        );

                        assertEquals(technicianAppPage, returnedAppPage);

                        verify(breakageService, times(1)).getAllBreakages(
                                pageSize, pageIndex, defaultSortBy, defaultDirection,
                                true, true, true, true, true,
                                true, true, true, true, true,
                                defaultExecutor, false, null,
                                Role.TECHNICIAN, DEPARTMENT_TEST_ID, USER_TEST_ID
                        );
                    }

                    @Test
                    void whenGetAllBreakagesByTextWithNoDeadlineThenReturnAppPage() {

                        when(breakageService.getAllBreakages(
                                        pageSize, pageIndex, defaultSortBy, defaultDirection,
                                        true, true, true, true, true,
                                        true, true, true, true, true,
                                        defaultExecutor, false, defaultSearchText,
                                        Role.TECHNICIAN, DEPARTMENT_TEST_ID, USER_TEST_ID
                                )
                        ).thenReturn(technicianAppPage);

                        AppPage returnedAppPage = breakageController.getAllBreakages(
                                pageSize, pageIndex, defaultSortBy, defaultDirection,
                                true, true, true, true, true,
                                true, true, true, true, true,
                                defaultExecutor, false, defaultSearchText,
                                Role.TECHNICIAN, DEPARTMENT_TEST_ID, USER_TEST_ID
                        );

                        assertEquals(technicianAppPage, returnedAppPage);

                        verify(breakageService, times(1)).getAllBreakages(
                                pageSize, pageIndex, defaultSortBy, defaultDirection,
                                true, true, true, true, true,
                                true, true, true, true, true,
                                defaultExecutor, false, defaultSearchText,
                                Role.TECHNICIAN, DEPARTMENT_TEST_ID, USER_TEST_ID
                        );
                    }
                }

                @Nested
                class WhenAllBreakagesWithNoStatusOrPriorityGetting {

                    private AppPage technicianEmptyAppPage;

                    @BeforeEach
                    void setUp() {

                        content = Collections.emptyList();

                        technicianEmptyAppPage = AppPage.builder()
                                .content(content)
                                .totalElements(0L)
                                .totalPages(0)
                                .numberOfElements(0)
                                .pageNumber(0)
                                .pageSize(10)
                                .offset(0L)
                                .first(true)
                                .last(true)
                                .isForEmployee(false)
                                .now(now)
                                .build();
                    }

                    @Test
                    void whenGetAllBreakagesIfNoStatusThenReturnEmptyAppPage() {

                        when(breakageService.getAllBreakages(
                                        pageSize, pageIndex, defaultSortBy, defaultDirection,
                                        false, true, true, true, true,
                                        true, true, true, true, true,
                                        defaultExecutor, false, null,
                                        Role.TECHNICIAN, DEPARTMENT_TEST_ID, USER_TEST_ID
                                )
                        ).thenReturn(technicianEmptyAppPage);

                        AppPage returnedAppPage = breakageController.getAllBreakages(
                                pageSize, pageIndex, defaultSortBy, defaultDirection,
                                false, true, true, true, true,
                                true, true, true, true, true,
                                defaultExecutor, false, null,
                                Role.TECHNICIAN, DEPARTMENT_TEST_ID, USER_TEST_ID
                        );

                        assertEquals(technicianEmptyAppPage, returnedAppPage);

                        verify(breakageService, times(1)).getAllBreakages(
                                pageSize, pageIndex, defaultSortBy, defaultDirection,
                                false, true, true, true, true,
                                true, true, true, true, true,
                                defaultExecutor, false, null,
                                Role.TECHNICIAN, DEPARTMENT_TEST_ID, USER_TEST_ID
                        );
                    }

                    @Test
                    void whenGetAllBreakagesIfNoPriorityThenReturnEmptyAppPage() {

                        when(breakageService.getAllBreakages(
                                        pageSize, pageIndex, defaultSortBy, defaultDirection,
                                true, true, true, true, true,
                                        true, true, true, false, true,
                                        defaultExecutor, false, null,
                                        Role.TECHNICIAN, DEPARTMENT_TEST_ID, USER_TEST_ID
                                )
                        ).thenReturn(technicianEmptyAppPage);

                        AppPage returnedAppPage = breakageController.getAllBreakages(
                                pageSize, pageIndex, defaultSortBy, defaultDirection,
                                true, true, true, true, true,
                                true, true, true, false, true,
                                defaultExecutor, false, null,
                                Role.TECHNICIAN, DEPARTMENT_TEST_ID, USER_TEST_ID
                        );

                        assertEquals(technicianEmptyAppPage, returnedAppPage);

                        verify(breakageService, times(1)).getAllBreakages(
                                pageSize, pageIndex, defaultSortBy, defaultDirection,
                                true, true, true, true, true,
                                true, true, true, false, true,
                                defaultExecutor, false, null,
                                Role.TECHNICIAN, DEPARTMENT_TEST_ID, USER_TEST_ID
                        );
                    }
                }
            }
        }
    }

    @Nested
    class WhenBreakageByEmployeeGetting {

        @Test
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

            when(breakageService.getBreakageEmployee(testBreakage.getId(), DEPARTMENT_TEST_ID))
                    .thenReturn(breakageEmployeeDto);

            BreakageEmployeeDto returnedBreakageEmployeeDto =
                    breakageController.getBreakageEmployee(testBreakage.getId(), DEPARTMENT_TEST_ID);

            assertEquals(breakageEmployeeDto, returnedBreakageEmployeeDto);

            verify(breakageService, times(1))
                    .getBreakageEmployee(testBreakage.getId(), DEPARTMENT_TEST_ID);
        }

        @Test
        void whenGetBreakageByEmployeeThenReturnForbiddenException() {

            String message = "Данный пользователь не имеет право на получение информации по " +
                    "этой заявке на неисправность !!!";

            when(breakageService.getBreakageEmployee(testBreakage.getId(), SOME_NOT_EXIST_ID))
                    .thenThrow(new ForbiddenException(message));

            assertThatThrownBy(
                    () -> breakageController.getBreakageEmployee(testBreakage.getId(), SOME_NOT_EXIST_ID)
            )
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining(message);

            verify(breakageService, times(1))
                    .getBreakageEmployee(testBreakage.getId(), SOME_NOT_EXIST_ID);
        }

        @Test
        void whenGetBreakageByEmployeeThenReturnNotFoundException() {

            when(breakageService.getBreakageEmployee(SOME_NOT_EXIST_ID, DEPARTMENT_TEST_ID))
                    .thenThrow(new NotFoundException(BREAKAGE_NOT_EXIST));

            assertThatThrownBy(
                    () -> breakageController.getBreakageEmployee(SOME_NOT_EXIST_ID, DEPARTMENT_TEST_ID)
            )
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(BREAKAGE_NOT_EXIST);

            verify(breakageService, times(1))
                    .getBreakageEmployee(SOME_NOT_EXIST_ID, DEPARTMENT_TEST_ID);
        }
    }

    @Nested
    class WhenBreakageGetting {

        @Test
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

            ApiResponse returnedApiResponse =
                    breakageController.getBreakage(DEFAULT_ADMIN_USER_ID, testBreakage.getId());

            assertEquals(apiResponse, returnedApiResponse);

            verify(breakageService, times(1))
                    .getBreakage(testBreakage.getId(), DEFAULT_ADMIN_USER_ID);
        }

        @Test
        void whenGetBreakageThenReturnNotFoundException() {

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(BREAKAGE_NOT_EXIST)
                    .status(404)
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .timestamp(now)
                    .build();

            when(breakageService.getBreakage(SOME_NOT_EXIST_ID, DEFAULT_ADMIN_USER_ID))
                    .thenReturn(apiResponse);

            ApiResponse returnedApiResponse =
                    breakageController.getBreakage(DEFAULT_ADMIN_USER_ID, SOME_NOT_EXIST_ID);

            assertEquals(BREAKAGE_NOT_EXIST, returnedApiResponse.message());
            assertEquals(404, returnedApiResponse.status());
            assertEquals(HttpStatus.NOT_FOUND, returnedApiResponse.httpStatus());
            assertEquals(now, returnedApiResponse.timestamp());

            verify(breakageService, times(1))
                    .getBreakage(SOME_NOT_EXIST_ID, DEFAULT_ADMIN_USER_ID);
        }
    }

    @Nested
    class WhenBreakageCommentMethodsAreInvoked {

        private CreateBreakageCommentDto createBreakageCommentDto;

        @BeforeEach
        void setUp() {

            createBreakageCommentDto =
                    new CreateBreakageCommentDto(BREAKAGE_COMMENT_TEST_TEXT, Status.IN_PROGRESS);
        }

        @Test
        void whenCreateBreakageCommentThenReturnCreated() {

            String responseMessage = "Комментарий к заявке о неисправности - был успешно создан.";

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(responseMessage)
                    .status(201)
                    .httpStatus(HttpStatus.CREATED)
                    .timestamp(now)
                    .build();

            when(breakageService.createBreakageComment(
                    createBreakageCommentDto, testBreakage.getId(), DEFAULT_ADMIN_USER_ID))
                    .thenReturn(apiResponse);

            ApiResponse returnedApiResponse =
                    breakageController.createBreakageComment(
                            DEFAULT_ADMIN_USER_ID, testBreakage.getId(), createBreakageCommentDto
                    );

            assertEquals(responseMessage, returnedApiResponse.message());
            assertEquals(201, returnedApiResponse.status());
            assertEquals(HttpStatus.CREATED, returnedApiResponse.httpStatus());
            assertEquals(now, returnedApiResponse.timestamp());

            verify(breakageService, times(1))
                    .createBreakageComment(createBreakageCommentDto, testBreakage.getId(), DEFAULT_ADMIN_USER_ID);
        }

        @Test
        void whenCreateBreakageCommentThenReturnThenReturnNotFoundException() {

            String responseMessage = "Заявки на неисправность не существует !!!";

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(responseMessage)
                    .status(404)
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .timestamp(now)
                    .build();

            when(breakageService.createBreakageComment(
                    createBreakageCommentDto, SOME_NOT_EXIST_ID, DEFAULT_ADMIN_USER_ID))
                    .thenReturn(apiResponse);

            ApiResponse returnedApiResponse =
                    breakageController.createBreakageComment(
                            DEFAULT_ADMIN_USER_ID, SOME_NOT_EXIST_ID, createBreakageCommentDto
                    );

            assertEquals(responseMessage, returnedApiResponse.message());
            assertEquals(404, returnedApiResponse.status());
            assertEquals(HttpStatus.NOT_FOUND, returnedApiResponse.httpStatus());
            assertEquals(now, returnedApiResponse.timestamp());

            verify(breakageService, times(1))
                    .createBreakageComment(createBreakageCommentDto, SOME_NOT_EXIST_ID, DEFAULT_ADMIN_USER_ID);
        }

        @Test
        void whenCreateBreakageCommentThenReturnThenReturnNotCorrectParameter() {

            CreateBreakageCommentDto commentDto =
                    new CreateBreakageCommentDto(BREAKAGE_COMMENT_TEST_TEXT, Status.SOLVED);

            String responseMessage = "Комментарии к заявке о неисправности со статусами " +
                    "\"Решена\" и \"Отменена\" - не создаются !!!";

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(responseMessage)
                    .status(400)
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .timestamp(now)
                    .build();

            when(breakageService.createBreakageComment(
                    commentDto, testBreakage.getId(), DEFAULT_ADMIN_USER_ID))
                    .thenReturn(apiResponse);

            ApiResponse returnedApiResponse =
                    breakageController.createBreakageComment(
                            DEFAULT_ADMIN_USER_ID, testBreakage.getId(), commentDto
                    );

            assertEquals(responseMessage, returnedApiResponse.message());
            assertEquals(400, returnedApiResponse.status());
            assertEquals(HttpStatus.BAD_REQUEST, returnedApiResponse.httpStatus());
            assertEquals(now, returnedApiResponse.timestamp());

            verify(breakageService, times(1))
                    .createBreakageComment(commentDto, testBreakage.getId(), DEFAULT_ADMIN_USER_ID);
        }

        @Test
        void whenUpdateBreakageCommentThenReturnOk() {

            String responseMessage = "Комментарий к заявке на неисправность был успешно обновлен.";

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(responseMessage)
                    .status(200)
                    .httpStatus(HttpStatus.OK)
                    .timestamp(now)
                    .build();

            when(breakageService.updateBreakageComment(
                    createBreakageCommentDto, BREAKAGE_COMMENT_TEST_ID, DEFAULT_ADMIN_USER_ID))
                    .thenReturn(apiResponse);

            ApiResponse returnedApiResponse =
                    breakageController.updateBreakageComment(
                            DEFAULT_ADMIN_USER_ID, BREAKAGE_COMMENT_TEST_ID, createBreakageCommentDto
                    );

            assertEquals(responseMessage, returnedApiResponse.message());
            assertEquals(200, returnedApiResponse.status());
            assertEquals(HttpStatus.OK, returnedApiResponse.httpStatus());
            assertEquals(now, returnedApiResponse.timestamp());

            verify(breakageService, times(1))
                    .updateBreakageComment(createBreakageCommentDto, BREAKAGE_COMMENT_TEST_ID, DEFAULT_ADMIN_USER_ID);
        }

        @Test
        void whenUpdateBreakageCommentThenReturnNotFoundException() {

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(BREAKAGE_COMMENT_NOT_EXIST)
                    .status(404)
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .timestamp(now)
                    .build();

            when(breakageService.updateBreakageComment(
                    createBreakageCommentDto, SOME_NOT_EXIST_ID, DEFAULT_ADMIN_USER_ID))
                    .thenReturn(apiResponse);

            ApiResponse returnedApiResponse =
                    breakageController.updateBreakageComment(
                            DEFAULT_ADMIN_USER_ID, SOME_NOT_EXIST_ID, createBreakageCommentDto
                    );

            assertEquals(BREAKAGE_COMMENT_NOT_EXIST, returnedApiResponse.message());
            assertEquals(404, returnedApiResponse.status());
            assertEquals(HttpStatus.NOT_FOUND, returnedApiResponse.httpStatus());
            assertEquals(now, returnedApiResponse.timestamp());

            verify(breakageService, times(1))
                    .updateBreakageComment(createBreakageCommentDto, SOME_NOT_EXIST_ID, DEFAULT_ADMIN_USER_ID);
        }

        @Test
        void whenDeleteBreakageCommentThenReturnOk() {

            String responseMessage = "Комментарий к заявке на неисправность был успешно удален.";

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(responseMessage)
                    .status(200)
                    .httpStatus(HttpStatus.OK)
                    .timestamp(now)
                    .build();

            when(breakageService.deleteBreakageComment(BREAKAGE_COMMENT_TEST_ID)).thenReturn(apiResponse);

            ApiResponse returnedApiResponse =
                    breakageController.deleteBreakageComment(BREAKAGE_COMMENT_TEST_ID);

            assertEquals(responseMessage, returnedApiResponse.message());
            assertEquals(200, returnedApiResponse.status());
            assertEquals(HttpStatus.OK, returnedApiResponse.httpStatus());
            assertEquals(now, returnedApiResponse.timestamp());

            verify(breakageService, times(1))
                    .deleteBreakageComment(BREAKAGE_COMMENT_TEST_ID);
        }
    }
}