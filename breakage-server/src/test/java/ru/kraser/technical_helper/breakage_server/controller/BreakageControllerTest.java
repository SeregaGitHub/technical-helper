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
import ru.kraser.technical_helper.common_module.dto.breakage.CreateBreakageFullDto;
import ru.kraser.technical_helper.common_module.model.Department;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
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

//        when(clock.getZone()).thenReturn(NOW_ZDT.getZone());
//        when(clock.instant()).thenReturn(NOW_ZDT.toInstant());
    }


//
//    @AfterEach
//    void tearDown() {
//    }

    @Nested
    class WhenBreakageCreating {

        private Department testDepartment;
        private CreateBreakageFullDto createBreakageFullDto;

        @BeforeEach
        void setUp() {

            testDepartment = Department.builder()
                    .id(DEPARTMENT_TEST_ID)
                    .name(DEPARTMENT_TEST_NAME)
                    .enabled(true)
                    .createdBy(DEFAULT_ADMIN_USER_ID)
                    .createdDate(now)
                    .lastUpdatedBy(DEFAULT_ADMIN_USER_ID)
                    .lastUpdatedDate(now)
                    .build();

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