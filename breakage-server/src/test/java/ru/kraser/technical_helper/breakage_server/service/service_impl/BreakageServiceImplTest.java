package ru.kraser.technical_helper.breakage_server.service.service_impl;

import org.junit.jupiter.api.AfterEach;
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
import ru.kraser.technical_helper.common_module.enums.Status;
import ru.kraser.technical_helper.common_module.exception.AlreadyExistsException;
import ru.kraser.technical_helper.common_module.exception.NotFoundException;
import ru.kraser.technical_helper.common_module.model.Breakage;
import ru.kraser.technical_helper.common_module.model.Department;
import ru.kraser.technical_helper.common_module.model.User;
import ru.kraser.technical_helper.main_server.util.mapper.UserMapper;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ru.kraser.technical_helper.common_module.util.ConstantForTests.*;
import static ru.kraser.technical_helper.common_module.util.ConstantForTests.DEFAULT_ADMIN_USER_ID;

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
//
//    @Test
//    void cancelBreakage() {
//    }
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