package ru.kraser.technical_helper.main_server.repository;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.kraser.technical_helper.common_module.model.Department;
import ru.kraser.technical_helper.common_module.model.User;

import java.time.LocalDateTime;
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


//
//    @Test
//    void updateDepartment() {
//    }
//
//    @Test
//    void getAllDepartments() {
//    }
//
//    @Test
//    void getDepartmentById() {
//    }
//
//    @Test
//    void deleteDepartment() {
//    }
}