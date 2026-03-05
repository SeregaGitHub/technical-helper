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
import ru.kraser.technical_helper.common_module.enums.Role;
import ru.kraser.technical_helper.common_module.model.Department;
import ru.kraser.technical_helper.common_module.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static ru.kraser.technical_helper.main_server.util.Constant.*;

@Testcontainers
@SpringBootTest
class UserRepositoryTest {

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

        defaultAdminDepartment = departmentRepository.findById(DEPARTMENT_ID).get();
        defaultAdminUser = userRepository.findById(USER_ID).get();

        now = LocalDateTime.of(
                2025,
                9,
                29,
                13,
                0,
                0);
    }

    @Nested
    class WhenUserCreating {

        @Test
        void whenCreateUserThenReturnUser() {

            User toSaveUser = User.builder()
                    .username("some_username")
                    .password("some_password")
                    .enabled(true)
                    .role(Role.ADMIN)
                    .department(defaultAdminDepartment)
                    .createdBy(defaultAdminUser.getId())
                    .createdDate(now)
                    .lastUpdatedBy(defaultAdminUser.getId())
                    .lastUpdatedDate(now)
                    .build();

            User savedUser = userRepository.saveAndFlush(toSaveUser);

            userRepository.deleteById(savedUser.getId());

            assertThat(savedUser.getId()).isNotNull();
            assertThat(savedUser.getUsername()).isEqualTo(toSaveUser.getUsername());
            assertThat(savedUser.getPassword()).isNotNull();
            assertThat(savedUser.isEnabled()).isTrue();
            assertThat(savedUser.getRole()).isEqualTo(toSaveUser.getRole());
            assertThat(savedUser.getDepartment().getId()).isEqualTo(toSaveUser.getDepartment().getId());
            assertThat(savedUser.getCreatedBy()).isEqualTo(toSaveUser.getCreatedBy());
            assertThat(savedUser.getCreatedDate()).isEqualTo(toSaveUser.getCreatedDate());
            assertThat(savedUser.getLastUpdatedBy()).isEqualTo(toSaveUser.getLastUpdatedBy());
            assertThat(savedUser.getLastUpdatedDate()).isEqualTo(toSaveUser.getLastUpdatedDate());
        }

        @Test
        void whenCreateUserWithNotUniqueNameThenThrowException() {

            String existUserName = defaultAdminUser.getUsername();

            User toSaveUserWithNotUniqueName = User.builder()
                    .username(existUserName)
                    .password("some_password")
                    .enabled(true)
                    .role(Role.ADMIN)
                    .department(defaultAdminDepartment)
                    .createdBy(defaultAdminUser.getId())
                    .createdDate(now)
                    .lastUpdatedBy(defaultAdminUser.getId())
                    .lastUpdatedDate(now)
                    .build();

            assertThrows(
                    DataIntegrityViolationException.class,
                    () -> userRepository.saveAndFlush(toSaveUserWithNotUniqueName)
            );
        }

        @Test
        void whenCreateUserWithNotExistDepartmentThenThrowException() {

            Department notExistDepartment = Department.builder()
                    .id("some_not_exist_department_id")
                    .name(DEPARTMENT_TEST_NAME)
                    .enabled(true)
                    .createdBy(defaultAdminUser.getId())
                    .createdDate(now)
                    .lastUpdatedBy(defaultAdminUser.getId())
                    .lastUpdatedDate(now)
                    .build();

            User toSaveUserWithNotExistDepartment = User.builder()
                    .username("some_username")
                    .password("some_password")
                    .enabled(true)
                    .role(Role.ADMIN)
                    .department(notExistDepartment)
                    .createdBy(defaultAdminUser.getId())
                    .createdDate(now)
                    .lastUpdatedBy(defaultAdminUser.getId())
                    .lastUpdatedDate(now)
                    .build();

            assertThrows(
                    DataIntegrityViolationException.class,
                    () -> userRepository.saveAndFlush(toSaveUserWithNotExistDepartment)
            );
        }
    }

//    @Test
//    void updateUser() {
//    }
//
//    @Test
//    void changeUserPassword() {
//    }
//
//    @Test
//    void getAllUsers() {
//    }
//
//    @Test
//    void getAdminAndTechnicianList() {
//    }
//
//    @Test
//    void getUserById() {
//    }
//
//    @Test
//    void findUserByUsername() {
//    }
//
//    @Test
//    void getUserByUsernameAndEnabledTrue() {
//    }
//
//    @Test
//    void findTop1ByRoleAndEnabledTrue() {
//    }
//
//    @Test
//    void deleteUser() {
//    }
//
//    @Test
//    void dropConstraints() {
//    }
//
//    @Test
//    void setCurrentId() {
//    }
}