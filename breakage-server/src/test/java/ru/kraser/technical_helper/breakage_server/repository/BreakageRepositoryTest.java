package ru.kraser.technical_helper.breakage_server.repository;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.kraser.technical_helper.BreakageServer;
import ru.kraser.technical_helper.common_module.enums.Priority;
import ru.kraser.technical_helper.common_module.enums.Status;
import ru.kraser.technical_helper.common_module.model.Breakage;
import ru.kraser.technical_helper.common_module.model.Department;
import ru.kraser.technical_helper.common_module.model.User;
import ru.kraser.technical_helper.main_server.repository.DepartmentRepository;
import ru.kraser.technical_helper.main_server.repository.UserRepository;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static ru.kraser.technical_helper.common_module.util.ConstantForTests.*;

@Testcontainers
@SpringBootTest
@ContextConfiguration(classes = BreakageServer.class)
class BreakageRepositoryTest {

    @Autowired
    BreakageRepository breakageRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    DepartmentRepository departmentRepository;

    private LocalDateTime now;
    private User defaultAdminUser;
    private Department defaultAdminDepartment;

    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(
            "postgres:17-alpine")
            .withDatabaseName("technical_helper")
            .withUsername("sa")
            .withPassword("sapassword")
            .withInitScript("schema.sql");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @BeforeAll
    @SneakyThrows
    static void beforeAll() {

        postgreSQLContainer.start();
    }

    @AfterAll
    static void afterAll() {

        postgreSQLContainer.stop();
    }

    @BeforeEach
    @SneakyThrows
    void setUp() {

        defaultAdminDepartment = departmentRepository.findById(DEFAULT_ADMIN_DEPARTMENT_ID).get();
        defaultAdminUser = userRepository.findById(DEFAULT_ADMIN_USER_ID).get();

        now = LocalDateTime.of(
                2025,
                9,
                29,
                13,
                0,
                0);
    }

    @Nested
    class WhenBreakageCreating {

        @Test
        void whenCreateBreakageWithNoExecutorThenReturnBreakage() {

            Breakage toSaveBreakage = Breakage.builder()
                    .department(defaultAdminDepartment)
                    .room("some_room")
                    .breakageTopic("test_breakage_topic")
                    .breakageText("test_breakage_text")
                    .status(Status.NEW)
                    .priority(Priority.MEDIUM)
                    .executor(null)
                    .executorAppointedBy(null)
                    .deadline(null)
                    .createdBy(defaultAdminUser.getId())
                    .createdDate(now)
                    .lastUpdatedBy(defaultAdminUser.getId())
                    .lastUpdatedDate(now)
                    .build();

            Breakage savedBreakage = breakageRepository.saveAndFlush(toSaveBreakage);

            assertThat(savedBreakage.getId()).isNotNull();
            assertThat(savedBreakage.getDepartment().getId()).isEqualTo(toSaveBreakage.getDepartment().getId());
            assertThat(savedBreakage.getRoom()).isEqualTo(toSaveBreakage.getRoom());
            assertThat(savedBreakage.getBreakageTopic()).isEqualTo(toSaveBreakage.getBreakageTopic());
            assertThat(savedBreakage.getBreakageText()).isEqualTo(toSaveBreakage.getBreakageText());
            assertThat(savedBreakage.getStatus()).isEqualTo(toSaveBreakage.getStatus());
            assertThat(savedBreakage.getPriority()).isEqualTo(toSaveBreakage.getPriority());
            assertThat(savedBreakage.getExecutor()).isNull();
            assertThat(savedBreakage.getExecutorAppointedBy()).isNull();
            assertThat(savedBreakage.getDeadline()).isNull();
            assertThat(savedBreakage.getCreatedBy()).isEqualTo(toSaveBreakage.getCreatedBy());
            assertThat(savedBreakage.getCreatedDate()).isEqualTo(toSaveBreakage.getCreatedDate());
            assertThat(savedBreakage.getLastUpdatedBy()).isEqualTo(toSaveBreakage.getLastUpdatedBy());
            assertThat(savedBreakage.getLastUpdatedDate()).isEqualTo(toSaveBreakage.getLastUpdatedDate());
        }

        @Test
        void whenCreateBreakageWithExecutorThenReturnBreakage() {

            Breakage toSaveBreakage = Breakage.builder()
                    .department(defaultAdminDepartment)
                    .room("some_room")
                    .breakageTopic("test_breakage_topic")
                    .breakageText("test_breakage_text")
                    .status(Status.NEW)
                    .priority(Priority.MEDIUM)
                    .executor(defaultAdminUser)
                    .executorAppointedBy(defaultAdminUser)
                    .deadline(now)
                    .createdBy(defaultAdminUser.getId())
                    .createdDate(now)
                    .lastUpdatedBy(defaultAdminUser.getId())
                    .lastUpdatedDate(now)
                    .build();

            Breakage savedBreakage = breakageRepository.saveAndFlush(toSaveBreakage);

            assertThat(savedBreakage.getId()).isNotNull();
            assertThat(savedBreakage.getDepartment().getId()).isEqualTo(toSaveBreakage.getDepartment().getId());
            assertThat(savedBreakage.getRoom()).isEqualTo(toSaveBreakage.getRoom());
            assertThat(savedBreakage.getBreakageTopic()).isEqualTo(toSaveBreakage.getBreakageTopic());
            assertThat(savedBreakage.getBreakageText()).isEqualTo(toSaveBreakage.getBreakageText());
            assertThat(savedBreakage.getStatus()).isEqualTo(toSaveBreakage.getStatus());
            assertThat(savedBreakage.getPriority()).isEqualTo(toSaveBreakage.getPriority());
            assertThat(savedBreakage.getExecutor()).isEqualTo(toSaveBreakage.getExecutor());
            assertThat(savedBreakage.getExecutorAppointedBy()).isEqualTo(toSaveBreakage.getExecutorAppointedBy());
            assertThat(savedBreakage.getDeadline()).isEqualTo(toSaveBreakage.getDeadline());
            assertThat(savedBreakage.getCreatedBy()).isEqualTo(toSaveBreakage.getCreatedBy());
            assertThat(savedBreakage.getCreatedDate()).isEqualTo(toSaveBreakage.getCreatedDate());
            assertThat(savedBreakage.getLastUpdatedBy()).isEqualTo(toSaveBreakage.getLastUpdatedBy());
            assertThat(savedBreakage.getLastUpdatedDate()).isEqualTo(toSaveBreakage.getLastUpdatedDate());
        }

        @Test
        void whenCreateBreakageWithNotNotExistDepartmentThenThrowException() {

            Department notExistDepartment = Department.builder()
                    .id(SOME_NOT_EXIST_ID)
                    .name(DEPARTMENT_TEST_NAME)
                    .createdBy(defaultAdminUser.getId())
                    .createdDate(now)
                    .lastUpdatedBy(defaultAdminUser.getId())
                    .lastUpdatedDate(now)
                    .build();

            Breakage toSaveBreakage = Breakage.builder()
                    .department(notExistDepartment)
                    .room("some_room")
                    .breakageTopic("test_breakage_topic")
                    .breakageText("test_breakage_text")
                    .status(Status.NEW)
                    .priority(Priority.MEDIUM)
                    .executor(null)
                    .executorAppointedBy(null)
                    .deadline(null)
                    .createdBy(defaultAdminUser.getId())
                    .createdDate(now)
                    .lastUpdatedBy(defaultAdminUser.getId())
                    .lastUpdatedDate(now)
                    .build();

            assertThrows(
                    DataIntegrityViolationException.class,
                    () -> breakageRepository.saveAndFlush(toSaveBreakage)
            );
        }
    }

//    @Test
//    void updateBreakageStatus() {
//    }
//
//    @Test
//    void updateBreakageStatusAndResetExecutor() {
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
//    void getAllEmployeeBreakages() {
//    }
//
//    @Test
//    void getAllEmployeeBreakagesByText() {
//    }
//
//    @Test
//    void getAllBreakagesAppointedToMe() {
//    }
//
//    @Test
//    void getAllBreakagesByTextAppointedToMe() {
//    }
//
//    @Test
//    void getAllDeadlineExpiredBreakagesAppointedToMe() {
//    }
//
//    @Test
//    void getAllDeadlineExpiredBreakagesByTextAppointedToMe() {
//    }
//
//    @Test
//    void getAllBreakagesAppointedToOthers() {
//    }
//
//    @Test
//    void getAllBreakagesByTextAppointedToOthers() {
//    }
//
//    @Test
//    void getAllDeadlineExpiredBreakagesAppointedToOthers() {
//    }
//
//    @Test
//    void getAllDeadlineExpiredBreakagesByTextAppointedToOthers() {
//    }
//
//    @Test
//    void getAllBreakagesNoAppointed() {
//    }
//
//    @Test
//    void getAllBreakagesByTextNoAppointed() {
//    }
//
//    @Test
//    void getAllBreakages() {
//    }
//
//    @Test
//    void getAllBreakagesByText() {
//    }
//
//    @Test
//    void getAllDeadlineExpiredBreakages() {
//    }
//
//    @Test
//    void getAllDeadlineExpiredBreakagesByText() {
//    }
//
//    @Test
//    void getBreakageEmployee() {
//    }
//
//    @Test
//    void getBreakage() {
//    }
}