package ru.kraser.technical_helper.breakage_server.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
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

//    @PersistenceContext
//    private EntityManager entityManager;
    @Autowired
    BreakageRepository breakageRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    DepartmentRepository departmentRepository;

    private LocalDateTime now;
    private User defaultAdminUser;
    private Department defaultAdminDepartment;
    private Breakage testBreakage;

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

    /*@Transactional(propagation = Propagation.REQUIRES_NEW)
    public void truncateTables() {
        entityManager.createNativeQuery(
                "TRUNCATE TABLE breakage_comment_audit, breakage_comment CASCADE").executeUpdate();
    }*/

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

        testBreakage = Breakage.builder()
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
    }

    @AfterEach
    //@Transactional
    //@Sql(statements = "TRUNCATE TABLE breakage_comment_audit, breakage_comment CASCADE")
    @Sql(statements = "TRUNCATE TABLE breakage CASCADE")
    void tearDown() {

        //truncateTables();

//        entityManager.createNativeQuery("TRUNCATE TABLE breakage_comment CASCADE").executeUpdate();
//        entityManager.createNativeQuery("TRUNCATE TABLE breakage CASCADE").executeUpdate();
    }

    @Nested
    class WhenBreakageCreating {

        @Test
        @SneakyThrows
        void whenCreateBreakageWithNoExecutorThenReturnBreakage() {

            /*Breakage toSaveBreakage = Breakage.builder()
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
                    .build();*/

            Breakage savedBreakage = breakageRepository.saveAndFlush(testBreakage);

/*            postgreSQLContainer.createConnection("DELETE FROM breakage_audit");

            breakageRepository.deleteById(savedBreakage.getId());*/

            assertThat(savedBreakage.getId()).isNotNull();
            assertThat(savedBreakage.getDepartment().getId()).isEqualTo(testBreakage.getDepartment().getId());
            assertThat(savedBreakage.getRoom()).isEqualTo(testBreakage.getRoom());
            assertThat(savedBreakage.getBreakageTopic()).isEqualTo(testBreakage.getBreakageTopic());
            assertThat(savedBreakage.getBreakageText()).isEqualTo(testBreakage.getBreakageText());
            assertThat(savedBreakage.getStatus()).isEqualTo(testBreakage.getStatus());
            assertThat(savedBreakage.getPriority()).isEqualTo(testBreakage.getPriority());
            assertThat(savedBreakage.getExecutor()).isNull();
            assertThat(savedBreakage.getExecutorAppointedBy()).isNull();
            assertThat(savedBreakage.getDeadline()).isNull();
            assertThat(savedBreakage.getCreatedBy()).isEqualTo(testBreakage.getCreatedBy());
            assertThat(savedBreakage.getCreatedDate()).isEqualTo(testBreakage.getCreatedDate());
            assertThat(savedBreakage.getLastUpdatedBy()).isEqualTo(testBreakage.getLastUpdatedBy());
            assertThat(savedBreakage.getLastUpdatedDate()).isEqualTo(testBreakage.getLastUpdatedDate());
        }

        @Test
        void whenCreateBreakageWithExecutorThenReturnBreakage() {

            /*Breakage toSaveBreakage = Breakage.builder()
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
                    .build();*/

            testBreakage.setExecutor(defaultAdminUser);
            testBreakage.setExecutorAppointedBy(defaultAdminUser);
            testBreakage.setDeadline(now);

            Breakage savedBreakage = breakageRepository.saveAndFlush(testBreakage);

            assertThat(savedBreakage.getId()).isNotNull();
            assertThat(savedBreakage.getDepartment().getId()).isEqualTo(testBreakage.getDepartment().getId());
            assertThat(savedBreakage.getRoom()).isEqualTo(testBreakage.getRoom());
            assertThat(savedBreakage.getBreakageTopic()).isEqualTo(testBreakage.getBreakageTopic());
            assertThat(savedBreakage.getBreakageText()).isEqualTo(testBreakage.getBreakageText());
            assertThat(savedBreakage.getStatus()).isEqualTo(testBreakage.getStatus());
            assertThat(savedBreakage.getPriority()).isEqualTo(testBreakage.getPriority());
            assertThat(savedBreakage.getExecutor()).isEqualTo(testBreakage.getExecutor());
            assertThat(savedBreakage.getExecutorAppointedBy()).isEqualTo(testBreakage.getExecutorAppointedBy());
            assertThat(savedBreakage.getDeadline()).isEqualTo(testBreakage.getDeadline());
            assertThat(savedBreakage.getCreatedBy()).isEqualTo(testBreakage.getCreatedBy());
            assertThat(savedBreakage.getCreatedDate()).isEqualTo(testBreakage.getCreatedDate());
            assertThat(savedBreakage.getLastUpdatedBy()).isEqualTo(testBreakage.getLastUpdatedBy());
            assertThat(savedBreakage.getLastUpdatedDate()).isEqualTo(testBreakage.getLastUpdatedDate());
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

            /*Breakage toSaveBreakage = Breakage.builder()
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
                    .build();*/

            testBreakage.setDepartment(notExistDepartment);

            assertThrows(
                    DataIntegrityViolationException.class,
                    () -> breakageRepository.saveAndFlush(testBreakage)
            );
        }
    }

    /*@Nested
    class WhenBreakageStatusUpdating {


    }*/

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