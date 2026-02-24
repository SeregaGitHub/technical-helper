package ru.kraser.technical_helper.main_server.repository;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.kraser.technical_helper.common_module.dto.department.DepartmentDto;
import ru.kraser.technical_helper.common_module.model.Department;
import ru.kraser.technical_helper.common_module.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers
@SpringBootTest
class DepartmentRepositoryTest {

    @Autowired
    DepartmentRepository departmentRepository;

    @Autowired
    UserRepository userRepository;

    private Department defaultAdminDepartment;
    private LocalDateTime now;
    private User defaultAdminUser;

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

        defaultAdminDepartment = departmentRepository.findByName("test_admin_department").get();
        defaultAdminUser = userRepository.findUserByUsername("test_admin").get();

        now = LocalDateTime.of(
                2025,
                9,
                29,
                13,
                0,
                0);
    }

    @Nested
    class WhenDepartmentCreating {

        private Department toSaveDepartment;

        @BeforeEach
        void setUp() {

            toSaveDepartment = Department.builder()
                    .name("some_name")
                    .enabled(true)
                    .createdBy(defaultAdminUser.getId())
                    .createdDate(now)
                    .lastUpdatedBy(defaultAdminUser.getId())
                    .lastUpdatedDate(now)
                    .build();
        }

        @AfterEach
        @SneakyThrows
        void tearDown() {
            departmentRepository.deleteById(toSaveDepartment.getId());
        }

        @Test
        void whenCreateDepartmentThenReturnDepartment() {

            Department savedDepartment = departmentRepository.saveAndFlush(toSaveDepartment);

            assertThat(savedDepartment.getId()).isNotNull();
            assertThat(savedDepartment.getName()).isEqualTo(toSaveDepartment.getName());
            assertThat(savedDepartment.isEnabled()).isEqualTo(toSaveDepartment.isEnabled());
            assertThat(savedDepartment.getCreatedBy()).isEqualTo(toSaveDepartment.getCreatedBy());
            assertThat(savedDepartment.getCreatedDate()).isEqualTo(toSaveDepartment.getCreatedDate());
            assertThat(savedDepartment.getLastUpdatedBy()).isEqualTo(toSaveDepartment.getLastUpdatedBy());
            assertThat(savedDepartment.getLastUpdatedDate()).isEqualTo(toSaveDepartment.getLastUpdatedDate());
        }

        @Test
        void whenCreateDepartmentWithNotUniqueNameThenThrowException() {
            departmentRepository.saveAndFlush(toSaveDepartment);
            String existDepartmentName = toSaveDepartment.getName();

            Department toSaveDepartmentWithNotUniqueName = Department.builder()
                    .name(existDepartmentName)
                    .enabled(true)
                    .createdBy(defaultAdminUser.getId())
                    .createdDate(now)
                    .lastUpdatedBy(defaultAdminUser.getId())
                    .lastUpdatedDate(now)
                    .build();

            assertThrows(
                    DataIntegrityViolationException.class,
                    () -> departmentRepository.saveAndFlush(toSaveDepartmentWithNotUniqueName)
            );
        }
    }

    @Nested
    class WhenDepartmentFindByName {

        @Test
        void whenDepartmentFindByNameThenReturnDepartment() {

            Optional<Department> department = departmentRepository.findByName(defaultAdminDepartment.getName());

            assertThat(department).isNotEmpty();
        }

        @Test
        void whenDepartmentFindByNameThenReturnDepartmentEvenWhenEnabledIsFalse() {

            Department department = Department.builder()
                    .name("some_name")
                    .enabled(false)
                    .createdBy(defaultAdminUser.getId())
                    .createdDate(now)
                    .lastUpdatedBy(defaultAdminUser.getId())
                    .lastUpdatedDate(now)
                    .build();

            departmentRepository.saveAndFlush(department);

            Optional<Department> departmentWithEnabledIsFalse =
                    departmentRepository.findByName(department.getName());

            departmentRepository.deleteById(department.getId());

            assertThat(departmentWithEnabledIsFalse).isNotEmpty();
        }

        @Test
        void whenDepartmentFindByNameThenReturnEmptyOptionalIfNameNotExist() {

            Optional<Department> department = departmentRepository.findByName("some_non-existent_name");

            assertThat(department).isEmpty();
        }
    }

    @Nested
    class WhenDepartmentUpdating {

        private Department toSaveDepartment;

        @BeforeEach
        void setUp() {

            toSaveDepartment = Department.builder()
                    .name("some_name")
                    .enabled(true)
                    .createdBy(defaultAdminUser.getId())
                    .createdDate(now)
                    .lastUpdatedBy(defaultAdminUser.getId())
                    .lastUpdatedDate(now)
                    .build();

            departmentRepository.saveAndFlush(toSaveDepartment);
        }

        @AfterEach
        @SneakyThrows
        void tearDown() {
            departmentRepository.deleteById(toSaveDepartment.getId());
        }

        @Test
        @Transactional()
        @Modifying(clearAutomatically = true)
        void whenUpdateDepartmentThenReturnOne() {

            int response = departmentRepository.updateDepartment(
                    toSaveDepartment.getId(),
                    "updated_department_name",
                    defaultAdminUser.getId(),
                    now);

            assertThat(response).isEqualTo(1);
        }

        @Test
        @Transactional
        @Modifying(clearAutomatically = true)
        void whenUpdateDepartmentWhichNotExistThenReturnZero() {

            int response = departmentRepository.updateDepartment(
                    "some_non-existent_id",
                    "updated_department_name",
                    defaultAdminUser.getId(),
                    now);

            assertThat(response).isEqualTo(0);
        }


        @Test
        @Transactional
        @Modifying(clearAutomatically = true)
        void whenUpdateDepartmentWithNotUniqueNameThenThrowException() {

            String existDepartmentName = defaultAdminDepartment.getName();

            assertThrows(
                    DataIntegrityViolationException.class,
                    () -> departmentRepository.updateDepartment(
                            toSaveDepartment.getId(),
                            existDepartmentName,
                            defaultAdminUser.getId(),
                            now)
            );
        }
    }

    @Nested
    class WhenGetAllDepartments {

        @Test
        void whenGetAllDepartmentsThenReturnListOfDepartments() {

            List<DepartmentDto> departmentDtoList = departmentRepository.getAllDepartments();

            assertThat(departmentDtoList.size()).isEqualTo(1);
        }

        @Test
        void whenGetAllDepartmentsAndSomeDepartmentEnabledIsFalseThenReturnListOfDepartments() {

            Department notEnabledDepartment = Department.builder()
                    .name("some_not_enabled_department")
                    .enabled(false)
                    .createdBy(defaultAdminUser.getId())
                    .createdDate(now)
                    .lastUpdatedBy(defaultAdminUser.getId())
                    .lastUpdatedDate(now)
                    .build();

            departmentRepository.saveAndFlush(notEnabledDepartment);

            List<DepartmentDto> enabledDepartments = departmentRepository.getAllDepartments();
            List<Department> allDepartments = departmentRepository.findAll();

            departmentRepository.deleteById(notEnabledDepartment.getId());

            assertThat(enabledDepartments.size()).isEqualTo(1);
            assertThat(allDepartments.size()).isEqualTo(2);
        }
    }

    @Nested
    class WhenGetDepartment {

        @Test
        void whenGetDepartmentThenReturnDepartmentDto() {

            Optional<DepartmentDto> optional =
                    departmentRepository.getDepartmentById(defaultAdminDepartment.getId());

            assertThat(optional).isNotEmpty();
        }

        @Test
        void whenGetDepartmentWhichNotExistThenReturnEmptyOptional() {

            Optional<DepartmentDto> optional =
                    departmentRepository.getDepartmentById("some_non-existent_id");

            assertThat(optional).isEmpty();
        }

        @Test
        void whenGetDepartmentWhichNotEnabledThenReturnEmptyOptional() {

            Department notEnabledDepartment = Department.builder()
                    .name("some_not_enabled_department")
                    .enabled(false)
                    .createdBy(defaultAdminUser.getId())
                    .createdDate(now)
                    .lastUpdatedBy(defaultAdminUser.getId())
                    .lastUpdatedDate(now)
                    .build();

            Department savedDepartment = departmentRepository.saveAndFlush(notEnabledDepartment);

            Optional<DepartmentDto> optional =
                    departmentRepository.getDepartmentById(savedDepartment.getId());

            departmentRepository.deleteById(savedDepartment.getId());

            assertThat(optional).isEmpty();
        }
    }



//
//    @Test
//    void deleteDepartment() {
//    }
}