package ru.kraser.technical_helper.main_server.repository;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.kraser.technical_helper.common_module.exception.AlreadyExistsException;
import ru.kraser.technical_helper.common_module.model.Department;
import ru.kraser.technical_helper.common_module.model.User;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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

    /*@Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:17-alpine");*/

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

        Connection connection = postgreSQLContainer.createConnection("");
        Statement statement = connection.createStatement();

        // creating default admin department and default admin user
        statement.executeUpdate(
                """
                        ALTER TABLE IF EXISTS users
                            DROP CONSTRAINT IF EXISTS fk_user_created_by;
                        ALTER TABLE IF EXISTS users
                            DROP CONSTRAINT IF EXISTS fk_user_last_updated_by;
                        ALTER TABLE IF EXISTS department
                            DROP CONSTRAINT IF EXISTS fk_department_created_by;
                        ALTER TABLE IF EXISTS department
                            DROP CONSTRAINT IF EXISTS fk_department_last_updated_by;
                        """
        );
        statement.executeUpdate(
                """
                        INSERT INTO department
                        VALUES
                        ('d1d11111-11d1-1d11-1111-d111111d1111', 'test_admin_department', true,
                        'u1u11111-11u1-1u11-1111-u111111u1111', '2025-09-29 13:00:00',
                        'u1u11111-11u1-1u11-1111-u111111u1111', '2025-09-29 13:00:00');
                        """);
        statement.executeUpdate(
                """
                        INSERT INTO users
                        VALUES
                        ('u1u11111-11u1-1u11-1111-u111111u1111', 'test_admin', 'some_password', true,
                        'd1d11111-11d1-1d11-1111-d111111d1111', 'ADMIN',
                        'u1u11111-11u1-1u11-1111-u111111u1111', '2025-09-29 13:00:00',
                        'u1u11111-11u1-1u11-1111-u111111u1111', '2025-09-29 13:00:00');
                        """);
        statement.executeUpdate(
                """
                        ALTER TABLE department
                            ADD CONSTRAINT fk_department_created_by FOREIGN KEY (created_by)
                            	REFERENCES users (id);
                        ALTER TABLE department
                            ADD CONSTRAINT fk_department_last_updated_by FOREIGN KEY (last_updated_by)
                            	REFERENCES users (id);
                        ALTER TABLE users
                            ADD CONSTRAINT fk_user_created_by FOREIGN KEY (created_by)
                            	REFERENCES users (id);
                        ALTER TABLE users
                            ADD CONSTRAINT fk_user_last_updated_by FOREIGN KEY (last_updated_by)
                            	REFERENCES users (id);
                        """
        );

        statement.close();
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



//    @Test
//    void findByName() {
//    }
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