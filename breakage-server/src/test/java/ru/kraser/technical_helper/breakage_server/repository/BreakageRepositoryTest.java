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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.kraser.technical_helper.BreakageServer;
import ru.kraser.technical_helper.common_module.dto.breakage.BreakageDto;
import ru.kraser.technical_helper.common_module.dto.breakage.BreakageEmployeeDto;
import ru.kraser.technical_helper.common_module.dto.breakage.BreakageTechDto;
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
import java.util.Optional;

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

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class WhenBreakageRepositoryDataModifyingMethodsAreInvoked {

        @BeforeAll
        void insertData() {

            defaultAdminDepartment = departmentRepository.findById(DEFAULT_ADMIN_DEPARTMENT_ID).get();
            defaultAdminUser = userRepository.findById(DEFAULT_ADMIN_USER_ID).get();
        }

        @BeforeEach
        @SneakyThrows
        void setUp() {

            now = LocalDateTime.of(
                    2025,
                    9,
                    29,
                    13,
                    0,
                    0);

            testBreakage = Breakage.builder()
                    .department(defaultAdminDepartment)
                    .room(BREAKAGE_TEST_ROOM)
                    .breakageTopic(BREAKAGE_TEST_TOPIC)
                    .breakageText(BREAKAGE_TEST_TEXT)
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

        @Nested
        class WhenBreakageCreating {

            @Test
            @Transactional()
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
            @Transactional()
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
            void whenCreateBreakageWithNotExistDepartmentThenThrowException() {

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
            void whenUpdateBreakageStatusAndResetExecutorThenReturnOne() {

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
            void whenUpdateBreakagePriorityThenReturnOne() {

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
            void whenAddBreakageExecutorThenReturnOne() {

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
            void whenDropBreakageExecutorThenReturnOne() {

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
        private User technicianUser;
        private LocalDateTime ldt;
        private LocalDateTime beforeLdt;
        private Breakage savedByEmployeeBreakage;
        private Breakage savedByAdminBreakage;
        private PageRequest defaultPageRequest;
        List<Status> defaultStatusList;
        List<Status> defaultEmployeeStatusList;
        List<Status> statusListWithOnlyInProgress;
        List<Priority> defaultPriorityList;
        List<Priority> priorityListWithNoMedium;

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
            beforeLdt = ldt.minusDays(1);

            Department emplDepartment = Department.builder()
                    .name(DEPARTMENT_TEST_NAME)
                    .enabled(true)
                    .createdBy(adminUser.getId())
                    .createdDate(ldt)
                    .lastUpdatedBy(adminUser.getId())
                    .lastUpdatedDate(ldt)
                    .build();

            employeeDepartment = departmentRepository.saveAndFlush(emplDepartment);

            User emplUser = User.builder()
                    .username(USER_TEST_NAME)
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

            User techUser = User.builder()
                    .username(USER_TECHNICIAN_TEST_NAME)
                    .password(USER_TEST_PASSWORD)
                    .enabled(true)
                    .role(Role.TECHNICIAN)
                    .department(adminDepartment)
                    .createdBy(adminUser.getId())
                    .createdDate(ldt)
                    .lastUpdatedBy(adminUser.getId())
                    .lastUpdatedDate(ldt)
                    .build();

            technicianUser = userRepository.saveAndFlush(techUser);

            Breakage savedByEmplBreakage = Breakage.builder()
                    .department(employeeDepartment)
                    .room(BREAKAGE_TEST_ROOM)
                    .breakageTopic(BREAKAGE_TEST_EMPLOYEE_TOPIC)
                    .breakageText(BREAKAGE_TEST_EMPLOYEE_TEXT)
                    .status(Status.NEW)
                    .priority(Priority.MEDIUM)
                    .executor(technicianUser)
                    .executorAppointedBy(adminUser)
                    .deadline(beforeLdt)
                    .createdBy(employeeUser.getId())
                    .createdDate(ldt)
                    .lastUpdatedBy(employeeUser.getId())
                    .lastUpdatedDate(ldt)
                    .build();

            Breakage savedByAdmBreakage = Breakage.builder()
                    .department(adminDepartment)
                    .room(BREAKAGE_TEST_ROOM)
                    .breakageTopic(BREAKAGE_TEST_ADMIN_TOPIC)
                    .breakageText(BREAKAGE_TEST_ADMIN_TEXT)
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
            defaultStatusList = List.of(
                    Status.NEW, Status.IN_PROGRESS, Status.CANCELLED, Status.PAUSED, Status.REDIRECTED, Status.SOLVED);
            statusListWithOnlyInProgress = List.of(Status.IN_PROGRESS);
            defaultPriorityList = List.of(Priority.URGENTLY, Priority.HIGH, Priority.MEDIUM, Priority.LOW);
            priorityListWithNoMedium = List.of(Priority.URGENTLY, Priority.HIGH, Priority.LOW);
        }

        @AfterAll
        void cleanupData() {

            transactionTemplate.execute(status -> {
                entityManager.createNativeQuery("TRUNCATE TABLE breakage CASCADE")
                        .executeUpdate();
                return null;
            });

            userRepository.deleteById(employeeUser.getId());
            userRepository.deleteById(technicianUser.getId());
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

        @Test
        void whenGetAllEmployeeBreakagesByTextThenReturnListOfBreakageEmployeeDto() {

            Page<BreakageEmployeeDto> breakageEmployeeDtoPage = breakageRepository.getAllEmployeeBreakagesByText(
                    defaultEmployeeStatusList,
                    defaultPriorityList,
                    employeeDepartment.getId(),
                    defaultPageRequest,
                    BREAKAGE_TEST_SEARCH_BY_EMPLOYEE_TEXT
            );

            List<BreakageEmployeeDto> list = breakageEmployeeDtoPage.getContent();

            assertThat(list.size()).isEqualTo(1);
        }

        @Test
        void whenGetAllEmployeeBreakagesByTextIfNotExistThenReturnEmptyList() {

            Page<BreakageEmployeeDto> breakageEmployeeDtoPage = breakageRepository.getAllEmployeeBreakagesByText(
                    defaultEmployeeStatusList,
                    defaultPriorityList,
                    employeeDepartment.getId(),
                    defaultPageRequest,
                    SOME_NOT_EXIST_TEXT
            );

            List<BreakageEmployeeDto> list = breakageEmployeeDtoPage.getContent();

            assertThat(list).isEmpty();
        }

        @Test
        void whenGetAllEmployeeBreakagesIfStatusNotSelectedThenReturnEmptyList() {

            Page<BreakageEmployeeDto> breakageEmployeeDtoPage = breakageRepository.getAllEmployeeBreakages(
                    statusListWithOnlyInProgress,
                    defaultPriorityList,
                    employeeDepartment.getId(),
                    defaultPageRequest
            );

            List<BreakageEmployeeDto> list = breakageEmployeeDtoPage.getContent();

            assertThat(list).isEmpty();
        }

        @Test
        void whenGetAllEmployeeBreakagesByTextIfStatusNotSelectedThenReturnEmptyList() {

            List<Status> employeeStatusList = List.of(Status.IN_PROGRESS);

            Page<BreakageEmployeeDto> breakageEmployeeDtoPage = breakageRepository.getAllEmployeeBreakagesByText(
                    employeeStatusList,
                    defaultPriorityList,
                    employeeDepartment.getId(),
                    defaultPageRequest,
                    BREAKAGE_TEST_SEARCH_BY_EMPLOYEE_TEXT
            );

            List<BreakageEmployeeDto> list = breakageEmployeeDtoPage.getContent();

            assertThat(list).isEmpty();
        }

        @Test
        void whenGetAllEmployeeBreakagesIfPriorityNotSelectedThenReturnEmptyList() {

            Page<BreakageEmployeeDto> breakageEmployeeDtoPage = breakageRepository.getAllEmployeeBreakages(
                    defaultEmployeeStatusList,
                    priorityListWithNoMedium,
                    employeeDepartment.getId(),
                    defaultPageRequest
            );

            List<BreakageEmployeeDto> list = breakageEmployeeDtoPage.getContent();

            assertThat(list).isEmpty();
        }

        @Test
        void whenGetAllEmployeeBreakagesByTextIfPriorityNotSelectedThenReturnEmptyList() {

            Page<BreakageEmployeeDto> breakageEmployeeDtoPage = breakageRepository.getAllEmployeeBreakagesByText(
                    defaultEmployeeStatusList,
                    priorityListWithNoMedium,
                    employeeDepartment.getId(),
                    defaultPageRequest,
                    BREAKAGE_TEST_SEARCH_BY_EMPLOYEE_TEXT
            );

            List<BreakageEmployeeDto> list = breakageEmployeeDtoPage.getContent();

            assertThat(list).isEmpty();
        }

        @Test
        void whenGetAllBreakagesAppointedToMeThenReturnListOfBreakages() {

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllBreakagesAppointedToMe(
                    defaultStatusList,
                    defaultPriorityList,
                    defaultPageRequest,
                    technicianUser.getId()
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list.size()).isEqualTo(1);
        }

        @Test
        void whenGetAllBreakagesAppointedToMeIfStatusNotSelectedThenReturnEmptyList() {

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllBreakagesAppointedToMe(
                    statusListWithOnlyInProgress,
                    defaultPriorityList,
                    defaultPageRequest,
                    technicianUser.getId()
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list).isEmpty();
        }

        @Test
        void whenGetAllBreakagesAppointedToMeIfPriorityNotSelectedThenReturnEmptyList() {

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllBreakagesAppointedToMe(
                    defaultStatusList,
                    priorityListWithNoMedium,
                    defaultPageRequest,
                    technicianUser.getId()
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list).isEmpty();
        }

        @Test
        void whenGetAllBreakagesAppointedToMeThenReturnEmptyList() {

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllBreakagesAppointedToMe(
                    defaultStatusList,
                    defaultPriorityList,
                    defaultPageRequest,
                    adminUser.getId()
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list).isEmpty();
        }

        @Test
        void whenGetAllBreakagesByTextAppointedToMeThenReturnListOfBreakages() {

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllBreakagesByTextAppointedToMe(
                    defaultStatusList,
                    defaultPriorityList,
                    defaultPageRequest,
                    technicianUser.getId(),
                    BREAKAGE_TEST_SEARCH_BY_EMPLOYEE_TEXT
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list.size()).isEqualTo(1);
        }

        @Test
        void whenGetAllBreakagesByTextAppointedToMeIfStatusNotSelectedThenReturnEmptyList() {

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllBreakagesByTextAppointedToMe(
                    statusListWithOnlyInProgress,
                    defaultPriorityList,
                    defaultPageRequest,
                    technicianUser.getId(),
                    BREAKAGE_TEST_SEARCH_BY_EMPLOYEE_TEXT
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list).isEmpty();
        }

        @Test
        void whenGetAllBreakagesByTextAppointedToMeIfPriorityNotSelectedThenReturnEmptyList() {

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllBreakagesByTextAppointedToMe(
                    defaultStatusList,
                    priorityListWithNoMedium,
                    defaultPageRequest,
                    technicianUser.getId(),
                    BREAKAGE_TEST_SEARCH_BY_EMPLOYEE_TEXT
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list).isEmpty();
        }

        @Test
        void whenGetAllBreakagesByTextAppointedToMeThenReturnEmptyList() {

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllBreakagesByTextAppointedToMe(
                    defaultStatusList,
                    defaultPriorityList,
                    defaultPageRequest,
                    adminUser.getId(),
                    BREAKAGE_TEST_SEARCH_BY_EMPLOYEE_TEXT
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list).isEmpty();
        }

        @Test
        void whenGetAllDeadlineExpiredBreakagesAppointedToMeThenReturnListOfBreakages() {

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllDeadlineExpiredBreakagesAppointedToMe(
                    defaultStatusList,
                    defaultPriorityList,
                    defaultPageRequest,
                    technicianUser.getId(),
                    ldt
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list.size()).isEqualTo(1);
        }

        @Test
        void whenGetAllDeadlineExpiredBreakagesAppointedToMeIfStatusNotSelectedThenReturnEmptyList() {

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllDeadlineExpiredBreakagesAppointedToMe(
                    statusListWithOnlyInProgress,
                    defaultPriorityList,
                    defaultPageRequest,
                    technicianUser.getId(),
                    ldt
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list).isEmpty();
        }

        @Test
        void whenGetAllDeadlineExpiredBreakagesAppointedToMeIfPriorityNotSelectedThenReturnEmptyList() {

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllDeadlineExpiredBreakagesAppointedToMe(
                    defaultStatusList,
                    priorityListWithNoMedium,
                    defaultPageRequest,
                    technicianUser.getId(),
                    ldt
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list).isEmpty();
        }

        @Test
        void whenGetAllDeadlineExpiredBreakagesAppointedToMeThenReturnEmptyList() {

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllDeadlineExpiredBreakagesAppointedToMe(
                    defaultStatusList,
                    defaultPriorityList,
                    defaultPageRequest,
                    adminUser.getId(),
                    ldt
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list).isEmpty();
        }

        @Test
        void whenGetAllDeadlineExpiredBreakagesByTextAppointedToMeThenReturnListOfBreakages() {

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllDeadlineExpiredBreakagesByTextAppointedToMe(
                    defaultStatusList,
                    defaultPriorityList,
                    defaultPageRequest,
                    technicianUser.getId(),
                    ldt,
                    BREAKAGE_TEST_SEARCH_BY_EMPLOYEE_TEXT
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list.size()).isEqualTo(1);
        }

        @Test
        void whenGetAllDeadlineExpiredBreakagesByTextAppointedToMeIfStatusNotSelectedThenReturnEmptyList() {

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllDeadlineExpiredBreakagesByTextAppointedToMe(
                    statusListWithOnlyInProgress,
                    defaultPriorityList,
                    defaultPageRequest,
                    technicianUser.getId(),
                    ldt,
                    BREAKAGE_TEST_SEARCH_BY_EMPLOYEE_TEXT
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list).isEmpty();
        }

        @Test
        void whenGetAllDeadlineExpiredBreakagesByTextAppointedToMeIfPriorityNotSelectedThenReturnEmptyList() {

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllDeadlineExpiredBreakagesByTextAppointedToMe(
                    defaultStatusList,
                    priorityListWithNoMedium,
                    defaultPageRequest,
                    technicianUser.getId(),
                    ldt,
                    BREAKAGE_TEST_SEARCH_BY_EMPLOYEE_TEXT
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list).isEmpty();
        }

        @Test
        void whenGetAllDeadlineExpiredBreakagesByTextAppointedToMeThenReturnEmptyList() {

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllDeadlineExpiredBreakagesByTextAppointedToMe(
                    defaultStatusList,
                    defaultPriorityList,
                    defaultPageRequest,
                    adminUser.getId(),
                    ldt,
                    BREAKAGE_TEST_SEARCH_BY_EMPLOYEE_TEXT
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list).isEmpty();
        }

        @Test
        void whenGetAllBreakagesAppointedToOthersThenReturnListOfBreakages() {

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllBreakagesAppointedToOthers(
                    defaultStatusList,
                    defaultPriorityList,
                    defaultPageRequest,
                    adminUser.getId()
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list.size()).isEqualTo(1);
        }

        @Test
        void whenGetAllBreakagesAppointedToOthersIfStatusNotSelectedThenReturnEmptyList() {

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllBreakagesAppointedToOthers(
                    statusListWithOnlyInProgress,
                    defaultPriorityList,
                    defaultPageRequest,
                    adminUser.getId()
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list).isEmpty();
        }

        @Test
        void whenGetAllBreakagesAppointedToOthersIfPriorityNotSelectedThenReturnEmptyList() {

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllBreakagesAppointedToOthers(
                    defaultStatusList,
                    priorityListWithNoMedium,
                    defaultPageRequest,
                    adminUser.getId()
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list).isEmpty();
        }

        @Test
        void whenGetAllBreakagesAppointedToOthersThenReturnEmptyList() {

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllBreakagesAppointedToOthers(
                    defaultStatusList,
                    defaultPriorityList,
                    defaultPageRequest,
                    technicianUser.getId()
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list).isEmpty();
        }

        @Test
        void whenGetAllBreakagesByTextAppointedToOthersThenReturnListOfBreakages() {

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllBreakagesByTextAppointedToOthers(
                    defaultStatusList,
                    defaultPriorityList,
                    defaultPageRequest,
                    adminUser.getId(),
                    BREAKAGE_TEST_SEARCH_BY_EMPLOYEE_TEXT
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list.size()).isEqualTo(1);
        }

        @Test
        void whenGetAllBreakagesByTextAppointedToOthersIfStatusNotSelectedThenReturnEmptyList() {

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllBreakagesByTextAppointedToOthers(
                    statusListWithOnlyInProgress,
                    defaultPriorityList,
                    defaultPageRequest,
                    adminUser.getId(),
                    BREAKAGE_TEST_SEARCH_BY_EMPLOYEE_TEXT
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list).isEmpty();
        }

        @Test
        void whenGetAllBreakagesByTextAppointedToOthersIfPriorityNotSelectedThenReturnEmptyList() {

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllBreakagesByTextAppointedToOthers(
                    defaultStatusList,
                    priorityListWithNoMedium,
                    defaultPageRequest,
                    adminUser.getId(),
                    BREAKAGE_TEST_SEARCH_BY_EMPLOYEE_TEXT
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list).isEmpty();
        }

        @Test
        void whenGetAllBreakagesByTextAppointedToOthersThenReturnEmptyList() {

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllBreakagesByTextAppointedToOthers(
                    defaultStatusList,
                    defaultPriorityList,
                    defaultPageRequest,
                    technicianUser.getId(),
                    BREAKAGE_TEST_SEARCH_BY_EMPLOYEE_TEXT
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list).isEmpty();
        }

        @Test
        void whenGetAllDeadlineExpiredBreakagesAppointedToOthersThenReturnListOfBreakages() {

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllDeadlineExpiredBreakagesAppointedToOthers(
                    defaultStatusList,
                    defaultPriorityList,
                    defaultPageRequest,
                    adminUser.getId(),
                    ldt
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list.size()).isEqualTo(1);
        }

        @Test
        void whenGetAllDeadlineExpiredBreakagesAppointedToOthersIfStatusNotSelectedThenReturnEmptyList() {

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllDeadlineExpiredBreakagesAppointedToOthers(
                    statusListWithOnlyInProgress,
                    defaultPriorityList,
                    defaultPageRequest,
                    adminUser.getId(),
                    ldt
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list).isEmpty();
        }

        @Test
        void whenGetAllDeadlineExpiredBreakagesAppointedToOthersIfPriorityNotSelectedThenReturnEmptyList() {

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllDeadlineExpiredBreakagesAppointedToOthers(
                    defaultStatusList,
                    priorityListWithNoMedium,
                    defaultPageRequest,
                    adminUser.getId(),
                    ldt
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list).isEmpty();
        }

        @Test
        void whenGetAllDeadlineExpiredBreakagesAppointedToOthersThenReturnEmptyList() {

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllDeadlineExpiredBreakagesAppointedToOthers(
                    defaultStatusList,
                    defaultPriorityList,
                    defaultPageRequest,
                    technicianUser.getId(),
                    ldt
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list).isEmpty();
        }

        @Test
        void whenGetAllDeadlineExpiredBreakagesByTextAppointedToOthersThenReturnListOfBreakages() {

            Page<BreakageTechDto> breakagesPage =
                    breakageRepository.getAllDeadlineExpiredBreakagesByTextAppointedToOthers(
                            defaultStatusList,
                            defaultPriorityList,
                            defaultPageRequest,
                            adminUser.getId(),
                            ldt,
                            BREAKAGE_TEST_SEARCH_BY_EMPLOYEE_TEXT
                    );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list.size()).isEqualTo(1);
        }

        @Test
        void whenGetAllDeadlineExpiredBreakagesByTextAppointedToOthersIfStatusNotSelectedThenReturnEmptyList() {

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllDeadlineExpiredBreakagesByTextAppointedToOthers(
                    statusListWithOnlyInProgress,
                    defaultPriorityList,
                    defaultPageRequest,
                    adminUser.getId(),
                    ldt,
                    BREAKAGE_TEST_SEARCH_BY_EMPLOYEE_TEXT
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list).isEmpty();
        }

        @Test
        void whenGetAllDeadlineExpiredBreakagesByTextAppointedToOthersIfPriorityNotSelectedThenReturnEmptyList() {

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllDeadlineExpiredBreakagesByTextAppointedToOthers(
                    defaultStatusList,
                    priorityListWithNoMedium,
                    defaultPageRequest,
                    adminUser.getId(),
                    ldt,
                    BREAKAGE_TEST_SEARCH_BY_EMPLOYEE_TEXT
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list).isEmpty();
        }

        @Test
        void whenGetAllDeadlineExpiredBreakagesByTextAppointedToOthersThenReturnEmptyList() {

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllDeadlineExpiredBreakagesByTextAppointedToOthers(
                    defaultStatusList,
                    defaultPriorityList,
                    defaultPageRequest,
                    technicianUser.getId(),
                    ldt,
                    BREAKAGE_TEST_SEARCH_BY_EMPLOYEE_TEXT
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list).isEmpty();
        }

        @Test
        void whenGetAllBreakagesNoAppointedThenReturnListOfBreakages() {

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllBreakagesNoAppointed(
                    defaultStatusList,
                    defaultPriorityList,
                    defaultPageRequest
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list.size()).isEqualTo(1);
        }

        @Test
        void whenGetAllBreakagesNoAppointedIfStatusNotSelectedThenReturnEmptyList() {

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllBreakagesNoAppointed(
                    statusListWithOnlyInProgress,
                    defaultPriorityList,
                    defaultPageRequest
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list).isEmpty();
        }

        @Test
        void whenGetAllBreakagesNoAppointedIfPriorityNotSelectedThenReturnEmptyList() {

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllBreakagesNoAppointed(
                    defaultStatusList,
                    priorityListWithNoMedium,
                    defaultPageRequest
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list).isEmpty();
        }

        @Test
        void whenGetAllBreakagesByTextNoAppointedThenReturnListOfBreakages() {

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllBreakagesByTextNoAppointed(
                    defaultStatusList,
                    defaultPriorityList,
                    defaultPageRequest,
                    BREAKAGE_TEST_SEARCH_BY_ADMIN_TEXT
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list.size()).isEqualTo(1);
        }

        @Test
        void whenGetAllBreakagesByTextNoAppointedIfStatusNotSelectedThenReturnEmptyList() {

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllBreakagesByTextNoAppointed(
                    statusListWithOnlyInProgress,
                    defaultPriorityList,
                    defaultPageRequest,
                    BREAKAGE_TEST_SEARCH_BY_ADMIN_TEXT
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list).isEmpty();
        }

        @Test
        void whenGetAllBreakagesByTextNoAppointedIfPriorityNotSelectedThenReturnEmptyList() {

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllBreakagesByTextNoAppointed(
                    defaultStatusList,
                    priorityListWithNoMedium,
                    defaultPageRequest,
                    BREAKAGE_TEST_SEARCH_BY_ADMIN_TEXT
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list).isEmpty();
        }

        @Test
        void whenGetAllBreakagesByTextNoAppointedThenReturnEmptyList() {

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllBreakagesByTextNoAppointed(
                    defaultStatusList,
                    defaultPriorityList,
                    defaultPageRequest,
                    BREAKAGE_TEST_SEARCH_BY_EMPLOYEE_TEXT
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list).isEmpty();
        }

        @Test
        void whenGetAllBreakagesThenReturnListOfBreakages() {

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllBreakages(
                    defaultStatusList,
                    defaultPriorityList,
                    defaultPageRequest
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list.size()).isEqualTo(2);
        }

        @Test
        void whenGetAllBreakagesIfStatusNotSelectedThenReturnEmptyList() {

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllBreakages(
                    statusListWithOnlyInProgress,
                    defaultPriorityList,
                    defaultPageRequest
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list).isEmpty();
        }

        @Test
        void whenGetAllBreakagesIfPriorityNotSelectedThenReturnEmptyList() {

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllBreakages(
                    defaultStatusList,
                    priorityListWithNoMedium,
                    defaultPageRequest
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list).isEmpty();
        }

        @Test
        void whenGetAllBreakagesByTextThenReturnListOfBreakages() {

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllBreakagesByText(
                    defaultStatusList,
                    defaultPriorityList,
                    defaultPageRequest,
                    BREAKAGE_TEST_SEARCH_TEXT
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list.size()).isEqualTo(2);
        }

        @Test
        void whenGetAllBreakagesByTextIfStatusNotSelectedThenReturnEmptyList() {

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllBreakagesByText(
                    statusListWithOnlyInProgress,
                    defaultPriorityList,
                    defaultPageRequest,
                    BREAKAGE_TEST_SEARCH_TEXT
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list).isEmpty();
        }

        @Test
        void whenGetAllBreakagesByTextIfPriorityNotSelectedThenReturnEmptyList() {

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllBreakagesByText(
                    defaultStatusList,
                    priorityListWithNoMedium,
                    defaultPageRequest,
                    BREAKAGE_TEST_SEARCH_TEXT
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list).isEmpty();
        }

        @Test
        void whenGetAllBreakagesByTextThenReturnListOfEmployeeBreakages() {

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllBreakagesByText(
                    defaultStatusList,
                    defaultPriorityList,
                    defaultPageRequest,
                    BREAKAGE_TEST_SEARCH_BY_EMPLOYEE_TEXT
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list.size()).isEqualTo(1);
        }

        @Test
        void whenGetAllDeadlineExpiredBreakagesThenReturnListOfBreakages() {

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllDeadlineExpiredBreakages(
                    defaultStatusList,
                    defaultPriorityList,
                    defaultPageRequest,
                    ldt
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list.size()).isEqualTo(1);
        }

        @Test
        void whenGetAllDeadlineExpiredBreakagesIfStatusNotSelectedThenReturnEmptyList() {

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllDeadlineExpiredBreakages(
                    statusListWithOnlyInProgress,
                    defaultPriorityList,
                    defaultPageRequest,
                    ldt
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list).isEmpty();
        }

        @Test
        void whenGetAllDeadlineExpiredBreakagesIfPriorityNotSelectedThenReturnEmptyList() {

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllDeadlineExpiredBreakages(
                    defaultStatusList,
                    priorityListWithNoMedium,
                    defaultPageRequest,
                    ldt
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list).isEmpty();
        }

        @Test
        void whenGetAllDeadlineExpiredBreakagesThenReturnEmptyList() {

            LocalDateTime localDateTime = beforeLdt.minusDays(2);

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllDeadlineExpiredBreakages(
                    defaultStatusList,
                    defaultPriorityList,
                    defaultPageRequest,
                    localDateTime
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list).isEmpty();
        }

        @Test
        void whenGetAllDeadlineExpiredBreakagesByTextThenReturnListOfBreakages() {

            Page<BreakageTechDto> breakagesPage =
                    breakageRepository.getAllDeadlineExpiredBreakagesByText(
                            defaultStatusList,
                            defaultPriorityList,
                            defaultPageRequest,
                            ldt,
                            BREAKAGE_TEST_SEARCH_TEXT
                    );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list.size()).isEqualTo(1);
        }

        @Test
        void whenGetAllDeadlineExpiredBreakagesByTextIfStatusNotSelectedThenReturnEmptyList() {

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllDeadlineExpiredBreakagesByText(
                    statusListWithOnlyInProgress,
                    defaultPriorityList,
                    defaultPageRequest,
                    ldt,
                    BREAKAGE_TEST_SEARCH_TEXT
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list).isEmpty();
        }

        @Test
        void whenGetAllDeadlineExpiredBreakagesByTextIfPriorityNotSelectedThenReturnEmptyList() {

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllDeadlineExpiredBreakagesByText(
                    defaultStatusList,
                    priorityListWithNoMedium,
                    defaultPageRequest,
                    ldt,
                    BREAKAGE_TEST_SEARCH_TEXT
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list).isEmpty();
        }

        @Test
        void whenGetAllDeadlineExpiredBreakagesByTextThenReturnEmptyList() {

            LocalDateTime localDateTime = beforeLdt.minusDays(2);

            Page<BreakageTechDto> breakagesPage = breakageRepository.getAllDeadlineExpiredBreakagesByText(
                    defaultStatusList,
                    defaultPriorityList,
                    defaultPageRequest,
                    localDateTime,
                    BREAKAGE_TEST_SEARCH_TEXT
            );

            List<BreakageTechDto> list = breakagesPage.getContent();

            assertThat(list).isEmpty();
        }

        @Test
        void whenGetBreakageByEmployeeThenReturnBreakageEmployeeDto() {

            Optional<BreakageEmployeeDto> optional =
                    breakageRepository.getBreakageEmployee(savedByEmployeeBreakage.getId());

            assertThat(optional).isNotEmpty();
        }

        @Test
        void whenGetBreakageByEmployeeWhichNotExistThenReturnEmptyOptional() {

            Optional<BreakageEmployeeDto> optional =
                    breakageRepository.getBreakageEmployee(SOME_NOT_EXIST_ID);

            assertThat(optional).isEmpty();
        }

        @Test
        void whenGetBreakageThenReturnBreakageDto() {

            Optional<BreakageDto> optional =
                    breakageRepository.getBreakage(savedByAdminBreakage.getId());

            assertThat(optional).isNotEmpty();
        }

        @Test
        void whenGetBreakageWhichNotExistThenReturnEmptyOptional() {

            Optional<BreakageDto> optional =
                    breakageRepository.getBreakage(SOME_NOT_EXIST_ID);

            assertThat(optional).isEmpty();
        }
    }
}