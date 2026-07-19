package ru.kraser.technical_helper.main_server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.kraser.technical_helper.common_module.dto.api.ApiResponse;
import ru.kraser.technical_helper.common_module.dto.user.CreateUserDto;
import ru.kraser.technical_helper.common_module.dto.user.UpdateUserDto;
import ru.kraser.technical_helper.common_module.enums.Role;
import ru.kraser.technical_helper.common_module.exception.AlreadyExistsException;
import ru.kraser.technical_helper.common_module.exception.NotFoundException;
import ru.kraser.technical_helper.common_module.model.Department;
import ru.kraser.technical_helper.common_module.model.User;
import ru.kraser.technical_helper.main_server.repository.DepartmentRepository;
import ru.kraser.technical_helper.main_server.repository.UserRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.kraser.technical_helper.common_module.util.Constant.*;
import static ru.kraser.technical_helper.common_module.util.Constant.CURRENT_USER_ID_HEADER;
import static ru.kraser.technical_helper.main_server.util.Constant.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerIntegrationTest {

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    UserRepository userRepository;
    @MockitoBean
    private Clock clock;

    private static final ZonedDateTime NOW_ZDT = ZonedDateTime.of(
            2025,
            9,
            29,
            13,
            0,
            0,
            0,
            ZoneId.of("UTC")
    );

    private User user;
    private DateTimeFormatter dtf;
    private LocalDateTime now;
    private Department defaultAdminDepartment;
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

        dtf = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");

        when(clock.getZone()).thenReturn(NOW_ZDT.getZone());
        when(clock.instant()).thenReturn(NOW_ZDT.toInstant());

        user = User.builder()
                .username(USER_TEST_NAME)
                .enabled(true)
                .password(USER_TEST_PASSWORD)
                .role(Role.ADMIN)
                .department(defaultAdminDepartment)
                .createdBy(defaultAdminUser.getId())
                .createdDate(now)
                .lastUpdatedBy(defaultAdminUser.getId())
                .lastUpdatedDate(now)
                .build();
    }

    @Nested
    class WhenUserCreating {

        private CreateUserDto createUserDto;

        @BeforeEach
        void setUp() {

            createUserDto = CreateUserDto.builder()
                    .username(user.getUsername())
                    .password(USER_TEST_PASSWORD)
                    .departmentId(DEFAULT_ADMIN_DEPARTMENT_ID)
                    .role(Role.ADMIN)
                    .build();
        }

        @Test
        @SneakyThrows
        void whenCreateUserThenReturnCreated() {

            String responseMessage = "Сотрудник: " + createUserDto.username() + ", - был успешно создан.";

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(responseMessage)
                    .status(201)
                    .httpStatus(HttpStatus.CREATED)
                    .timestamp(now)
                    .build();

            String result = mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + ADMIN_URL + USER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(CURRENT_USER_ID_HEADER, DEFAULT_ADMIN_USER_ID)
                            .content(objectMapper.writeValueAsString(createUserDto)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(201))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value("CREATED"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(dtf.format(now)))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            Example<User> example = Example.of(user);
            User savedUser = userRepository.findOne(example).get();
            userRepository.deleteById(savedUser.getId());

            assertThat(savedUser.getId()).isNotNull();
            assertThat(savedUser.getUsername()).isEqualTo(user.getUsername());
            assertThat(savedUser.isEnabled()).isEqualTo(user.isEnabled());
            assertThat(savedUser.getPassword()).isNotNull();
            assertThat(savedUser.getRole()).isEqualTo(user.getRole());
            assertThat(savedUser.getDepartment()).isEqualTo(user.getDepartment());
            assertThat(savedUser.getCreatedBy()).isEqualTo(user.getCreatedBy());
            assertThat(savedUser.getCreatedDate()).isEqualTo(user.getCreatedDate());
            assertThat(savedUser.getLastUpdatedBy()).isEqualTo(user.getLastUpdatedBy());
            assertThat(savedUser.getLastUpdatedDate()).isEqualTo(user.getLastUpdatedDate());

            ApiResponse actualApiResponse = objectMapper.readValue(result, ApiResponse.class);
            assertThat(actualApiResponse).isEqualTo(apiResponse);
        }

        @SneakyThrows
        @Test
        void whenCreateUserThenReturnAlreadyExistsException() {

            String responseMessage = "Сотрудник: " + defaultAdminUser.getUsername() + ", - уже существует." +
                    " Используйте другое имя !!!";

            CreateUserDto createUserDtoWithNotUniqueName = CreateUserDto.builder()
                    .username(defaultAdminUser.getUsername())
                    .password(USER_TEST_PASSWORD)
                    .departmentId(DEFAULT_ADMIN_DEPARTMENT_ID)
                    .role(Role.ADMIN)
                    .build();

            String result = mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + ADMIN_URL + USER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(CURRENT_USER_ID_HEADER, DEFAULT_ADMIN_USER_ID)
                            .content(objectMapper.writeValueAsString(createUserDtoWithNotUniqueName)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            AlreadyExistsException exception = objectMapper.readValue(result, AlreadyExistsException.class);
            assertThat(exception.getMessage()).isEqualTo(responseMessage);
        }

        @SneakyThrows
        @Test
        void whenCreateUserThenReturnNotFoundException() {

            String responseMessage = "Отдел в котором находится сотрудник не существует !!!";

            CreateUserDto createUserDtoWithNotExistDepartmentId = CreateUserDto.builder()
                    .username(user.getUsername())
                    .password(USER_TEST_PASSWORD)
                    .departmentId(SOME_NOT_EXIST_ID)
                    .role(Role.ADMIN)
                    .build();

            String result = mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + ADMIN_URL + USER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(CURRENT_USER_ID_HEADER, DEFAULT_ADMIN_USER_ID)
                            .content(objectMapper.writeValueAsString(createUserDtoWithNotExistDepartmentId)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            AlreadyExistsException exception = objectMapper.readValue(result, AlreadyExistsException.class);
            assertThat(exception.getMessage()).isEqualTo(responseMessage);
        }
    }

    @Nested
    class WhenUserUpdating {

        private UpdateUserDto updateUserDto;
        private Department department;
        private final String newUserName = "new_user_name";

        @SneakyThrows
        @Test
        void whenUpdateUserThenReturnOk() {

            department = Department.builder()
                    .name(DEPARTMENT_TEST_NAME)
                    .enabled(true)
                    .createdBy(defaultAdminUser.getId())
                    .createdDate(now)
                    .lastUpdatedBy(defaultAdminUser.getId())
                    .lastUpdatedDate(now)
                    .build();

            User savedUser = userRepository.saveAndFlush(user);
            Department savedDepartment = departmentRepository.saveAndFlush(department);

            updateUserDto = UpdateUserDto.builder()
                    .username(newUserName)
                    .departmentId(savedDepartment.getId())
                    .role(Role.TECHNICIAN)
                    .build();

            String responseMessage = "Сотрудник: " + updateUserDto.username() + " - был успешно изменен.";

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(responseMessage)
                    .status(200)
                    .httpStatus(HttpStatus.OK)
                    .timestamp(now)
                    .build();

            String result = mockMvc.perform(MockMvcRequestBuilders.patch(BASE_URL + ADMIN_URL + USER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(CURRENT_USER_ID_HEADER, defaultAdminUser.getId())
                            .header(USER_ID_HEADER, savedUser.getId())
                            .content(objectMapper.writeValueAsString(updateUserDto)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(200))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value("OK"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(dtf.format(now)))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            entityManager.clear();
            User updatedUser = userRepository.findById(savedUser.getId()).get();
            userRepository.deleteById(savedUser.getId());
            departmentRepository.deleteById(savedDepartment.getId());

            assertThat(result).isEqualTo(objectMapper.writeValueAsString(apiResponse));
            assertThat(updatedUser.getUsername()).isEqualTo(updateUserDto.username());
            assertThat(updatedUser.getDepartment().getId()).isEqualTo(updateUserDto.departmentId());
            assertThat(updatedUser.getRole()).isEqualTo(updateUserDto.role());
        }

        @SneakyThrows
        @Test
        void whenUpdateUserThenReturnAlreadyExistsException() {

            User savedUser = userRepository.saveAndFlush(user);

            String responseMessage = "Сотрудник: " + defaultAdminUser.getUsername() + ", - уже существует." +
                    " Используйте другое имя !!!";

            UpdateUserDto updateUserDtoWithNotUniqueName = UpdateUserDto.builder()
                    .username(defaultAdminUser.getUsername())
                    .departmentId(defaultAdminDepartment.getId())
                    .role(Role.ADMIN)
                    .build();

            String result = mockMvc.perform(MockMvcRequestBuilders.patch(BASE_URL + ADMIN_URL + USER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(CURRENT_USER_ID_HEADER, defaultAdminUser.getId())
                            .header(USER_ID_HEADER, savedUser.getId())
                            .content(objectMapper.writeValueAsString(updateUserDtoWithNotUniqueName)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                    .andExpect(status().isUnprocessableEntity())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            userRepository.deleteById(savedUser.getId());

            AlreadyExistsException exception = objectMapper.readValue(result, AlreadyExistsException.class);
            assertThat(exception.getMessage()).isEqualTo(responseMessage);
        }

        @SneakyThrows
        @Test
        void whenUpdateUserIfSuchDepartmentNotExistThenReturnNotFoundException() {

            User savedUser = userRepository.saveAndFlush(user);

            String responseMessage = "Отдел в котором находится сотрудник не существует !!!";

            UpdateUserDto updateUserDtoWithNotExistDepartmentId = UpdateUserDto.builder()
                    .username(newUserName)
                    .departmentId(SOME_NOT_EXIST_ID)
                    .role(Role.ADMIN)
                    .build();

            String result = mockMvc.perform(MockMvcRequestBuilders.patch(BASE_URL + ADMIN_URL + USER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(CURRENT_USER_ID_HEADER, defaultAdminUser.getId())
                            .header(USER_ID_HEADER, savedUser.getId())
                            .content(objectMapper.writeValueAsString(updateUserDtoWithNotExistDepartmentId)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                    .andExpect(status().isNotFound())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            userRepository.deleteById(savedUser.getId());

            NotFoundException exception = objectMapper.readValue(result, NotFoundException.class);
            assertThat(exception.getMessage()).isEqualTo(responseMessage);
        }

        @SneakyThrows
        @Test
        void whenUpdateUserIfSuchUserNotExistThenReturnNotFoundException() {

            String responseMessage = "Данный пользователь не существует !!!";

            UpdateUserDto updateUserDtoWithNotExistUserId = UpdateUserDto.builder()
                    .username(newUserName)
                    .departmentId(defaultAdminDepartment.getId())
                    .role(Role.ADMIN)
                    .build();

            String result = mockMvc.perform(MockMvcRequestBuilders.patch(BASE_URL + ADMIN_URL + USER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(CURRENT_USER_ID_HEADER, defaultAdminUser.getId())
                            .header(USER_ID_HEADER, SOME_NOT_EXIST_ID)
                            .content(objectMapper.writeValueAsString(updateUserDtoWithNotExistUserId)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                    .andExpect(status().isNotFound())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            NotFoundException exception = objectMapper.readValue(result, NotFoundException.class);
            assertThat(exception.getMessage()).isEqualTo(responseMessage);
        }
    }

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
//    void getUser() {
//    }
//
//    @Test
//    void getUserByName() {
//    }
//
//    @Test
//    void deleteUser() {
//    }
}