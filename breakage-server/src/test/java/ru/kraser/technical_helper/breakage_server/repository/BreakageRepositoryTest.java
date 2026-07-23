package ru.kraser.technical_helper.breakage_server.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.kraser.technical_helper.BreakageServer;
import ru.kraser.technical_helper.common_module.dto.breakage.BreakageEmployeeDto;
import ru.kraser.technical_helper.common_module.enums.Priority;
import ru.kraser.technical_helper.common_module.enums.Role;
import ru.kraser.technical_helper.common_module.enums.Status;
import ru.kraser.technical_helper.common_module.model.Breakage;
import ru.kraser.technical_helper.common_module.model.Department;
import ru.kraser.technical_helper.common_module.model.User;
import ru.kraser.technical_helper.main_server.repository.DepartmentRepository;
import ru.kraser.technical_helper.main_server.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static ru.kraser.technical_helper.common_module.util.ConstantForTests.*;

@Testcontainers
@SpringBootTest
@ContextConfiguration(classes = BreakageServer.class)
class BreakageRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;
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
    @Sql(statements = "TRUNCATE TABLE breakage CASCADE")
    void tearDown() {

    }

    @Nested
    class WhenBreakageCreating {

        @Test
        void whenCreateBreakageWithNoExecutorThenReturnBreakage() {

            Breakage savedBreakage = breakageRepository.saveAndFlush(testBreakage);

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

            testBreakage.setDepartment(notExistDepartment);

            assertThrows(
                    DataIntegrityViolationException.class,
                    () -> breakageRepository.saveAndFlush(testBreakage)
            );
        }
    }

    @Nested
    class WhenBreakageStatusOrPriorityUpdating {

        private Breakage savedBreakage;
        private LocalDateTime afterNow;

        @BeforeEach
        void setUp() {

            afterNow = now.plusHours(1);
        }

        @Test
        @Transactional()
        @Modifying(clearAutomatically = true)
        void whenUpdateBreakageStatusThenReturnOne() {

            savedBreakage = breakageRepository.saveAndFlush(testBreakage);

            int response = breakageRepository.updateBreakageStatus(
                    savedBreakage.getId(),
                    Status.CANCELLED,
                    defaultAdminUser.getId(),
                    afterNow
            );

            entityManager.clear();
            Breakage updatedBreakage = breakageRepository.findById(savedBreakage.getId()).get();

            assertThat(response).isEqualTo(1);
            assertThat(updatedBreakage.getStatus()).isEqualTo(Status.CANCELLED);
        }

        @Test
        @Transactional()
        @Modifying(clearAutomatically = true)
        void updateBreakageStatusAndResetExecutor() {

            testBreakage.setExecutor(defaultAdminUser);
            testBreakage.setExecutorAppointedBy(defaultAdminUser);
            testBreakage.setDeadline(now);

            savedBreakage = breakageRepository.saveAndFlush(testBreakage);

            int response = breakageRepository.updateBreakageStatusAndResetExecutor(
                    savedBreakage.getId(),
                    Status.PAUSED,
                    defaultAdminUser.getId(),
                    afterNow
            );

            entityManager.clear();
            Breakage updatedBreakage = breakageRepository.findById(savedBreakage.getId()).get();

            assertThat(response).isEqualTo(1);
            assertThat(updatedBreakage.getExecutor()).isNull();
            assertThat(updatedBreakage.getExecutorAppointedBy()).isNull();
            assertThat(updatedBreakage.getDeadline()).isNull();
        }

        @Test
        @Transactional()
        @Modifying(clearAutomatically = true)
        void updateBreakagePriority() {

            savedBreakage = breakageRepository.saveAndFlush(testBreakage);

            int response = breakageRepository.updateBreakagePriority(
                    savedBreakage.getId(),
                    Priority.HIGH,
                    defaultAdminUser.getId(),
                    afterNow
            );

            entityManager.clear();
            Breakage updatedBreakage = breakageRepository.findById(savedBreakage.getId()).get();

            assertThat(response).isEqualTo(1);
            assertThat(updatedBreakage.getPriority()).isEqualTo(Priority.HIGH);
        }
    }

    @Nested
    class WhenBreakageExecutorAddingOrDropping {

        private Breakage savedBreakage;
        private LocalDateTime afterNow;

        @BeforeEach
        void setUp() {

            afterNow = now.plusHours(1);
        }

        @Test
        @Transactional()
        @Modifying(clearAutomatically = true)
        void addBreakageExecutor() {

            savedBreakage = breakageRepository.saveAndFlush(testBreakage);

            int response = breakageRepository.addBreakageExecutor(
                    savedBreakage.getId(),
                    defaultAdminUser.getId(),
                    afterNow,
                    defaultAdminUser.getId(),
                    afterNow
            );

            entityManager.clear();
            Breakage updatedBreakage = breakageRepository.findById(savedBreakage.getId()).get();

            assertThat(response).isEqualTo(1);
            assertThat(updatedBreakage.getExecutor().getId()).isEqualTo(defaultAdminUser.getId());
            assertThat(updatedBreakage.getExecutorAppointedBy().getId()).isEqualTo(defaultAdminUser.getId());
            assertThat(updatedBreakage.getDeadline()).isEqualTo(afterNow);
        }

        @Test
        @Transactional()
        @Modifying(clearAutomatically = true)
        void dropBreakageExecutor() {

            testBreakage.setExecutor(defaultAdminUser);
            testBreakage.setExecutorAppointedBy(defaultAdminUser);
            testBreakage.setDeadline(afterNow);

            savedBreakage = breakageRepository.saveAndFlush(testBreakage);

            int response = breakageRepository.dropBreakageExecutor(
                    savedBreakage.getId(),
                    defaultAdminUser.getId(),
                    afterNow
            );

            entityManager.clear();
            Breakage updatedBreakage = breakageRepository.findById(savedBreakage.getId()).get();

            assertThat(response).isEqualTo(1);
            assertThat(updatedBreakage.getExecutor()).isNull();
            assertThat(updatedBreakage.getExecutorAppointedBy()).isNull();
            assertThat(updatedBreakage.getDeadline()).isNull();
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class WhenGetBreakageMethodsAreInvoked {

        @Autowired
        private TransactionTemplate transactionTemplate;

        private Department employeeDepartment;
        private Department adminDepartment;
        private User employeeUser;
        private User adminUser;
        private LocalDateTime ldt;
        private Breakage savedByEmployeeBreakage;
        private Breakage savedByAdminBreakage;
        private PageRequest defaultPageRequest;
        List<Status> defaultEmployeeStatusList;
        List<Priority> defaultPriorityList;

        @BeforeAll
        void insertData() {

            adminUser = userRepository.findById(DEFAULT_ADMIN_USER_ID).get();
            adminDepartment = departmentRepository.findById(DEFAULT_ADMIN_DEPARTMENT_ID).get();
            ldt = LocalDateTime.of(
                    2025,
                    9,
                    29,
                    13,
                    0,
                    0);

            Department emplDepartment = Department.builder()
                    .name("employee_department")
                    .enabled(true)
                    .createdBy(adminUser.getId())
                    .createdDate(ldt)
                    .lastUpdatedBy(adminUser.getId())
                    .lastUpdatedDate(ldt)
                    .build();

            employeeDepartment = departmentRepository.saveAndFlush(emplDepartment);

            User emplUser = User.builder()
                    .username("employee_user")
                    .password(USER_TEST_PASSWORD)
                    .enabled(true)
                    .role(Role.EMPLOYEE)
                    .department(employeeDepartment)
                    .createdBy(adminUser.getId())
                    .createdDate(ldt)
                    .lastUpdatedBy(adminUser.getId())
                    .lastUpdatedDate(ldt)
                    .build();

            employeeUser = userRepository.saveAndFlush(emplUser);

            Breakage savedByEmplBreakage = Breakage.builder()
                    .department(employeeDepartment)
                    .room("some_room")
                    .breakageTopic("saved_by_employee_breakage_topic")
                    .breakageText("saved_by_employee_breakage_text")
                    .status(Status.NEW)
                    .priority(Priority.MEDIUM)
                    .executor(null)
                    .executorAppointedBy(null)
                    .deadline(null)
                    .createdBy(employeeUser.getId())
                    .createdDate(ldt)
                    .lastUpdatedBy(employeeUser.getId())
                    .lastUpdatedDate(ldt)
                    .build();

            Breakage savedByAdmBreakage = Breakage.builder()
                    .department(adminDepartment)
                    .room("some_room")
                    .breakageTopic("test_breakage_topic")
                    .breakageText("test_breakage_text")
                    .status(Status.NEW)
                    .priority(Priority.MEDIUM)
                    .executor(null)
                    .executorAppointedBy(null)
                    .deadline(null)
                    .createdBy(adminUser.getId())
                    .createdDate(ldt)
                    .lastUpdatedBy(adminUser.getId())
                    .lastUpdatedDate(ldt)
                    .build();

            savedByAdminBreakage = breakageRepository.saveAndFlush(savedByAdmBreakage);
            savedByEmployeeBreakage = breakageRepository.saveAndFlush(savedByEmplBreakage);

            defaultPageRequest = PageRequest.of(
                    0, 10, Sort.by(Sort.Direction.DESC, "lastUpdatedDate"));
            defaultEmployeeStatusList = List.of(Status.NEW, Status.IN_PROGRESS);
            defaultPriorityList = List.of(Priority.URGENTLY, Priority.HIGH, Priority.MEDIUM, Priority.LOW);
        }

        @AfterAll
        void cleanupData() {

            transactionTemplate.execute(status -> {
                entityManager.createNativeQuery("TRUNCATE TABLE breakage CASCADE")
                        .executeUpdate();
                return null;
            });

            userRepository.deleteById(employeeUser.getId());
            departmentRepository.deleteById(employeeDepartment.getId());
        }

        @Test
        void whenGetAllEmployeeBreakagesThenReturnListOfBreakageEmployeeDto() {

            Page<BreakageEmployeeDto> breakageEmployeeDtoPage = breakageRepository.getAllEmployeeBreakages(
                    defaultEmployeeStatusList,
                    defaultPriorityList,
                    employeeDepartment.getId(),
                    defaultPageRequest
            );

            List<BreakageEmployeeDto> list = breakageEmployeeDtoPage.getContent();

            assertThat(list.size()).isEqualTo(1);
        }
    }


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