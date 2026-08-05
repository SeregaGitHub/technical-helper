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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import ru.kraser.technical_helper.breakage_server.repository.BreakageCommentRepository;
import ru.kraser.technical_helper.breakage_server.repository.BreakageRepository;
import ru.kraser.technical_helper.breakage_server.util.mapper.BreakageCommentMapper;
import ru.kraser.technical_helper.breakage_server.util.mapper.BreakageMapper;
import ru.kraser.technical_helper.common_module.dto.api.ApiResponse;
import ru.kraser.technical_helper.common_module.dto.api.AppPage;
import ru.kraser.technical_helper.common_module.dto.breakage.*;
import ru.kraser.technical_helper.common_module.dto.breakage_comment.BreakageCommentBackendDto;
import ru.kraser.technical_helper.common_module.dto.breakage_comment.BreakageCommentFrontDto;
import ru.kraser.technical_helper.common_module.dto.breakage_comment.CreateBreakageCommentDto;
import ru.kraser.technical_helper.common_module.enums.Executor;
import ru.kraser.technical_helper.common_module.enums.Priority;
import ru.kraser.technical_helper.common_module.enums.Role;
import ru.kraser.technical_helper.common_module.enums.Status;
import ru.kraser.technical_helper.common_module.exception.ForbiddenException;
import ru.kraser.technical_helper.common_module.exception.NotCorrectParameter;
import ru.kraser.technical_helper.common_module.exception.NotFoundException;
import ru.kraser.technical_helper.common_module.model.Breakage;
import ru.kraser.technical_helper.common_module.model.BreakageComment;
import ru.kraser.technical_helper.common_module.model.Department;

import java.time.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static ru.kraser.technical_helper.common_module.util.Constant.BREAKAGE_COMMENT_NOT_EXIST;
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
                            Role.TECHNICIAN, DEFAULT_ADMIN_DEPARTMENT_ID, USER_TEST_NAME)
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
        private AppointBreakageExecutorDto appointBreakageExecutorDto;

        @BeforeEach
        void setUp() {

            afterNowDate = now.plusDays(1).toLocalDate();
            expectedDeadline = LocalDateTime.of(afterNowDate, LocalTime.of(23, 59, 59));
        }

        @Test
        void whenAddBreakageExecutorThenReturnOk() {

            appointBreakageExecutorDto = new AppointBreakageExecutorDto(USER_TEST_ID, afterNowDate, Status.NEW);

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

            appointBreakageExecutorDto = new AppointBreakageExecutorDto(USER_TEST_ID, afterNowDate, Status.NEW);

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

            appointBreakageExecutorDto = new AppointBreakageExecutorDto(SOME_NOT_EXIST_ID, afterNowDate, Status.NEW);

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

            appointBreakageExecutorDto = new AppointBreakageExecutorDto(USER_TEST_ID, beforeNowDate, Status.NEW);

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

            appointBreakageExecutorDto = new AppointBreakageExecutorDto(USER_TEST_ID, afterNowDate, Status.SOLVED);

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

    @Nested
    class WhenBreakagesGetting {

        private Integer pageSize;
        private Integer pageIndex;
        private String defaultSortBy;
        private String defaultSearchText;
        private List<Status> defaultStatusList;
        private List<Status> defaultShortStatusList;
        private List<Priority> defaultPriorityList;
        private PageRequest defaultPageRequest;

        @BeforeEach
        void setUp() {

            pageSize = 10;
            pageIndex = 0;
            defaultSortBy = "lastUpdatedDate";
            defaultSearchText = "breakage";

            defaultStatusList = new ArrayList<>();
            defaultStatusList.add(Status.NEW);
            defaultStatusList.add(Status.SOLVED);
            defaultStatusList.add(Status.IN_PROGRESS);
            defaultStatusList.add(Status.PAUSED);
            defaultStatusList.add(Status.REDIRECTED);
            defaultStatusList.add(Status.CANCELLED);

            defaultShortStatusList = new ArrayList<>();
            defaultShortStatusList.add(Status.NEW);
            defaultShortStatusList.add(Status.IN_PROGRESS);

            defaultPriorityList = new ArrayList<>();
            defaultPriorityList.add(Priority.URGENTLY);
            defaultPriorityList.add(Priority.HIGH);
            defaultPriorityList.add(Priority.MEDIUM);
            defaultPriorityList.add(Priority.LOW);

            Sort sort = Sort.by(Sort.Direction.DESC, defaultSortBy);
            defaultPageRequest = PageRequest.of(
                    0, 10, sort);
        }

        @Nested
        class WhenAllBreakagesGettingByEmployee {

            private BreakageEmployeeDto breakageEmployeeDto;
            private List<BreakageEmployeeDto> content;
            private Page<BreakageEmployeeDto> page;

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
                page = new PageImpl<>(content, defaultPageRequest, content.size());
            }

            @Test
            void whenGetAllEmployeeBreakagesThenReturnAppPage() {

                when(breakageRepository.getAllEmployeeBreakages(
                                defaultStatusList, defaultPriorityList, DEPARTMENT_TEST_ID, defaultPageRequest
                        )
                ).thenReturn(page);

                AppPage appPage =
                        breakageService.getAllBreakages(pageSize, pageIndex, defaultSortBy, "DESC",
                                true, true, true, true, true,
                                true, true, true, true, true,
                                Executor.NO_APPOINTED.name(), false, null,
                                Role.EMPLOYEE, DEPARTMENT_TEST_ID, USER_TEST_ID);

                assertEquals(content, appPage.content());

                verify(breakageRepository, times(1))
                        .getAllEmployeeBreakages(
                                defaultStatusList, defaultPriorityList, DEPARTMENT_TEST_ID, defaultPageRequest
                        );
            }

            @Test
            void whenGetAllEmployeeBreakagesByTextThenReturnAppPage() {

                when(breakageRepository.getAllEmployeeBreakagesByText(
                                defaultStatusList, defaultPriorityList,
                                DEPARTMENT_TEST_ID, defaultPageRequest, defaultSearchText
                        )
                ).thenReturn(page);

                AppPage appPage =
                        breakageService.getAllBreakages(pageSize, pageIndex, defaultSortBy, "DESC",
                                true, true, true, true, true,
                                true, true, true, true, true,
                                Executor.NO_APPOINTED.name(), false, defaultSearchText,
                                Role.EMPLOYEE, DEPARTMENT_TEST_ID, USER_TEST_ID);

                assertEquals(content, appPage.content());

                verify(breakageRepository, times(1))
                        .getAllEmployeeBreakagesByText(
                                defaultStatusList, defaultPriorityList,
                                DEPARTMENT_TEST_ID, defaultPageRequest, defaultSearchText
                        );
            }

            @Test
            void whenGetAllEmployeeBreakagesThenReturnEmptyAppPageContent() {

                defaultStatusList.remove(Status.NEW);
                page = new PageImpl<>(Collections.emptyList(), defaultPageRequest, 0);

                when(breakageRepository.getAllEmployeeBreakages(
                                defaultStatusList, defaultPriorityList, DEPARTMENT_TEST_ID, defaultPageRequest
                        )
                ).thenReturn(page);

                AppPage appPage =
                        breakageService.getAllBreakages(pageSize, pageIndex, defaultSortBy, "DESC",
                                false, true, true, true, true,
                                true, true, true, true, true,
                                Executor.NO_APPOINTED.name(), false, null,
                                Role.EMPLOYEE, DEPARTMENT_TEST_ID, USER_TEST_ID);

                assertEquals(Collections.emptyList(), appPage.content());

                verify(breakageRepository, times(1))
                        .getAllEmployeeBreakages(
                                defaultStatusList, defaultPriorityList, DEPARTMENT_TEST_ID, defaultPageRequest
                        );
            }
        }

        @Nested
        class WhenAllBreakagesGettingByTechnician {

            private BreakageTechDto breakageTechDto;
            private List<BreakageTechDto> content;
            private Page<BreakageTechDto> page;
            private LocalDateTime testDeadlineBeforeNow;

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
                                .priority(testBreakage.getPriority())
                                .breakageExecutor(USER_TEST_NAME)
                                .createdBy(testBreakage.getCreatedBy())
                                .createdDate(testBreakage.getCreatedDate())
                                .deadline(testDeadlineBeforeNow)
                                .build();

                        content = List.of(breakageTechDto);
                        page = new PageImpl<>(content, defaultPageRequest, content.size());
                    }

                    @Test
                    void whenGetAllBreakagesAppointedToMeWithDeadlineThenReturnAppPage() {

                        when(breakageRepository.getAllDeadlineExpiredBreakagesAppointedToMe(
                                        defaultShortStatusList, defaultPriorityList, defaultPageRequest, USER_TEST_ID, now
                                )
                        ).thenReturn(page);

                        AppPage appPage =
                                breakageService.getAllBreakages(pageSize, pageIndex, defaultSortBy, "DESC",
                                        true, true, true, true, true,
                                        true, true, true, true, true,
                                        Executor.APPOINTED_TO_ME.name(), true, null,
                                        Role.TECHNICIAN, DEFAULT_ADMIN_DEPARTMENT_ID, USER_TEST_ID);

                        assertEquals(content, appPage.content());

                        verify(breakageRepository, times(1))
                                .getAllDeadlineExpiredBreakagesAppointedToMe(
                                        defaultShortStatusList, defaultPriorityList, defaultPageRequest, USER_TEST_ID, now
                                );
                    }

                    @Test
                    void whenGetAllBreakagesByTextAppointedToMeWithDeadlineThenReturnAppPage() {

                        when(breakageRepository.getAllDeadlineExpiredBreakagesByTextAppointedToMe(
                                        defaultShortStatusList, defaultPriorityList, defaultPageRequest,
                                        USER_TEST_ID, now, defaultSearchText
                                )
                        ).thenReturn(page);

                        AppPage appPage =
                                breakageService.getAllBreakages(pageSize, pageIndex, defaultSortBy, "DESC",
                                        true, true, true, true, true,
                                        true, true, true, true, true,
                                        Executor.APPOINTED_TO_ME.name(), true, defaultSearchText,
                                        Role.TECHNICIAN, DEFAULT_ADMIN_DEPARTMENT_ID, USER_TEST_ID);

                        assertEquals(content, appPage.content());

                        verify(breakageRepository, times(1))
                                .getAllDeadlineExpiredBreakagesByTextAppointedToMe(
                                        defaultShortStatusList, defaultPriorityList, defaultPageRequest,
                                        USER_TEST_ID, now, defaultSearchText
                                );
                    }
                }

                @Nested
                class WhenAllBreakagesWithNoDeadlineAppointedToMeGetting {

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
                                .priority(testBreakage.getPriority())
                                .breakageExecutor(USER_TEST_NAME)
                                .createdBy(testBreakage.getCreatedBy())
                                .createdDate(testBreakage.getCreatedDate())
                                .deadline(null)
                                .build();

                        content = List.of(breakageTechDto);
                        page = new PageImpl<>(content, defaultPageRequest, content.size());
                    }

                    @Test
                    void whenGetAllBreakagesAppointedToMeWithNoDeadlineThenReturnAppPage() {

                        when(breakageRepository.getAllBreakagesAppointedToMe(
                                        defaultStatusList, defaultPriorityList, defaultPageRequest, USER_TEST_ID
                                )
                        ).thenReturn(page);

                        AppPage appPage =
                                breakageService.getAllBreakages(pageSize, pageIndex, defaultSortBy, "DESC",
                                        true, true, true, true, true,
                                        true, true, true, true, true,
                                        Executor.APPOINTED_TO_ME.name(), false, null,
                                        Role.TECHNICIAN, DEFAULT_ADMIN_DEPARTMENT_ID, USER_TEST_ID);

                        assertEquals(content, appPage.content());

                        verify(breakageRepository, times(1))
                                .getAllBreakagesAppointedToMe(
                                        defaultStatusList, defaultPriorityList, defaultPageRequest, USER_TEST_ID
                                );
                    }

                    @Test
                    void whenGetAllBreakagesByTextAppointedToMeWithNoDeadlineThenReturnAppPage() {

                        when(breakageRepository.getAllBreakagesByTextAppointedToMe(
                                        defaultStatusList, defaultPriorityList, defaultPageRequest,
                                        USER_TEST_ID, defaultSearchText
                                )
                        ).thenReturn(page);

                        AppPage appPage =
                                breakageService.getAllBreakages(pageSize, pageIndex, defaultSortBy, "DESC",
                                        true, true, true, true, true,
                                        true, true, true, true, true,
                                        Executor.APPOINTED_TO_ME.name(), false, defaultSearchText,
                                        Role.TECHNICIAN, DEFAULT_ADMIN_DEPARTMENT_ID, USER_TEST_ID);

                        assertEquals(content, appPage.content());

                        verify(breakageRepository, times(1))
                                .getAllBreakagesByTextAppointedToMe(
                                        defaultStatusList, defaultPriorityList, defaultPageRequest,
                                        USER_TEST_ID, defaultSearchText
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
                                .priority(testBreakage.getPriority())
                                .breakageExecutor(DEFAULT_ADMIN_USERNAME)
                                .createdBy(testBreakage.getCreatedBy())
                                .createdDate(testBreakage.getCreatedDate())
                                .deadline(testDeadlineBeforeNow)
                                .build();

                        content = List.of(breakageTechDto);
                        page = new PageImpl<>(content, defaultPageRequest, content.size());
                    }

                    @Test
                    void whenGetAllBreakagesAppointedToOthersWithDeadlineThenReturnAppPage() {

                        when(breakageRepository.getAllDeadlineExpiredBreakagesAppointedToOthers(
                                        defaultShortStatusList, defaultPriorityList, defaultPageRequest, USER_TEST_ID, now
                                )
                        ).thenReturn(page);

                        AppPage appPage =
                                breakageService.getAllBreakages(pageSize, pageIndex, defaultSortBy, "DESC",
                                        true, true, true, true, true,
                                        true, true, true, true, true,
                                        Executor.APPOINTED_TO_OTHERS.name(), true, null,
                                        Role.TECHNICIAN, DEFAULT_ADMIN_DEPARTMENT_ID, USER_TEST_ID);

                        assertEquals(content, appPage.content());

                        verify(breakageRepository, times(1))
                                .getAllDeadlineExpiredBreakagesAppointedToOthers(
                                        defaultShortStatusList, defaultPriorityList, defaultPageRequest, USER_TEST_ID, now
                                );
                    }

                    @Test
                    void whenGetAllBreakagesByTextAppointedToMeWithDeadlineThenReturnAppPage() {

                        when(breakageRepository.getAllDeadlineExpiredBreakagesByTextAppointedToOthers(
                                        defaultShortStatusList, defaultPriorityList, defaultPageRequest,
                                        USER_TEST_ID, now, defaultSearchText
                                )
                        ).thenReturn(page);

                        AppPage appPage =
                                breakageService.getAllBreakages(pageSize, pageIndex, defaultSortBy, "DESC",
                                        true, true, true, true, true,
                                        true, true, true, true, true,
                                        Executor.APPOINTED_TO_OTHERS.name(), true, defaultSearchText,
                                        Role.TECHNICIAN, DEFAULT_ADMIN_DEPARTMENT_ID, USER_TEST_ID);

                        assertEquals(content, appPage.content());

                        verify(breakageRepository, times(1))
                                .getAllDeadlineExpiredBreakagesByTextAppointedToOthers(
                                        defaultShortStatusList, defaultPriorityList, defaultPageRequest,
                                        USER_TEST_ID, now, defaultSearchText
                                );
                    }
                }

                @Nested
                class WhenAllBreakagesWithNoDeadlineAppointedToOthersGetting {

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
                                .priority(testBreakage.getPriority())
                                .breakageExecutor(DEPARTMENT_TEST_NAME)
                                .createdBy(testBreakage.getCreatedBy())
                                .createdDate(testBreakage.getCreatedDate())
                                .deadline(null)
                                .build();

                        content = List.of(breakageTechDto);
                        page = new PageImpl<>(content, defaultPageRequest, content.size());
                    }

                    @Test
                    void whenGetAllBreakagesAppointedToOthersWithNoDeadlineThenReturnAppPage() {

                        when(breakageRepository.getAllBreakagesAppointedToOthers(
                                        defaultStatusList, defaultPriorityList, defaultPageRequest, USER_TEST_ID
                                )
                        ).thenReturn(page);

                        AppPage appPage =
                                breakageService.getAllBreakages(pageSize, pageIndex, defaultSortBy, "DESC",
                                        true, true, true, true, true,
                                        true, true, true, true, true,
                                        Executor.APPOINTED_TO_OTHERS.name(), false, null,
                                        Role.TECHNICIAN, DEFAULT_ADMIN_DEPARTMENT_ID, USER_TEST_ID);

                        assertEquals(content, appPage.content());

                        verify(breakageRepository, times(1))
                                .getAllBreakagesAppointedToOthers(
                                        defaultStatusList, defaultPriorityList, defaultPageRequest, USER_TEST_ID
                                );
                    }

                    @Test
                    void whenGetAllBreakagesByTextAppointedToOthersWithNoDeadlineThenReturnAppPage() {

                        when(breakageRepository.getAllBreakagesByTextAppointedToOthers(
                                        defaultStatusList, defaultPriorityList, defaultPageRequest,
                                        USER_TEST_ID, defaultSearchText
                                )
                        ).thenReturn(page);

                        AppPage appPage =
                                breakageService.getAllBreakages(pageSize, pageIndex, defaultSortBy, "DESC",
                                        true, true, true, true, true,
                                        true, true, true, true, true,
                                        Executor.APPOINTED_TO_OTHERS.name(), false, defaultSearchText,
                                        Role.TECHNICIAN, DEFAULT_ADMIN_DEPARTMENT_ID, USER_TEST_ID);

                        assertEquals(content, appPage.content());

                        verify(breakageRepository, times(1))
                                .getAllBreakagesByTextAppointedToOthers(
                                        defaultStatusList, defaultPriorityList, defaultPageRequest,
                                        USER_TEST_ID, defaultSearchText
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
                            .priority(testBreakage.getPriority())
                            .breakageExecutor(null)
                            .createdBy(testBreakage.getCreatedBy())
                            .createdDate(testBreakage.getCreatedDate())
                            .deadline(null)
                            .build();

                    content = List.of(breakageTechDto);
                    page = new PageImpl<>(content, defaultPageRequest, content.size());
                }

                @Test
                void whenGetAllNoAppointedBreakagesThenReturnAppPage() {

                    when(breakageRepository.getAllBreakagesNoAppointed(
                                    defaultStatusList, defaultPriorityList, defaultPageRequest
                            )
                    ).thenReturn(page);

                    AppPage appPage =
                            breakageService.getAllBreakages(pageSize, pageIndex, defaultSortBy, "DESC",
                                    true, true, true, true, true,
                                    true, true, true, true, true,
                                    Executor.NO_APPOINTED.name(), false, null,
                                    Role.TECHNICIAN, DEFAULT_ADMIN_DEPARTMENT_ID, USER_TEST_ID);

                    assertEquals(content, appPage.content());

                    verify(breakageRepository, times(1))
                            .getAllBreakagesNoAppointed(
                                    defaultStatusList, defaultPriorityList, defaultPageRequest
                            );
                }

                @Test
                void whenGetAllNoAppointedBreakagesByTextThenReturnAppPage() {

                    when(breakageRepository.getAllBreakagesByTextNoAppointed(
                                    defaultStatusList, defaultPriorityList, defaultPageRequest, defaultSearchText
                            )
                    ).thenReturn(page);

                    AppPage appPage =
                            breakageService.getAllBreakages(pageSize, pageIndex, defaultSortBy, "DESC",
                                    true, true, true, true, true,
                                    true, true, true, true, true,
                                    Executor.NO_APPOINTED.name(), false, defaultSearchText,
                                    Role.TECHNICIAN, DEFAULT_ADMIN_DEPARTMENT_ID, USER_TEST_ID);

                    assertEquals(content, appPage.content());

                    verify(breakageRepository, times(1))
                            .getAllBreakagesByTextNoAppointed(
                                    defaultStatusList, defaultPriorityList, defaultPageRequest, defaultSearchText
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
                                .priority(testBreakage.getPriority())
                                .breakageExecutor(DEFAULT_ADMIN_USERNAME)
                                .createdBy(testBreakage.getCreatedBy())
                                .createdDate(testBreakage.getCreatedDate())
                                .deadline(testDeadlineBeforeNow)
                                .build();

                        content = List.of(breakageTechDto);
                        page = new PageImpl<>(content, defaultPageRequest, content.size());
                    }

                    @Test
                    void whenGetAllBreakagesWithDeadlineThenReturnAppPage() {

                        when(breakageRepository.getAllDeadlineExpiredBreakages(
                                        defaultShortStatusList, defaultPriorityList, defaultPageRequest, now
                                )
                        ).thenReturn(page);

                        AppPage appPage =
                                breakageService.getAllBreakages(pageSize, pageIndex, defaultSortBy, "DESC",
                                        true, true, true, true, true,
                                        true, true, true, true, true,
                                        "ALL", true, null,
                                        Role.TECHNICIAN, DEFAULT_ADMIN_DEPARTMENT_ID, USER_TEST_ID);

                        assertEquals(content, appPage.content());

                        verify(breakageRepository, times(1))
                                .getAllDeadlineExpiredBreakages(
                                        defaultShortStatusList, defaultPriorityList, defaultPageRequest, now
                                );
                    }

                    @Test
                    void whenGetAllBreakagesByTextWithDeadlineThenReturnAppPage() {

                        when(breakageRepository.getAllDeadlineExpiredBreakagesByText(
                                        defaultShortStatusList, defaultPriorityList, defaultPageRequest,
                                        now, defaultSearchText
                                )
                        ).thenReturn(page);

                        AppPage appPage =
                                breakageService.getAllBreakages(pageSize, pageIndex, defaultSortBy, "DESC",
                                        true, true, true, true, true,
                                        true, true, true, true, true,
                                        "ALL", true, defaultSearchText,
                                        Role.TECHNICIAN, DEFAULT_ADMIN_DEPARTMENT_ID, USER_TEST_ID);

                        assertEquals(content, appPage.content());

                        verify(breakageRepository, times(1))
                                .getAllDeadlineExpiredBreakagesByText(
                                        defaultShortStatusList, defaultPriorityList, defaultPageRequest,
                                        now, defaultSearchText
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
                                .priority(testBreakage.getPriority())
                                .breakageExecutor(null)
                                .createdBy(testBreakage.getCreatedBy())
                                .createdDate(testBreakage.getCreatedDate())
                                .deadline(null)
                                .build();

                        content = List.of(breakageTechDto);
                        page = new PageImpl<>(content, defaultPageRequest, content.size());
                    }

                    @Test
                    void whenGetAllBreakagesThenReturnAppPage() {

                        when(breakageRepository.getAllBreakages(
                                        defaultStatusList, defaultPriorityList, defaultPageRequest
                                )
                        ).thenReturn(page);

                        AppPage appPage =
                                breakageService.getAllBreakages(pageSize, pageIndex, defaultSortBy, "DESC",
                                        true, true, true, true, true,
                                        true, true, true, true, true,
                                        null, false, null,
                                        Role.TECHNICIAN, DEFAULT_ADMIN_DEPARTMENT_ID, USER_TEST_ID);

                        assertEquals(content, appPage.content());

                        verify(breakageRepository, times(1))
                                .getAllBreakages(
                                        defaultStatusList, defaultPriorityList, defaultPageRequest
                                );
                    }

                    @Test
                    void whenGetAllBreakagesByTextThenReturnAppPage() {

                        when(breakageRepository.getAllBreakagesByText(
                                        defaultStatusList, defaultPriorityList, defaultPageRequest, defaultSearchText
                                )
                        ).thenReturn(page);

                        AppPage appPage =
                                breakageService.getAllBreakages(pageSize, pageIndex, defaultSortBy, "DESC",
                                        true, true, true, true, true,
                                        true, true, true, true, true,
                                        null, false, defaultSearchText,
                                        Role.TECHNICIAN, DEFAULT_ADMIN_DEPARTMENT_ID, USER_TEST_ID);

                        assertEquals(content, appPage.content());

                        verify(breakageRepository, times(1))
                                .getAllBreakagesByText(
                                        defaultStatusList, defaultPriorityList, defaultPageRequest, defaultSearchText
                                );
                    }
                }

                @Nested
                class WhenAllBreakagesWithNoStatusOrPriorityGetting {

                    @BeforeEach
                    void setUp() {

                        page = new PageImpl<>(Collections.emptyList(), defaultPageRequest, 0);
                    }

                    @Test
                    void whenGetAllBreakagesIfNoStatusThenReturnEmptyAppPage() {

                        defaultStatusList.remove(Status.NEW);

                        when(breakageRepository.getAllBreakages(
                                        defaultStatusList, defaultPriorityList, defaultPageRequest
                                )
                        ).thenReturn(page);

                        AppPage appPage =
                                breakageService.getAllBreakages(pageSize, pageIndex, defaultSortBy, "DESC",
                                        false, true, true, true, true,
                                        true, true, true, true, true,
                                        null, false, null,
                                        Role.TECHNICIAN, DEFAULT_ADMIN_DEPARTMENT_ID, USER_TEST_ID);

                        assertEquals(Collections.emptyList(), appPage.content());

                        verify(breakageRepository, times(1))
                                .getAllBreakages(
                                        defaultStatusList, defaultPriorityList, defaultPageRequest
                                );
                    }

                    @Test
                    void whenGetAllBreakagesIfNoPriorityThenReturnEmptyAppPage() {

                        defaultPriorityList.remove(Priority.MEDIUM);

                        when(breakageRepository.getAllBreakages(
                                        defaultStatusList, defaultPriorityList, defaultPageRequest
                                )
                        ).thenReturn(page);

                        AppPage appPage =
                                breakageService.getAllBreakages(pageSize, pageIndex, defaultSortBy, "DESC",
                                        true, true, true, true, true,
                                        true, true, true, false, true,
                                        null, false, null,
                                        Role.TECHNICIAN, DEFAULT_ADMIN_DEPARTMENT_ID, USER_TEST_ID);

                        assertEquals(Collections.emptyList(), appPage.content());

                        verify(breakageRepository, times(1))
                                .getAllBreakages(
                                        defaultStatusList, defaultPriorityList, defaultPageRequest
                                );
                    }
                }
            }
        }
    }

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
        private BreakageComment breakageComment;
        private CreateBreakageCommentDto createBreakageCommentDto;
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

            breakageComment = BreakageComment.builder()
                    .id(BREAKAGE_COMMENT_TEST_ID)
                    .breakage(testBreakage)
                    .comment(BREAKAGE_COMMENT_TEST_TEXT)
                    .createdBy(testBreakage.getCreatedBy())
                    .createdDate(testBreakage.getCreatedDate())
                    .lastUpdatedBy(testBreakage.getLastUpdatedBy())
                    .lastUpdatedDate(testBreakage.getLastUpdatedDate())
                    .build();

            createBreakageCommentDto = new CreateBreakageCommentDto(BREAKAGE_COMMENT_TEST_TEXT, Status.IN_PROGRESS);

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
        void whenCreateBreakageCommentThenReturnCreated() {

            String responseMessage = "Комментарий к заявке о неисправности - был успешно создан.";

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(responseMessage)
                    .status(201)
                    .httpStatus(HttpStatus.CREATED)
                    .timestamp(now)
                    .build();

            when(breakageRepository.getReferenceById(testBreakage.getId()))
                    .thenReturn(testBreakage);
            when(breakageCommentRepository.saveAndFlush(breakageComment))
                    .thenReturn(breakageComment);

            ApiResponse returnedApiResponse =
                    breakageService.createBreakageComment(createBreakageCommentDto, testBreakage.getId(), USER_TEST_ID);

            assertEquals(apiResponse, returnedApiResponse);

            verify(breakageRepository, times(1))
                    .getReferenceById(testBreakage.getId());
            verify(breakageCommentRepository, times(1))
                    .saveAndFlush(
                            BreakageCommentMapper.toBreakageComment(
                                    createBreakageCommentDto, testBreakage, USER_TEST_ID, now
                            )
                    );
        }

        @Test
        void whenCreateBreakageCommentThenReturnThenReturnNotFoundException() {

            String message = "Заявки на неисправность не существует !!!";

            when(breakageRepository.getReferenceById(testBreakage.getId()))
                    .thenThrow(new NotFoundException(message));

            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> breakageService.createBreakageComment(
                            createBreakageCommentDto, testBreakage.getId(), USER_TEST_ID
                    )
            );

            assertEquals(message, exception.getMessage());

            verify(breakageRepository, times(1))
                    .getReferenceById(testBreakage.getId());
            verify(breakageCommentRepository, never())
                    .saveAndFlush(
                            BreakageCommentMapper.toBreakageComment(
                                    createBreakageCommentDto, testBreakage, USER_TEST_ID, now
                            )
                    );
        }

        @Test
        void whenCreateBreakageCommentThenReturnThenReturnNotCorrectParameter() {

            CreateBreakageCommentDto commentDto =
                    new CreateBreakageCommentDto(BREAKAGE_COMMENT_TEST_TEXT, Status.SOLVED);

            String message = "Комментарии к заявке о неисправности со статусами " +
                    "\"Решена\" и \"Отменена\" - не создаются !!!";

            NotCorrectParameter exception = assertThrows(
                    NotCorrectParameter.class,
                    () -> breakageService.createBreakageComment(
                            commentDto, testBreakage.getId(), USER_TEST_ID
                    )
            );

            assertEquals(message, exception.getMessage());

            verify(breakageRepository, never())
                    .getReferenceById(testBreakage.getId());
            verify(breakageCommentRepository, never())
                    .saveAndFlush(
                            BreakageCommentMapper.toBreakageComment(
                                    createBreakageCommentDto, testBreakage, USER_TEST_ID, now
                            )
                    );
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

            when(breakageCommentRepository.updateBreakageComment(
                    breakageComment.getId(), createBreakageCommentDto.comment(), USER_TEST_ID, now)
            ).thenReturn(1);

            ApiResponse returnedApiResponse = breakageService.updateBreakageComment(
                    createBreakageCommentDto, breakageComment.getId(), USER_TEST_ID
            );

            assertEquals(apiResponse, returnedApiResponse);

            verify(breakageCommentRepository, times(1))
                    .updateBreakageComment(
                            breakageComment.getId(), createBreakageCommentDto.comment(), USER_TEST_ID, now
                    );
        }

        @Test
        void whenUpdateBreakageCommentThenReturnNotFoundException() {

            when(breakageCommentRepository.updateBreakageComment(
                    SOME_NOT_EXIST_ID, createBreakageCommentDto.comment(), USER_TEST_ID, now)
            ).thenReturn(0);

            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> breakageService.updateBreakageComment(
                            createBreakageCommentDto, SOME_NOT_EXIST_ID, USER_TEST_ID
                    )
            );

            assertEquals(BREAKAGE_COMMENT_NOT_EXIST, exception.getMessage());

            verify(breakageCommentRepository, times(1))
                    .updateBreakageComment(
                            SOME_NOT_EXIST_ID, createBreakageCommentDto.comment(), USER_TEST_ID, now
                    );
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

            ApiResponse returnedApiResponse =
                    breakageService.deleteBreakageComment(breakageComment.getId());

            assertEquals(apiResponse, returnedApiResponse);

            verify(breakageCommentRepository, times(1))
                    .deleteById(breakageComment.getId());
        }
    }
}