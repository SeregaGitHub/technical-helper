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
import static ru.kraser.technical_helper.common_module.util.ConstantForTests.*;

@Testcontainers
@SpringBootTest
class DepartmentRepositoryTest {

    @Autowired
    DepartmentRepository departmentRepository;

    @Autowired
    UserRepository userRepository;

    private Department defaultAdminDepartment;
    private Department notEnabledDepartment;
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

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class WhenDepartmentRepositoryMethodsAreInvoked {

        @BeforeAll
        void beforeAll() {

            defaultAdminDepartment = departmentRepository.findById(DEFAULT_ADMIN_DEPARTMENT_ID).get();
            defaultAdminUser = userRepository.findById(DEFAULT_ADMIN_USER_ID).get();

            now = LocalDateTime.of(
                    2025,
                    9,
                    29,
                    13,
                    0,
                    0);

            notEnabledDepartment = departmentRepository.save(
                    Department.builder()
                            .name(DEPARTMENT_TEST_NAME)
                            .enabled(false)
                            .createdBy(defaultAdminUser.getId())
                            .createdDate(now)
                            .lastUpdatedBy(defaultAdminUser.getId())
                            .lastUpdatedDate(now)
                            .build());
        }

        @Nested
        class WhenDepartmentCreating {

            @Test
            @Transactional
            void whenCreateDepartmentThenReturnDepartment() {

                Department toSaveDepartment = Department.builder()
                        .name(DEPARTMENT_TEST_OTHER_NAME)
                        .enabled(true)
                        .createdBy(defaultAdminUser.getId())
                        .createdDate(now)
                        .lastUpdatedBy(defaultAdminUser.getId())
                        .lastUpdatedDate(now)
                        .build();

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

                String existDepartmentName = defaultAdminDepartment.getName();

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
            @Transactional
            void whenDepartmentFindByNameThenReturnDepartmentEvenWhenEnabledIsFalse() {

                Optional<Department> departmentWithEnabledIsFalse =
                        departmentRepository.findByName(notEnabledDepartment.getName());

                assertThat(departmentWithEnabledIsFalse).isNotEmpty();
            }

            @Test
            void whenDepartmentFindByNameThenReturnEmptyOptionalIfNameNotExist() {

                Optional<Department> department = departmentRepository.findByName(SOME_NOT_EXIST_TEXT);

                assertThat(department).isEmpty();
            }
        }

        @Nested
        @TestInstance(TestInstance.Lifecycle.PER_CLASS)
        class WhenDepartmentUpdating {

            @Test
            @Transactional
            @Modifying(clearAutomatically = true)
            void whenUpdateDepartmentThenReturnOne() {

                int response = departmentRepository.updateDepartment(
                        defaultAdminDepartment.getId(),
                        DEPARTMENT_TEST_NEW_NAME,
                        defaultAdminUser.getId(),
                        now);

                assertThat(response).isEqualTo(1);
            }

            @Test
            @Transactional
            @Modifying(clearAutomatically = true)
            void whenUpdateDepartmentWhichNotExistThenReturnZero() {

                int response = departmentRepository.updateDepartment(
                        SOME_NOT_EXIST_ID,
                        DEPARTMENT_TEST_NEW_NAME,
                        defaultAdminUser.getId(),
                        now);

                assertThat(response).isEqualTo(0);
            }


            @Test
            @Transactional
            @Modifying(clearAutomatically = true)
            void whenUpdateDepartmentWithNotUniqueNameThenThrowException() {

                String existDepartmentName = notEnabledDepartment.getName();

                assertThrows(
                        DataIntegrityViolationException.class,
                        () -> departmentRepository.updateDepartment(
                                defaultAdminDepartment.getId(),
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
            @Transactional
            void whenGetAllDepartmentsAndSomeDepartmentEnabledIsFalseThenReturnListOfDepartments() {

                List<DepartmentDto> enabledDepartments = departmentRepository.getAllDepartments();
                List<Department> allDepartments = departmentRepository.findAll();

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
                        departmentRepository.getDepartmentById(SOME_NOT_EXIST_ID);

                assertThat(optional).isEmpty();
            }

            @Test
            @Transactional
            void whenGetDepartmentWhichNotEnabledThenReturnEmptyOptional() {

                Optional<DepartmentDto> optional =
                        departmentRepository.getDepartmentById(notEnabledDepartment.getId());

                assertThat(optional).isEmpty();
            }
        }

        @Nested
        class WhenDepartmentDeleting {

            @Test
            @Transactional
            @Modifying(clearAutomatically = true)
            void whenDeleteDepartmentThenReturnOne() {

                int response = departmentRepository.deleteDepartment(
                        defaultAdminDepartment.getId(), defaultAdminUser.getId(), now
                );

                assertThat(response).isEqualTo(1);
            }

            @Test
            @Transactional
            @Modifying(clearAutomatically = true)
            void whenDeleteDepartmentThenReturnZero() {

                int response = departmentRepository.deleteDepartment(
                        SOME_NOT_EXIST_ID, defaultAdminUser.getId(), now
                );

                assertThat(response).isEqualTo(0);
            }
        }
    }
}