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
import ru.kraser.technical_helper.common_module.dto.user.UserDto;
import ru.kraser.technical_helper.common_module.dto.user.UserFullDto;
import ru.kraser.technical_helper.common_module.dto.user.UserShortDto;
import ru.kraser.technical_helper.common_module.enums.Role;
import ru.kraser.technical_helper.common_module.model.Department;
import ru.kraser.technical_helper.common_module.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
                    .username(USER_TEST_NAME)
                    .password(USER_TEST_PASSWORD)
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
                    .password(USER_TEST_PASSWORD)
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
                    .id(SOME_NOT_EXIST_ID)
                    .name(DEPARTMENT_TEST_NAME)
                    .enabled(true)
                    .createdBy(defaultAdminUser.getId())
                    .createdDate(now)
                    .lastUpdatedBy(defaultAdminUser.getId())
                    .lastUpdatedDate(now)
                    .build();

            User toSaveUserWithNotExistDepartment = User.builder()
                    .username(USER_TEST_NAME)
                    .password(USER_TEST_PASSWORD)
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

    @Nested
    class WhenUserUpdating {

        private Department savedDepartment;
        private User savedUser;

        @BeforeEach
        void setUp() {

            savedDepartment = departmentRepository.saveAndFlush(
                    Department.builder()
                            .name(DEPARTMENT_TEST_NAME)
                            .enabled(true)
                            .createdBy(defaultAdminUser.getId())
                            .createdDate(now)
                            .lastUpdatedBy(defaultAdminUser.getId())
                            .lastUpdatedDate(now)
                            .build()
            );

            savedUser = userRepository.saveAndFlush(
                    User.builder()
                            .username(USER_TEST_NAME)
                            .password(USER_TEST_PASSWORD)
                            .enabled(true)
                            .role(Role.ADMIN)
                            .department(defaultAdminDepartment)
                            .createdBy(defaultAdminUser.getId())
                            .createdDate(now)
                            .lastUpdatedBy(defaultAdminUser.getId())
                            .lastUpdatedDate(now)
                            .build()
            );
        }

        @AfterEach
        @SneakyThrows
        void tearDown() {
            userRepository.deleteById(savedUser.getId());
            departmentRepository.deleteById(savedDepartment.getId());
        }

        @Test
        @Transactional()
        @Modifying(clearAutomatically = true)
        void whenUpdateUserThenReturnOne() {

            int response = userRepository.updateUser(
                    savedUser.getId(),
                    "new_username",
                    savedDepartment.getId(),
                    Role.EMPLOYEE,
                    defaultAdminUser.getId(),
                    now
            );

            assertThat(response).isEqualTo(1);
        }

        @Test
        @Transactional
        @Modifying(clearAutomatically = true)
        void whenUpdateUserWhichNotExistThenReturnZero() {

            int response = userRepository.updateUser(
                    SOME_NOT_EXIST_ID,
                    "new_username",
                    savedDepartment.getId(),
                    Role.EMPLOYEE,
                    defaultAdminUser.getId(),
                    now
            );

            assertThat(response).isEqualTo(0);
        }

        @Test
        @Transactional
        @Modifying(clearAutomatically = true)
        void whenUpdateUserWithNotUniqueNameThenThrowException() {

            String existUsername = defaultAdminUser.getUsername();

            assertThrows(
                    DataIntegrityViolationException.class,
                    () -> userRepository.updateUser(
                            savedUser.getId(),
                            existUsername,
                            savedDepartment.getId(),
                            Role.EMPLOYEE,
                            defaultAdminUser.getId(),
                            now
                    )
            );
        }

        @Test
        @Transactional
        @Modifying(clearAutomatically = true)
        void whenUpdateUserWithNotExistDepartmentThenThrowException() {

            assertThrows(
                    DataIntegrityViolationException.class,
                    () -> userRepository.updateUser(
                            savedUser.getId(),
                            "new_username",
                            SOME_NOT_EXIST_ID,
                            Role.EMPLOYEE,
                            defaultAdminUser.getId(),
                            now
                    )
            );
        }
    }

    @Nested
    class WhenUserChangingPassword {

        private User savedUser;

        @BeforeEach
        void setUp() {

            savedUser = userRepository.saveAndFlush(
                    User.builder()
                            .username(USER_TEST_NAME)
                            .password(USER_TEST_PASSWORD)
                            .enabled(true)
                            .role(Role.ADMIN)
                            .department(defaultAdminDepartment)
                            .createdBy(defaultAdminUser.getId())
                            .createdDate(now)
                            .lastUpdatedBy(defaultAdminUser.getId())
                            .lastUpdatedDate(now)
                            .build()
            );
        }

        @AfterEach
        @SneakyThrows
        void tearDown() {
            userRepository.deleteById(savedUser.getId());
        }

        @Test
        @Transactional()
        @Modifying(clearAutomatically = true)
        void whenChangeUserPasswordThenReturnOne() {

            int response = userRepository.changeUserPassword(
                    savedUser.getId(),
                    "new_user_password",
                    defaultAdminUser.getId(),
                    now
            );

            assertThat(response).isEqualTo(1);
        }

        @Test
        @Transactional
        @Modifying(clearAutomatically = true)
        void whenChangeUserPasswordWhichNotExistThenReturnZero() {

            int response = userRepository.changeUserPassword(
                    SOME_NOT_EXIST_ID,
                    "new_user_password",
                    defaultAdminUser.getId(),
                    now
            );

            assertThat(response).isEqualTo(0);
        }
    }

    @Nested
    class WhenGetAllUsers {

        @Test
        void whenGetAllUsersThenReturnListOfUsers() {

            List<UserDto> userDtoList = userRepository.getAllUsers();

            assertThat(userDtoList.size()).isEqualTo(1);
        }

        @Test
        void whenGetAllUsersAndSomeUserEnabledIsFalseThenReturnListOfUsers() {

            User notEnabledUser = userRepository.saveAndFlush(
                    User.builder()
                            .username(USER_TEST_NAME)
                            .password(USER_TEST_PASSWORD)
                            .enabled(false)
                            .role(Role.ADMIN)
                            .department(defaultAdminDepartment)
                            .createdBy(defaultAdminUser.getId())
                            .createdDate(now)
                            .lastUpdatedBy(defaultAdminUser.getId())
                            .lastUpdatedDate(now)
                            .build()
            );

            List<UserDto> enabledUsers = userRepository.getAllUsers();
            List<User> allUsers = userRepository.findAll();

            userRepository.deleteById(notEnabledUser.getId());

            assertThat(enabledUsers.size()).isEqualTo(1);
            assertThat(allUsers.size()).isEqualTo(2);
        }
    }

    @Nested
    class WhenGetAllAdminAndTechnician {

        private User technicianUser;
        private User employeeUser;

        @BeforeEach
        void setUp() {

            technicianUser = userRepository.saveAndFlush(
                    User.builder()
                            .username("technician_user")
                            .password(USER_TEST_PASSWORD)
                            .enabled(true)
                            .role(Role.TECHNICIAN)
                            .department(defaultAdminDepartment)
                            .createdBy(defaultAdminUser.getId())
                            .createdDate(now)
                            .lastUpdatedBy(defaultAdminUser.getId())
                            .lastUpdatedDate(now)
                            .build()
            );

            employeeUser = userRepository.saveAndFlush(
                    User.builder()
                            .username("employee_user")
                            .password(USER_TEST_PASSWORD)
                            .enabled(true)
                            .role(Role.EMPLOYEE)
                            .department(defaultAdminDepartment)
                            .createdBy(defaultAdminUser.getId())
                            .createdDate(now)
                            .lastUpdatedBy(defaultAdminUser.getId())
                            .lastUpdatedDate(now)
                            .build()
            );
        }

        @AfterEach
        @SneakyThrows
        void tearDown() {
            userRepository.deleteById(technicianUser.getId());
            userRepository.deleteById(employeeUser.getId());
        }

        @Test
        void whenGetAllAdminAndTechnicianThenReturnList() {

            List<UserShortDto> userDtoList = userRepository.getAdminAndTechnicianList();

            assertThat(userDtoList.size()).isEqualTo(2);
        }

        @Test
        void whenGetAllAdminAndTechnicianThenReturnOnlyEnabledList() {

            User notEnabledUser = userRepository.findById(technicianUser.getId()).get();
            notEnabledUser.setEnabled(false);
            userRepository.saveAndFlush(notEnabledUser);

            List<UserShortDto> userDtoList = userRepository.getAdminAndTechnicianList();

            assertThat(userDtoList.size()).isEqualTo(1);
        }
    }

    @Nested
    class WhenGetUser {

        @Test
        void whenGetUserThenReturnUserDto() {

            Optional<UserDto> optional =
                    userRepository.getUserById(defaultAdminUser.getId());

            assertThat(optional).isNotEmpty();
        }

        @Test
        void whenGetUserWhichNotExistThenReturnEmptyOptional() {

            Optional<UserDto> optional =
                    userRepository.getUserById(SOME_NOT_EXIST_ID);

            assertThat(optional).isEmpty();
        }

        @Test
        void whenGetUserWhichNotEnabledThenReturnEmptyOptional() {

            User notEnabledUser = userRepository.saveAndFlush(
                    User.builder()
                            .username(USER_TEST_NAME)
                            .password(USER_TEST_PASSWORD)
                            .enabled(false)
                            .role(Role.ADMIN)
                            .department(defaultAdminDepartment)
                            .createdBy(defaultAdminUser.getId())
                            .createdDate(now)
                            .lastUpdatedBy(defaultAdminUser.getId())
                            .lastUpdatedDate(now)
                            .build()
            );

            Optional<UserDto> optional =
                    userRepository.getUserById(notEnabledUser.getId());

            userRepository.deleteById(notEnabledUser.getId());

            assertThat(optional).isEmpty();
        }
    }

    @Nested
    class WhenFindUserByUsername {

        @Test
        void whenFindUserByUsernameThenReturnUser() {

            Optional<User> optional =
                    userRepository.findUserByUsername(defaultAdminUser.getUsername());

            assertThat(optional).isNotEmpty();
        }

        @Test
        void whenFindUserByUsernameWhichNotEnabledThenReturnUser() {

            User notEnabledUser = userRepository.saveAndFlush(
                    User.builder()
                            .username(USER_TEST_NAME)
                            .password(USER_TEST_PASSWORD)
                            .enabled(false)
                            .role(Role.ADMIN)
                            .department(defaultAdminDepartment)
                            .createdBy(defaultAdminUser.getId())
                            .createdDate(now)
                            .lastUpdatedBy(defaultAdminUser.getId())
                            .lastUpdatedDate(now)
                            .build()
            );

            Optional<User> optional =
                    userRepository.findUserByUsername(notEnabledUser.getUsername());

            userRepository.deleteById(notEnabledUser.getId());

            assertThat(optional).isNotEmpty();
        }
    }

    @Nested
    class WhenGetUserByUsernameAndEnabledTrue {

        @Test
        void whenGetUserByUsernameAndEnabledTrueThenReturnUser() {

            Optional<UserFullDto> optional =
                    userRepository.getUserByUsernameAndEnabledTrue(defaultAdminUser.getUsername());

            assertThat(optional).isNotEmpty();
        }

        @Test
        void whenGetUserByUsernameAndEnabledTrueWhichNotEnabledThenReturnEmptyOption() {

            User notEnabledUser = userRepository.saveAndFlush(
                    User.builder()
                            .username(USER_TEST_NAME)
                            .password(USER_TEST_PASSWORD)
                            .enabled(false)
                            .role(Role.ADMIN)
                            .department(defaultAdminDepartment)
                            .createdBy(defaultAdminUser.getId())
                            .createdDate(now)
                            .lastUpdatedBy(defaultAdminUser.getId())
                            .lastUpdatedDate(now)
                            .build()
            );

            Optional<UserFullDto> optional =
                    userRepository.getUserByUsernameAndEnabledTrue(notEnabledUser.getUsername());

            userRepository.deleteById(notEnabledUser.getId());

            assertThat(optional).isEmpty();
        }
    }

    @Nested
    class WhenFindTop1ByRoleAndEnabledTrue {

        @Test
        void whenFindTop1ByRoleAndEnabledTrueThenReturnUser() {

            Optional<User> optional =
                    userRepository.findTop1ByRoleAndEnabledTrue(Role.ADMIN);

            assertThat(optional).isNotEmpty();
        }

        @Test
        void whenFindTop1ByRoleAndEnabledTrueIfNoOneAdminThenReturnEmptyOptional() {

            User notEnabledAdmin = userRepository.findById(defaultAdminUser.getId()).get();
            notEnabledAdmin.setEnabled(false);
            userRepository.saveAndFlush(notEnabledAdmin);

            Optional<User> optional =
                    userRepository.findTop1ByRoleAndEnabledTrue(Role.ADMIN);

            User enabledAdmin = userRepository.findById(defaultAdminUser.getId()).get();
            enabledAdmin.setEnabled(true);
            userRepository.saveAndFlush(enabledAdmin);

            assertThat(optional).isEmpty();
        }
    }

    @Nested
    class WhenUserDeleting {

        @Test
        @Transactional
        @Modifying(clearAutomatically = true)
        void whenDeleteUserThenReturnOne() {

            User toSaveUser = User.builder()
                    .username(USER_TEST_NAME)
                    .password(USER_TEST_PASSWORD)
                    .enabled(true)
                    .role(Role.ADMIN)
                    .department(defaultAdminDepartment)
                    .createdBy(defaultAdminUser.getId())
                    .createdDate(now)
                    .lastUpdatedBy(defaultAdminUser.getId())
                    .lastUpdatedDate(now)
                    .build();

            User savedUser = userRepository.saveAndFlush(toSaveUser);

            int response = userRepository.deleteUser(
                    savedUser.getId(),
                    defaultAdminUser.getId(),
                    now
            );

            userRepository.deleteById(savedUser.getId());

            assertThat(response).isEqualTo(1);
        }

        @Test
        @Transactional
        @Modifying(clearAutomatically = true)
        void whenDeleteUserThenReturnZero() {

            int response = userRepository.deleteUser(
                    SOME_NOT_EXIST_ID,
                    defaultAdminUser.getId(),
                    now
            );

            assertThat(response).isEqualTo(0);
        }
    }
}