package ru.kraser.technical_helper.breakage_server.service.service_impl;

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
import ru.kraser.technical_helper.breakage_server.repository.BreakageCommentRepository;
import ru.kraser.technical_helper.breakage_server.repository.BreakageRepository;
import ru.kraser.technical_helper.breakage_server.util.mapper.BreakageMapper;
import ru.kraser.technical_helper.common_module.dto.api.ApiResponse;
import ru.kraser.technical_helper.common_module.dto.breakage.*;
import ru.kraser.technical_helper.common_module.dto.breakage_comment.BreakageCommentBackendDto;
import ru.kraser.technical_helper.common_module.dto.breakage_comment.BreakageCommentFrontDto;
import ru.kraser.technical_helper.common_module.enums.Priority;
import ru.kraser.technical_helper.common_module.enums.Role;
import ru.kraser.technical_helper.common_module.enums.Status;
import ru.kraser.technical_helper.common_module.exception.ForbiddenException;
import ru.kraser.technical_helper.common_module.exception.NotCorrectParameter;
import ru.kraser.technical_helper.common_module.exception.NotFoundException;
import ru.kraser.technical_helper.common_module.model.Breakage;
import ru.kraser.technical_helper.common_module.model.Department;

import java.time.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static ru.kraser.technical_helper.common_module.util.Constant.BREAKAGE_NOT_EXIST;
import static ru.kraser.technical_helper.common_module.util.ConstantForTests.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BreakageServiceImplTest {

    @Mock
    private Clock clock;
    @Mock
    private BreakageRepository breakageRepository;
    @Mock
    private BreakageCommentRepository breakageCommentRepository;
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
                .department(testDepartment)
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

            verify(breakageRepository, never())
                    .updateBreakageStatus(testBreakage.getId(), Status.CANCELLED, DEFAULT_ADMIN_USER_ID, now);
        }
    }

    @Nested
    class WhenBreakageStatusUpdating {

        @Test
        void whenUpdateBreakageStatusThenReturnOk() {

            UpdateBreakageStatusDto updateBreakageStatusDto = new UpdateBreakageStatusDto(Status.IN_PROGRESS);

            String responseMessage = "Статус заявки на неисправность был успешно изменен";

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(responseMessage)
                    .status(200)
                    .httpStatus(HttpStatus.OK)
                    .timestamp(now)
                    .data(USER_TEST_NAME)
                    .build();

            when(breakageRepository.updateBreakageStatus(
                    testBreakage.getId(), Status.IN_PROGRESS, DEFAULT_ADMIN_USER_ID, now)
            ).thenReturn(1);

            ApiResponse returnedApiResponse = breakageService.updateBreakageStatus(
                    testBreakage.getId(), updateBreakageStatusDto, DEFAULT_ADMIN_USER_ID, USER_TEST_NAME
            );

            assertEquals(apiResponse, returnedApiResponse);

            verify(breakageRepository, times(1))
                    .updateBreakageStatus(testBreakage.getId(), Status.IN_PROGRESS, DEFAULT_ADMIN_USER_ID, now);
        }

        @Test
        void whenUpdateBreakageStatusThenResetExecutorAndReturnOk() {

            UpdateBreakageStatusDto updateBreakageStatusDto = new UpdateBreakageStatusDto(Status.PAUSED);

            String responseMessage = "Статус заявки на неисправность был успешно изменен";

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(responseMessage)
                    .status(200)
                    .httpStatus(HttpStatus.OK)
                    .timestamp(now)
                    .data(USER_TEST_NAME)
                    .build();

            when(breakageRepository.updateBreakageStatusAndResetExecutor(
                    testBreakage.getId(), Status.PAUSED, DEFAULT_ADMIN_USER_ID, now)
            ).thenReturn(1);

            ApiResponse returnedApiResponse = breakageService.updateBreakageStatus(
                    testBreakage.getId(), updateBreakageStatusDto, DEFAULT_ADMIN_USER_ID, USER_TEST_NAME
            );

            assertEquals(apiResponse, returnedApiResponse);

            verify(breakageRepository, times(1))
                    .updateBreakageStatusAndResetExecutor(
                            testBreakage.getId(), Status.PAUSED, DEFAULT_ADMIN_USER_ID, now
                    );
        }

        @Test
        void whenUpdateBreakageWhichNotExistThenReturnNotFoundException() {

            UpdateBreakageStatusDto updateBreakageStatusDto = new UpdateBreakageStatusDto(Status.IN_PROGRESS);

            when(breakageRepository.updateBreakageStatus(
                    SOME_NOT_EXIST_ID, Status.IN_PROGRESS, DEFAULT_ADMIN_USER_ID, now)
            ).thenThrow(new NotFoundException(BREAKAGE_NOT_EXIST));

            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> breakageService.updateBreakageStatus(
                            SOME_NOT_EXIST_ID, updateBreakageStatusDto, DEFAULT_ADMIN_USER_ID, USER_TEST_NAME)
            );

            assertEquals(BREAKAGE_NOT_EXIST, exception.getMessage());

            verify(breakageRepository, times(1))
                    .updateBreakageStatus(SOME_NOT_EXIST_ID, Status.IN_PROGRESS, DEFAULT_ADMIN_USER_ID, now);
        }

        @Test
        void whenUpdateBreakageStatusIfStatusIsNewThenReturnNotCorrectParameter() {

            UpdateBreakageStatusDto updateBreakageStatusDto = new UpdateBreakageStatusDto(Status.NEW);

            String responseMessage = "Заявка на неисправность не может изменить статус на - \"Новая\" !!!";

            NotCorrectParameter exception = assertThrows(
                    NotCorrectParameter.class,
                    () -> breakageService.updateBreakageStatus(
                            testBreakage.getId(), updateBreakageStatusDto, DEFAULT_ADMIN_USER_ID, USER_TEST_NAME
                    )
            );

            assertEquals(responseMessage, exception.getMessage());

            verify(breakageRepository, never())
                    .updateBreakageStatus(testBreakage.getId(), Status.NEW, DEFAULT_ADMIN_USER_ID, now);
        }
    }

    @Nested
    class WhenBreakagePriorityUpdating {

        @Test
        void whenUpdateBreakagePriorityThenReturnOk() {

            UpdateBreakagePriorityDto updateBreakagePriorityDto =
                    new UpdateBreakagePriorityDto(Priority.HIGH, Status.IN_PROGRESS);

            String responseMessage = "Приоритет заявки на неисправность был успешно изменен";

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(responseMessage)
                    .status(200)
                    .httpStatus(HttpStatus.OK)
                    .timestamp(now)
                    .data(USER_TEST_NAME)
                    .build();

            when(breakageRepository.updateBreakagePriority(
                    testBreakage.getId(), Priority.HIGH, DEFAULT_ADMIN_USER_ID, now)
            ).thenReturn(1);

            ApiResponse returnedApiResponse = breakageService.updateBreakagePriority(
                    testBreakage.getId(), updateBreakagePriorityDto, DEFAULT_ADMIN_USER_ID, USER_TEST_NAME
            );

            assertEquals(apiResponse, returnedApiResponse);

            verify(breakageRepository, times(1))
                    .updateBreakagePriority(testBreakage.getId(), Priority.HIGH, DEFAULT_ADMIN_USER_ID, now);
        }

        @Test
        void whenUpdateBreakageWhichNotExistThenReturnNotFoundException() {

            UpdateBreakagePriorityDto updateBreakagePriorityDto =
                    new UpdateBreakagePriorityDto(Priority.HIGH, Status.IN_PROGRESS);

            when(breakageRepository.updateBreakagePriority(
                    SOME_NOT_EXIST_ID, Priority.HIGH, DEFAULT_ADMIN_USER_ID, now)
            ).thenThrow(new NotFoundException(BREAKAGE_NOT_EXIST));

            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> breakageService.updateBreakagePriority(
                            SOME_NOT_EXIST_ID, updateBreakagePriorityDto, DEFAULT_ADMIN_USER_ID, USER_TEST_NAME)
            );

            assertEquals(BREAKAGE_NOT_EXIST, exception.getMessage());

            verify(breakageRepository, times(1))
                    .updateBreakagePriority(SOME_NOT_EXIST_ID, Priority.HIGH, DEFAULT_ADMIN_USER_ID, now);
        }

        @Test
        void whenUpdateBreakagePriorityIfStatusIsSolvedOrCancelledThenReturnNotCorrectParameter() {

            UpdateBreakagePriorityDto updateBreakagePriorityDto =
                    new UpdateBreakagePriorityDto(Priority.HIGH, Status.SOLVED);

            String responseMessage = "Заявка на неисправность со статусом: \"Решена\" или \"Отменена\"" +
                    " - не может быть изменена !!!";

            NotCorrectParameter exception = assertThrows(
                    NotCorrectParameter.class,
                    () -> breakageService.updateBreakagePriority(
                            testBreakage.getId(), updateBreakagePriorityDto, DEFAULT_ADMIN_USER_ID, USER_TEST_NAME
                    )
            );

            assertEquals(responseMessage, exception.getMessage());

            verify(breakageRepository, never())
                    .updateBreakagePriority(testBreakage.getId(), Priority.HIGH, DEFAULT_ADMIN_USER_ID, now);
        }
    }

    @Nested
    class WhenBreakageExecutorAdding {

        private LocalDate afterNowDate;
        private LocalDateTime expectedDeadline;

        @BeforeEach
        void setUp() {

            afterNowDate = now.plusDays(1).toLocalDate();
            expectedDeadline = LocalDateTime.of(afterNowDate, LocalTime.of(23, 59, 59));
        }

        @Test
        void whenAddBreakageExecutorThenReturnOk() {

            AppointBreakageExecutorDto appointBreakageExecutorDto =
                    new AppointBreakageExecutorDto(USER_TEST_ID, afterNowDate, Status.NEW);

            String responseMessage = "Исполнитель заявки на неисправность и срок исполнения были успешно назначены.";

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(responseMessage)
                    .status(200)
                    .httpStatus(HttpStatus.OK)
                    .timestamp(now)
                    .data(USER_TEST_NAME)
                    .build();

            when(breakageRepository.addBreakageExecutor(
                    testBreakage.getId(), appointBreakageExecutorDto.executor(), expectedDeadline, DEFAULT_ADMIN_USER_ID, now)
            ).thenReturn(1);

            ApiResponse returnedApiResponse = breakageService.addBreakageExecutor(
                    testBreakage.getId(), appointBreakageExecutorDto, DEFAULT_ADMIN_USER_ID, USER_TEST_NAME
            );

            assertEquals(apiResponse, returnedApiResponse);

            verify(breakageRepository, times(1))
                    .addBreakageExecutor(
                            testBreakage.getId(),
                            appointBreakageExecutorDto.executor(),
                            expectedDeadline,
                            DEFAULT_ADMIN_USER_ID,
                            now
                    );
        }

        @Test
        void whenAddBreakageExecutorIfBreakageNotExistThenReturnNotFoundException() {

            AppointBreakageExecutorDto appointBreakageExecutorDto =
                    new AppointBreakageExecutorDto(USER_TEST_ID, afterNowDate, Status.NEW);

            when(breakageRepository.addBreakageExecutor(
                    SOME_NOT_EXIST_ID, USER_TEST_ID, expectedDeadline, DEFAULT_ADMIN_USER_ID, now)
            ).thenReturn(0);

            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> breakageService.addBreakageExecutor(
                            SOME_NOT_EXIST_ID, appointBreakageExecutorDto, DEFAULT_ADMIN_USER_ID, USER_TEST_NAME)
            );

            assertEquals(BREAKAGE_NOT_EXIST, exception.getMessage());

            verify(breakageRepository, times(1))
                    .addBreakageExecutor(SOME_NOT_EXIST_ID, USER_TEST_ID, expectedDeadline, DEFAULT_ADMIN_USER_ID, now);
        }

        @Test
        void whenAddBreakageExecutorIfExecutorNotExistThenReturnNotFoundException() {

            AppointBreakageExecutorDto appointBreakageExecutorDto =
                    new AppointBreakageExecutorDto(SOME_NOT_EXIST_ID, afterNowDate, Status.NEW);

            String responseMessage =
                    "Пользователь, который назначается исполнителем заявки на неисправность, не существует.";

            when(breakageRepository.addBreakageExecutor(
                            testBreakage.getId(), SOME_NOT_EXIST_ID, expectedDeadline, DEFAULT_ADMIN_USER_ID, now)
                    ).thenThrow(new NotFoundException(responseMessage));

            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> breakageService.addBreakageExecutor(
                            testBreakage.getId(), appointBreakageExecutorDto, DEFAULT_ADMIN_USER_ID, USER_TEST_NAME
                    )
            );

            assertEquals(responseMessage, exception.getMessage());

            verify(breakageRepository, times(1))
                    .addBreakageExecutor(
                            testBreakage.getId(), SOME_NOT_EXIST_ID, expectedDeadline, DEFAULT_ADMIN_USER_ID, now
                    );
        }

        @Test
        void whenAddBreakageExecutorIfDeadlineIsNotCorrectThenReturnNotCorrectParameter() {

            LocalDateTime beforeNow = now.minusDays(1);
            LocalDate beforeNowDate = beforeNow.toLocalDate();

            AppointBreakageExecutorDto appointBreakageExecutorDto =
                    new AppointBreakageExecutorDto(USER_TEST_ID, beforeNowDate, Status.NEW);

            String responseMessage = "Необходимо указать корректный срок исполнения заявки на неисправность.";

            NotCorrectParameter exception = assertThrows(
                    NotCorrectParameter.class,
                    () -> breakageService.addBreakageExecutor(
                            testBreakage.getId(), appointBreakageExecutorDto, DEFAULT_ADMIN_USER_ID, USER_TEST_NAME
                    )
            );

            assertEquals(responseMessage, exception.getMessage());

            verify(breakageRepository, never())
                    .addBreakageExecutor(testBreakage.getId(), USER_TEST_ID, beforeNow, DEFAULT_ADMIN_USER_ID, now);
        }

        @Test
        void whenAddBreakageExecutorIfStatusIsNotCorrectThenReturnNotCorrectParameter() {

            AppointBreakageExecutorDto appointBreakageExecutorDto =
                    new AppointBreakageExecutorDto(USER_TEST_ID, afterNowDate, Status.SOLVED);

            String responseMessage = "Заявке на неисправность со статусами: \"В ожидании\", \"Передана\"" +
                    ", \"Решена\" или \"Отменена\" - не может быть назначен исполнитель !!!";

            NotCorrectParameter exception = assertThrows(
                    NotCorrectParameter.class,
                    () -> breakageService.addBreakageExecutor(
                            testBreakage.getId(), appointBreakageExecutorDto, DEFAULT_ADMIN_USER_ID, USER_TEST_NAME
                    )
            );

            assertEquals(responseMessage, exception.getMessage());

            verify(breakageRepository, never())
                    .addBreakageExecutor(testBreakage.getId(), USER_TEST_ID, expectedDeadline, DEFAULT_ADMIN_USER_ID, now);
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
                    .data(USER_TEST_NAME)
                    .build();

            when(breakageRepository.dropBreakageExecutor(
                    testBreakage.getId(), DEFAULT_ADMIN_USER_ID, now)
            ).thenReturn(1);

            ApiResponse returnedApiResponse = breakageService.dropBreakageExecutor(
                    testBreakage.getId(), DEFAULT_ADMIN_USER_ID, USER_TEST_NAME);

            assertEquals(apiResponse, returnedApiResponse);

            verify(breakageRepository, times(1))
                    .dropBreakageExecutor(testBreakage.getId(), DEFAULT_ADMIN_USER_ID, now);
        }

        @Test
        void whenDropBreakageThenReturnNotFoundException() {

            when(breakageRepository.dropBreakageExecutor(
                    testBreakage.getId(), DEFAULT_ADMIN_USER_ID, now)
            ).thenReturn(0);

            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> breakageService.dropBreakageExecutor(
                            testBreakage.getId(), DEFAULT_ADMIN_USER_ID, USER_TEST_NAME
                    )
            );

            assertEquals(BREAKAGE_NOT_EXIST, exception.getMessage());

            verify(breakageRepository, times(1))
                    .dropBreakageExecutor(
                            testBreakage.getId(), DEFAULT_ADMIN_USER_ID, now
                    );
        }
    }


//
//    @Test
//    void getAllBreakages() {
//    }
//

    @Nested
    class WhenBreakageByEmployeeGetting {

        private BreakageEmployeeDto breakageEmployeeDto;

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
        }

        @Test
        void whenGetBreakageByEmployeeThenReturnBreakage() {

            when(breakageRepository.getBreakageEmployee(testBreakage.getId()))
                    .thenReturn(Optional.of(breakageEmployeeDto));

            BreakageEmployeeDto returnedBreakage =
                    breakageService.getBreakageEmployee(testBreakage.getId(), testBreakage.getDepartment().getId());

            assertEquals(breakageEmployeeDto, returnedBreakage);

            verify(breakageRepository, times(1))
                    .getBreakageEmployee(testBreakage.getId());
        }

        @Test
        void whenGetBreakageByEmployeeThenReturnForbiddenException() {

            String message = "Данный пользователь не имеет право на получение информации по " +
                    "этой заявке на неисправность !!!";

            when(breakageRepository.getBreakageEmployee(testBreakage.getId()))
                    .thenReturn(Optional.of(breakageEmployeeDto));

            ForbiddenException exception = assertThrows(
                    ForbiddenException.class,
                    () -> breakageService.getBreakageEmployee(
                            testBreakage.getId(), SOME_NOT_EXIST_ID
                    )
            );

            assertEquals(message, exception.getMessage());

            verify(breakageRepository, times(1))
                    .getBreakageEmployee(testBreakage.getId());
        }

        @Test
        void whenGetBreakageByEmployeeThenReturnNotFoundException() {

            when(breakageRepository.getBreakageEmployee(testBreakage.getId()))
                    .thenThrow(new NotFoundException(BREAKAGE_NOT_EXIST));

            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> breakageService.getBreakageEmployee(
                            testBreakage.getId(), testBreakage.getDepartment().getId()
                    )
            );

            assertEquals(BREAKAGE_NOT_EXIST, exception.getMessage());

            verify(breakageRepository, times(1))
                    .getBreakageEmployee(testBreakage.getId());
        }
    }

    @Nested
    class WhenBreakageGettingAndBreakageCommentMethodsAreInvoked {

        private BreakageDto breakageDto;
        private BreakageFullDto breakageFullDto;
        private BreakageCommentBackendDto breakageCommentBackendDto;
        private BreakageCommentFrontDto breakageCommentFrontDto;

        @BeforeEach
        void setUp() {

            breakageDto = BreakageDto.builder()
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
                    .lastUpdatedBy(testBreakage.getLastUpdatedBy())
                    .lastUpdatedDate(testBreakage.getLastUpdatedDate())
                    .deadline(null)
                    .build();

            breakageCommentBackendDto = BreakageCommentBackendDto.builder()
                    .id(BREAKAGE_COMMENT_TEST_ID)
                    .comment(BREAKAGE_COMMENT_TEST_TEXT)
                    .createdBy(USER_TEST_ID)
                    .creatorName(USER_TEST_NAME)
                    .createdDate(now)
                    .lastUpdatedDate(now)
                    .build();

            breakageCommentFrontDto = BreakageCommentFrontDto.builder()
                    .id(BREAKAGE_COMMENT_TEST_ID)
                    .comment(BREAKAGE_COMMENT_TEST_TEXT)
                    .actionEnabled(true)
                    .creatorName(USER_TEST_NAME)
                    .createdDate(now)
                    .lastUpdatedDate(now)
                    .build();

            breakageFullDto = BreakageFullDto.builder()
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
                    .lastUpdatedBy(testBreakage.getLastUpdatedBy())
                    .lastUpdatedDate(testBreakage.getLastUpdatedDate())
                    .deadline(null)
                    .comments(List.of(breakageCommentFrontDto))
                    .build();
        }

        @Test
        void whenGetBreakageThenReturnNotFoundException() {

            when(breakageRepository.getBreakage(testBreakage.getId()))
                    .thenThrow(new NotFoundException(BREAKAGE_NOT_EXIST));

            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> breakageService.getBreakage(
                            testBreakage.getId(), DEFAULT_ADMIN_USER_ID
                    )
            );

            assertEquals(BREAKAGE_NOT_EXIST, exception.getMessage());

            verify(breakageRepository, times(1))
                    .getBreakage(testBreakage.getId());
        }

        @Test
        void whenGetBreakageThenReturnBreakage() {

            String responseMessage = "Заявка на неисправность с ID=" + testBreakage.getId() + ", получена успешно";

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(responseMessage)
                    .status(200)
                    .httpStatus(HttpStatus.OK)
                    .timestamp(now)
                    .data(breakageFullDto)
                    .build();

            when(breakageRepository.getBreakage(testBreakage.getId()))
                    .thenReturn(Optional.of(breakageDto));
            when(breakageCommentRepository.getAllBreakageComments(testBreakage.getId()))
                    .thenReturn(List.of(breakageCommentBackendDto));

            ApiResponse returnedApiResponse =
                    breakageService.getBreakage(testBreakage.getId(), USER_TEST_ID);

            assertEquals(apiResponse, returnedApiResponse);

            verify(breakageRepository, times(1))
                    .getBreakage(testBreakage.getId());
            verify(breakageCommentRepository, times(1))
                    .getAllBreakageComments(testBreakage.getId());
        }
    }


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