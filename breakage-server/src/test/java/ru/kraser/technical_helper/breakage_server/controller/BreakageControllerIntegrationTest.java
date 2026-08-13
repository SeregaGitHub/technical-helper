package ru.kraser.technical_helper.breakage_server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.kraser.technical_helper.BreakageServer;
import ru.kraser.technical_helper.breakage_server.repository.BreakageCommentRepository;
import ru.kraser.technical_helper.breakage_server.repository.BreakageRepository;
import ru.kraser.technical_helper.common_module.dto.api.ApiResponse;
import ru.kraser.technical_helper.common_module.dto.breakage.*;
import ru.kraser.technical_helper.common_module.dto.breakage_comment.BreakageCommentFrontDto;
import ru.kraser.technical_helper.common_module.dto.breakage_comment.CreateBreakageCommentDto;
import ru.kraser.technical_helper.common_module.dto.user.CreateUserDto;
import ru.kraser.technical_helper.common_module.enums.Priority;
import ru.kraser.technical_helper.common_module.enums.Role;
import ru.kraser.technical_helper.common_module.enums.Status;
import ru.kraser.technical_helper.common_module.exception.ForbiddenException;
import ru.kraser.technical_helper.common_module.exception.NotFoundException;
import ru.kraser.technical_helper.common_module.model.Breakage;
import ru.kraser.technical_helper.common_module.model.BreakageComment;
import ru.kraser.technical_helper.common_module.model.Department;
import ru.kraser.technical_helper.common_module.model.User;
import ru.kraser.technical_helper.main_server.repository.DepartmentRepository;
import ru.kraser.technical_helper.main_server.repository.UserRepository;
import ru.kraser.technical_helper.main_server.util.mapper.UserMapper;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.kraser.technical_helper.common_module.util.Constant.*;
import static ru.kraser.technical_helper.common_module.util.Constant.CURRENT_USER_ID_HEADER;
import static ru.kraser.technical_helper.common_module.util.ConstantForTests.*;
import static ru.kraser.technical_helper.common_module.util.ConstantForTests.DEFAULT_ADMIN_USER_ID;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = BreakageServer.class)
class BreakageControllerIntegrationTest {

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private BreakageRepository breakageRepository;
    @Autowired
    private BreakageCommentRepository breakageCommentRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    DepartmentRepository departmentRepository;
    @MockitoBean
    private Clock breakageClock;

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

    private DateTimeFormatter dtf;
    private LocalDateTime now;
    private LocalDateTime afterNow;

    private Department defaultAdminDepartment;
    private Department employeeCurrentDepartment;
    private Department employeeOtherDepartment;

    private User defaultAdminUser;
    private User technicianUser;
    private User employeeCurrentUser;
    private User employeeOtherUser;

    private Breakage employeeCurrentBreakage;
    private BreakageComment breakageComment;

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

        when(breakageClock.getZone()).thenReturn(NOW_ZDT.getZone());
        when(breakageClock.instant()).thenReturn(NOW_ZDT.toInstant());
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class WhenBreakageRepositoryMethodsAreInvoked {

        @BeforeAll
        void insertData() {

            now = LocalDateTime.of(
                    2025,
                    9,
                    29,
                    13,
                    0,
                    0);

            dtf = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");

            defaultAdminDepartment = departmentRepository.findById(DEFAULT_ADMIN_DEPARTMENT_ID).get();
            defaultAdminUser = userRepository.findById(DEFAULT_ADMIN_USER_ID).get();

            Department toSaveEmployeeCurrentDepartment = Department.builder()
                    .name(DEPARTMENT_TEST_NAME)
                    .enabled(true)
                    .createdBy(defaultAdminUser.getId())
                    .createdDate(now)
                    .lastUpdatedBy(defaultAdminUser.getId())
                    .lastUpdatedDate(now)
                    .build();

            Department toSaveEmployeeOtherDepartment = Department.builder()
                    .name(DEPARTMENT_TEST_OTHER_NAME)
                    .enabled(true)
                    .createdBy(defaultAdminUser.getId())
                    .createdDate(now)
                    .lastUpdatedBy(defaultAdminUser.getId())
                    .lastUpdatedDate(now)
                    .build();

            employeeCurrentDepartment = departmentRepository.saveAndFlush(toSaveEmployeeCurrentDepartment);
            employeeOtherDepartment = departmentRepository.saveAndFlush(toSaveEmployeeOtherDepartment);

            User toSaveEmployeeUser = User.builder()
                    .username(USER_TEST_NAME)
                    .password(USER_TEST_PASSWORD)
                    .enabled(true)
                    .role(Role.EMPLOYEE)
                    .department(employeeCurrentDepartment)
                    .createdBy(defaultAdminUser.getId())
                    .createdDate(now)
                    .lastUpdatedBy(defaultAdminUser.getId())
                    .lastUpdatedDate(now)
                    .build();

            User toSaveEmployeeOtherUser = User.builder()
                    .username(USER_TEST_OTHER_NAME)
                    .password(USER_TEST_PASSWORD)
                    .enabled(true)
                    .role(Role.EMPLOYEE)
                    .department(employeeOtherDepartment)
                    .createdBy(defaultAdminUser.getId())
                    .createdDate(now)
                    .lastUpdatedBy(defaultAdminUser.getId())
                    .lastUpdatedDate(now)
                    .build();

            User toSaveTechnicianUser = User.builder()
                    .username(USER_TECHNICIAN_TEST_NAME)
                    .password(USER_TEST_PASSWORD)
                    .enabled(true)
                    .role(Role.TECHNICIAN)
                    .department(defaultAdminDepartment)
                    .createdBy(defaultAdminUser.getId())
                    .createdDate(now)
                    .lastUpdatedBy(defaultAdminUser.getId())
                    .lastUpdatedDate(now)
                    .build();

            employeeCurrentUser = userRepository.saveAndFlush(toSaveEmployeeUser);
            employeeOtherUser = userRepository.saveAndFlush(toSaveEmployeeOtherUser);
            technicianUser = userRepository.saveAndFlush(toSaveTechnicianUser);

            Breakage toSaveEmployeeBreakage = Breakage.builder()
                    .department(employeeCurrentDepartment)
                    .room(BREAKAGE_TEST_ROOM)
                    .breakageTopic(BREAKAGE_TEST_TOPIC)
                    .breakageText(BREAKAGE_TEST_TEXT)
                    .status(Status.NEW)
                    .priority(Priority.MEDIUM)
                    .executor(null)
                    .executorAppointedBy(null)
                    .deadline(null)
                    .createdBy(employeeCurrentUser.getId())
                    .createdDate(now)
                    .lastUpdatedBy(employeeCurrentUser.getId())
                    .lastUpdatedDate(now)
                    .build();

            employeeCurrentBreakage = breakageRepository.saveAndFlush(toSaveEmployeeBreakage);

            BreakageComment toSaveBreakageComment = BreakageComment.builder()
                    .breakage(employeeCurrentBreakage)
                    .comment(BREAKAGE_COMMENT_TEST_TEXT)
                    .createdBy(defaultAdminUser.getId())
                    .createdDate(now)
                    .lastUpdatedBy(defaultAdminUser.getId())
                    .lastUpdatedDate(now)
                    .build();

            breakageComment = breakageCommentRepository.saveAndFlush(toSaveBreakageComment);
        }

        @AfterAll
        void cleanupData() {

            transactionTemplate.execute(status -> {
                entityManager.createNativeQuery("TRUNCATE TABLE breakage_comment CASCADE")
                        .executeUpdate();
                return null;
            });

            transactionTemplate.execute(status -> {
                entityManager.createNativeQuery("TRUNCATE TABLE breakage CASCADE")
                        .executeUpdate();
                return null;
            });

            userRepository.deleteById(employeeCurrentUser.getId());
            userRepository.deleteById(employeeOtherUser.getId());
            userRepository.deleteById(technicianUser.getId());
            departmentRepository.deleteById(employeeCurrentDepartment.getId());
        }

        @Nested
        @Transactional
        class WhenBreakageCreating {

            private CreateBreakageFullDto createBreakageFullDto;

            @Test
            @SneakyThrows
            void whenCreateBreakageThenReturnCreated() {

                Breakage testBreakage = Breakage.builder()
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

                createBreakageFullDto = CreateBreakageFullDto.builder()
                        .department(testBreakage.getDepartment())
                        .room(testBreakage.getRoom())
                        .breakageTopic(testBreakage.getBreakageTopic())
                        .breakageText(testBreakage.getBreakageText())
                        .build();

                String responseMessage = "Заявка о неисправности с темой: " +
                        createBreakageFullDto.breakageTopic() +
                        ", - была успешно создана.";

                ApiResponse apiResponse = ApiResponse.builder()
                        .message(responseMessage)
                        .status(201)
                        .httpStatus(HttpStatus.CREATED)
                        .timestamp(now)
                        .build();

                String result = mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + BREAKAGE_URL +
                                        EMPLOYEE_URL
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(CURRENT_USER_ID_HEADER, defaultAdminUser.getId())
                                .content(objectMapper.writeValueAsString(createBreakageFullDto)))
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(201))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value("CREATED"))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(dtf.format(now)))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                Example<Breakage> example = Example.of(testBreakage);
                Breakage savedBreakage = breakageRepository.findOne(example).get();

                assertThat(savedBreakage.getId()).isNotNull();
                assertThat(savedBreakage.getDepartment()).isEqualTo(testBreakage.getDepartment());
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

                ApiResponse actualApiResponse = objectMapper.readValue(result, ApiResponse.class);

                assertThat(actualApiResponse.message()).isEqualTo(apiResponse.message());
                assertThat(actualApiResponse.status()).isEqualTo(apiResponse.status());
                assertThat(actualApiResponse.httpStatus()).isEqualTo(apiResponse.httpStatus());
                assertThat(actualApiResponse.timestamp()).isEqualTo(apiResponse.timestamp());
                assertThat(actualApiResponse.data()).isEqualTo(apiResponse.data());
            }

            @Test
            @SneakyThrows
            void whenCreateBreakageThenReturnNotFoundException() {

                String responseMessage = "Отдел, за которым числится неисправность не существует !!!";

                Department notExistDepartment = Department.builder()
                        .id(DEPARTMENT_TEST_ID)
                        .name(DEPARTMENT_TEST_NAME)
                        .enabled(true)
                        .createdBy(DEFAULT_ADMIN_USER_ID)
                        .createdDate(now)
                        .lastUpdatedBy(DEFAULT_ADMIN_USER_ID)
                        .lastUpdatedDate(now)
                        .build();

                createBreakageFullDto = CreateBreakageFullDto.builder()
                        .department(notExistDepartment)
                        .room(BREAKAGE_TEST_ROOM)
                        .breakageTopic(BREAKAGE_TEST_TOPIC)
                        .breakageText(BREAKAGE_TEST_TEXT)
                        .build();

                ApiResponse apiResponse = ApiResponse.builder()
                        .message(responseMessage)
                        .status(404)
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .timestamp(now)
                        .build();

                String result = mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + BREAKAGE_URL +
                                        EMPLOYEE_URL
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(CURRENT_USER_ID_HEADER, USER_TEST_ID)
                                .content(objectMapper.writeValueAsString(createBreakageFullDto)))
                        .andExpect(status().isNotFound())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                ApiResponse actualApiResponse = objectMapper.readValue(result, ApiResponse.class);

                assertThat(actualApiResponse.message()).isEqualTo(apiResponse.message());
            }
        }

        @Nested
        @Transactional
        class WhenBreakageCancelling {

            @Test
            @SneakyThrows
            void whenCancelBreakageByTechnicianThenReturnOk() {

                String responseMessage = "Заявка на неисправность была успешно отменена.";

                ApiResponse apiResponse = ApiResponse.builder()
                        .message(responseMessage)
                        .status(200)
                        .httpStatus(HttpStatus.OK)
                        .timestamp(now)
                        .data(technicianUser.getUsername())
                        .build();

                String result = mockMvc.perform(MockMvcRequestBuilders.patch(
                                        BASE_URL + BREAKAGE_URL + EMPLOYEE_URL + "/" +
                                                technicianUser.getUsername()
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(CURRENT_USER_ID_HEADER, technicianUser.getId())
                                .header(BREAKAGE_ID_HEADER, employeeCurrentBreakage.getId())
                                .header(DEPARTMENT_ID_HEADER, employeeCurrentBreakage.getDepartment().getId())
                                .header(USER_ROLE_HEADER, Role.TECHNICIAN)
                                .header(USER_DEPARTMENT_ID_HEADER, technicianUser.getDepartment().getId()))
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(200))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value(HttpStatus.OK.name()))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(dtf.format(now)))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                ApiResponse actualApiResponse = objectMapper.readValue(result, ApiResponse.class);

                assertThat(actualApiResponse.message()).isEqualTo(apiResponse.message());
                assertThat(actualApiResponse.status()).isEqualTo(apiResponse.status());
                assertThat(actualApiResponse.httpStatus()).isEqualTo(apiResponse.httpStatus());
                assertThat(actualApiResponse.timestamp()).isEqualTo(apiResponse.timestamp());
                assertThat(actualApiResponse.data()).isEqualTo(apiResponse.data());

                Breakage cancelledBreakage = breakageRepository.findById(employeeCurrentBreakage.getId()).get();

                assertThat(cancelledBreakage.getStatus()).isEqualTo(Status.CANCELLED);
                assertThat(cancelledBreakage.getLastUpdatedBy()).isEqualTo(technicianUser.getId());
            }

            @Test
            @SneakyThrows
            void whenCancelBreakageByEmployeeFromSameDepartmentThenReturnOk() {

                when(breakageClock.getZone()).thenReturn(NOW_ZDT.plusHours(1).getZone());
                when(breakageClock.instant()).thenReturn(NOW_ZDT.plusHours(1).toInstant());
                afterNow = now.plusHours(1);

                String responseMessage = "Заявка на неисправность была успешно отменена.";

                ApiResponse apiResponse = ApiResponse.builder()
                        .message(responseMessage)
                        .status(200)
                        .httpStatus(HttpStatus.OK)
                        .timestamp(afterNow)
                        .data(employeeCurrentUser.getUsername())
                        .build();

                String result = mockMvc.perform(MockMvcRequestBuilders.patch(
                                        BASE_URL + BREAKAGE_URL + EMPLOYEE_URL + "/" +
                                                employeeCurrentUser.getUsername()
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(CURRENT_USER_ID_HEADER, employeeCurrentUser.getId())
                                .header(BREAKAGE_ID_HEADER, employeeCurrentBreakage.getId())
                                .header(DEPARTMENT_ID_HEADER, employeeCurrentBreakage.getDepartment().getId())
                                .header(USER_ROLE_HEADER, Role.EMPLOYEE)
                                .header(USER_DEPARTMENT_ID_HEADER, employeeCurrentUser.getDepartment().getId()))
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(200))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value(HttpStatus.OK.name()))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(dtf.format(afterNow)))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                ApiResponse actualApiResponse = objectMapper.readValue(result, ApiResponse.class);

                assertThat(actualApiResponse.message()).isEqualTo(apiResponse.message());
                assertThat(actualApiResponse.status()).isEqualTo(apiResponse.status());
                assertThat(actualApiResponse.httpStatus()).isEqualTo(apiResponse.httpStatus());
                assertThat(actualApiResponse.timestamp()).isEqualTo(apiResponse.timestamp());
                assertThat(actualApiResponse.data()).isEqualTo(apiResponse.data());

                Breakage cancelledBreakage = breakageRepository.findById(employeeCurrentBreakage.getId()).get();

                assertThat(cancelledBreakage.getStatus()).isEqualTo(Status.CANCELLED);
                assertThat(cancelledBreakage.getLastUpdatedBy()).isEqualTo(employeeCurrentUser.getId());
            }

            @Test
            @SneakyThrows
            void whenCancelBreakageWhichNotExistThenReturnNotFoundException() {

                ApiResponse apiResponse = ApiResponse.builder()
                        .message(BREAKAGE_NOT_EXIST)
                        .status(404)
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .timestamp(now)
                        .data(defaultAdminUser.getUsername())
                        .build();

                String result = mockMvc.perform(MockMvcRequestBuilders.patch(
                                        BASE_URL + BREAKAGE_URL + EMPLOYEE_URL + "/" +
                                                defaultAdminUser.getUsername()
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(CURRENT_USER_ID_HEADER, defaultAdminUser.getUsername())
                                .header(BREAKAGE_ID_HEADER, SOME_NOT_EXIST_ID)
                                .header(DEPARTMENT_ID_HEADER, employeeCurrentDepartment.getId())
                                .header(USER_ROLE_HEADER, Role.ADMIN)
                                .header(USER_DEPARTMENT_ID_HEADER, defaultAdminUser.getDepartment().getId()))
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(BREAKAGE_NOT_EXIST))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                ApiResponse actualApiResponse = objectMapper.readValue(result, ApiResponse.class);

                assertThat(actualApiResponse.message()).isEqualTo(apiResponse.message());
            }

            @Test
            @SneakyThrows
            void whenCancelBreakageByEmployeeFromOtherDepartmentThenReturnForbiddenException() {

                String responseMessage = "Только технический специалист или сотрудник отдела, " +
                        "в котором произошла неисправность, могут отменить заявку !!!";

                ApiResponse apiResponse = ApiResponse.builder()
                        .message(responseMessage)
                        .status(422)
                        .httpStatus(HttpStatus.FORBIDDEN)
                        .timestamp(now)
                        .data(employeeOtherUser.getUsername())
                        .build();

                String result = mockMvc.perform(MockMvcRequestBuilders.patch(
                                        BASE_URL + BREAKAGE_URL + EMPLOYEE_URL + "/" +
                                                employeeOtherUser.getUsername()
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(CURRENT_USER_ID_HEADER, employeeOtherUser.getId())
                                .header(BREAKAGE_ID_HEADER, employeeCurrentBreakage.getId())
                                .header(DEPARTMENT_ID_HEADER, employeeCurrentBreakage.getDepartment().getId())
                                .header(USER_ROLE_HEADER, Role.EMPLOYEE)
                                .header(USER_DEPARTMENT_ID_HEADER, employeeOtherUser.getDepartment().getId()))
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                ApiResponse actualApiResponse = objectMapper.readValue(result, ApiResponse.class);

                assertThat(actualApiResponse.message()).isEqualTo(apiResponse.message());
            }
        }

        @Nested
        @Transactional
        class WhenBreakageStatusUpdating {

            private UpdateBreakageStatusDto updateBreakageStatusDto;

            @Test
            @SneakyThrows
            void whenUpdateBreakageStatusThenReturnOk() {

                updateBreakageStatusDto = new UpdateBreakageStatusDto(Status.IN_PROGRESS);

                String responseMessage = "Статус заявки на неисправность был успешно изменен";

                ApiResponse apiResponse = ApiResponse.builder()
                        .message(responseMessage)
                        .status(200)
                        .httpStatus(HttpStatus.OK)
                        .timestamp(now)
                        .data(defaultAdminUser.getUsername())
                        .build();

                String result = mockMvc.perform(MockMvcRequestBuilders.patch(
                                        BASE_URL + BREAKAGE_URL +
                                                TECHNICIAN_URL + STATUS_URL + "/" + defaultAdminUser.getUsername()
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(CURRENT_USER_ID_HEADER, defaultAdminUser.getId())
                                .header(BREAKAGE_ID_HEADER, employeeCurrentBreakage.getId())
                                .content(objectMapper.writeValueAsString(updateBreakageStatusDto)))
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(200))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value(HttpStatus.OK.name()))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(dtf.format(now)))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                ApiResponse actualApiResponse = objectMapper.readValue(result, ApiResponse.class);

                assertThat(actualApiResponse.message()).isEqualTo(apiResponse.message());
                assertThat(actualApiResponse.status()).isEqualTo(apiResponse.status());
                assertThat(actualApiResponse.httpStatus()).isEqualTo(apiResponse.httpStatus());
                assertThat(actualApiResponse.timestamp()).isEqualTo(apiResponse.timestamp());
                assertThat(actualApiResponse.data()).isEqualTo(apiResponse.data());

                Breakage updatedBreakage = breakageRepository.findById(employeeCurrentBreakage.getId()).get();

                assertThat(updatedBreakage.getStatus()).isEqualTo(Status.IN_PROGRESS);
                assertThat(updatedBreakage.getLastUpdatedBy()).isEqualTo(defaultAdminUser.getId());
            }

            @Test
            @SneakyThrows
            void whenUpdateBreakageStatusThenResetExecutorAndReturnOk() {

                when(breakageClock.getZone()).thenReturn(NOW_ZDT.plusHours(1).getZone());
                when(breakageClock.instant()).thenReturn(NOW_ZDT.plusHours(1).toInstant());
                afterNow = now.plusHours(1);

                Breakage breakage = breakageRepository.findById(employeeCurrentBreakage.getId()).get();
                breakage.setExecutor(technicianUser);
                breakage.setExecutorAppointedBy(defaultAdminUser);
                breakage.setDeadline(now);
                breakage.setLastUpdatedBy(defaultAdminUser.getId());
                breakage.setLastUpdatedDate(afterNow);
                breakageRepository.saveAndFlush(breakage);

                updateBreakageStatusDto = new UpdateBreakageStatusDto(Status.PAUSED);

                String responseMessage = "Статус заявки на неисправность был успешно изменен";

                ApiResponse apiResponse = ApiResponse.builder()
                        .message(responseMessage)
                        .status(200)
                        .httpStatus(HttpStatus.OK)
                        .timestamp(afterNow)
                        .data(technicianUser.getUsername())
                        .build();

                String result = mockMvc.perform(MockMvcRequestBuilders.patch(
                                        BASE_URL + BREAKAGE_URL +
                                                TECHNICIAN_URL + STATUS_URL + "/" + technicianUser.getUsername()
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(CURRENT_USER_ID_HEADER, technicianUser.getId())
                                .header(BREAKAGE_ID_HEADER, employeeCurrentBreakage.getId())
                                .content(objectMapper.writeValueAsString(updateBreakageStatusDto)))
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(200))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value(HttpStatus.OK.name()))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(dtf.format(afterNow)))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                ApiResponse actualApiResponse = objectMapper.readValue(result, ApiResponse.class);

                assertThat(actualApiResponse.message()).isEqualTo(apiResponse.message());
                assertThat(actualApiResponse.status()).isEqualTo(apiResponse.status());
                assertThat(actualApiResponse.httpStatus()).isEqualTo(apiResponse.httpStatus());
                assertThat(actualApiResponse.timestamp()).isEqualTo(apiResponse.timestamp());
                assertThat(actualApiResponse.data()).isEqualTo(apiResponse.data());

                entityManager.clear();
                Breakage updatedBreakage = breakageRepository.findById(employeeCurrentBreakage.getId()).get();

                assertThat(updatedBreakage.getStatus()).isEqualTo(Status.PAUSED);
                assertThat(updatedBreakage.getExecutor()).isNull();
                assertThat(updatedBreakage.getExecutorAppointedBy()).isNull();
                assertThat(updatedBreakage.getDeadline()).isNull();
                assertThat(updatedBreakage.getLastUpdatedBy()).isEqualTo(technicianUser.getId());
            }

            @Test
            @SneakyThrows
            void whenUpdateBreakageWhichNotExistThenReturnNotFoundException() {

                updateBreakageStatusDto = new UpdateBreakageStatusDto(Status.IN_PROGRESS);

                ApiResponse apiResponse = ApiResponse.builder()
                        .message(BREAKAGE_NOT_EXIST)
                        .status(404)
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .timestamp(now)
                        .data(defaultAdminUser.getUsername())
                        .build();

                String result = mockMvc.perform(MockMvcRequestBuilders.patch(
                                        BASE_URL + BREAKAGE_URL +
                                                TECHNICIAN_URL + STATUS_URL + "/" + defaultAdminUser.getUsername()
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(CURRENT_USER_ID_HEADER, defaultAdminUser.getId())
                                .header(BREAKAGE_ID_HEADER, SOME_NOT_EXIST_ID)
                                .content(objectMapper.writeValueAsString(updateBreakageStatusDto)))
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(BREAKAGE_NOT_EXIST))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                ApiResponse actualApiResponse = objectMapper.readValue(result, ApiResponse.class);

                assertThat(actualApiResponse.message()).isEqualTo(apiResponse.message());
            }

            @Test
            @SneakyThrows
            void whenUpdateBreakageStatusIfStatusIsNewThenReturnNotCorrectParameter() {

                updateBreakageStatusDto = new UpdateBreakageStatusDto(Status.NEW);

                String responseMessage = "Заявка на неисправность не может изменить статус на - \"Новая\" !!!";

                ApiResponse apiResponse = ApiResponse.builder()
                        .message(responseMessage)
                        .status(404)
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .timestamp(now)
                        .data(defaultAdminUser.getUsername())
                        .build();

                String result = mockMvc.perform(MockMvcRequestBuilders.patch(
                                        BASE_URL + BREAKAGE_URL +
                                                TECHNICIAN_URL + STATUS_URL + "/" + defaultAdminUser.getUsername()
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(CURRENT_USER_ID_HEADER, defaultAdminUser.getId())
                                .header(BREAKAGE_ID_HEADER, SOME_NOT_EXIST_ID)
                                .content(objectMapper.writeValueAsString(updateBreakageStatusDto)))
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                ApiResponse actualApiResponse = objectMapper.readValue(result, ApiResponse.class);

                assertThat(actualApiResponse.message()).isEqualTo(apiResponse.message());
            }
        }

        @Nested
        @Transactional
        class WhenBreakagePriorityUpdating {

            private UpdateBreakagePriorityDto updateBreakagePriorityDto;

            @Test
            @SneakyThrows
            void whenUpdateBreakagePriorityThenReturnOk() {

                updateBreakagePriorityDto = new UpdateBreakagePriorityDto(Priority.HIGH, Status.NEW);

                String responseMessage = "Приоритет заявки на неисправность был успешно изменен";

                ApiResponse apiResponse = ApiResponse.builder()
                        .message(responseMessage)
                        .status(200)
                        .httpStatus(HttpStatus.OK)
                        .timestamp(now)
                        .data(defaultAdminUser.getUsername())
                        .build();

                String result = mockMvc.perform(MockMvcRequestBuilders.patch(
                                        BASE_URL + BREAKAGE_URL +
                                                ADMIN_URL + PRIORITY_URL + "/" + defaultAdminUser.getUsername()
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(CURRENT_USER_ID_HEADER, defaultAdminUser.getId())
                                .header(BREAKAGE_ID_HEADER, employeeCurrentBreakage.getId())
                                .content(objectMapper.writeValueAsString(updateBreakagePriorityDto)))
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(200))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value(HttpStatus.OK.name()))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(dtf.format(now)))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                ApiResponse actualApiResponse = objectMapper.readValue(result, ApiResponse.class);

                assertThat(actualApiResponse.message()).isEqualTo(apiResponse.message());
                assertThat(actualApiResponse.status()).isEqualTo(apiResponse.status());
                assertThat(actualApiResponse.httpStatus()).isEqualTo(apiResponse.httpStatus());
                assertThat(actualApiResponse.timestamp()).isEqualTo(apiResponse.timestamp());
                assertThat(actualApiResponse.data()).isEqualTo(apiResponse.data());

                Breakage updatedBreakage = breakageRepository.findById(employeeCurrentBreakage.getId()).get();

                assertThat(updatedBreakage.getPriority()).isEqualTo(Priority.HIGH);
                assertThat(updatedBreakage.getLastUpdatedBy()).isEqualTo(defaultAdminUser.getId());
            }

            @Test
            @SneakyThrows
            void whenUpdateBreakageWhichNotExistThenReturnNotFoundException() {

                updateBreakagePriorityDto = new UpdateBreakagePriorityDto(Priority.HIGH, Status.NEW);

                ApiResponse apiResponse = ApiResponse.builder()
                        .message(BREAKAGE_NOT_EXIST)
                        .status(404)
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .timestamp(now)
                        .data(defaultAdminUser.getUsername())
                        .build();

                String result = mockMvc.perform(MockMvcRequestBuilders.patch(
                                        BASE_URL + BREAKAGE_URL +
                                                ADMIN_URL + PRIORITY_URL + "/" + defaultAdminUser.getUsername()
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(CURRENT_USER_ID_HEADER, defaultAdminUser.getId())
                                .header(BREAKAGE_ID_HEADER, SOME_NOT_EXIST_ID)
                                .content(objectMapper.writeValueAsString(updateBreakagePriorityDto)))
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(BREAKAGE_NOT_EXIST))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                ApiResponse actualApiResponse = objectMapper.readValue(result, ApiResponse.class);

                assertThat(actualApiResponse.message()).isEqualTo(apiResponse.message());
            }

            @Test
            @SneakyThrows
            void whenUpdateBreakagePriorityIfStatusIsSolvedOrCancelledThenReturnNotCorrectParameter() {

                updateBreakagePriorityDto = new UpdateBreakagePriorityDto(Priority.HIGH, Status.SOLVED);

                String responseMessage = "Заявка на неисправность со статусом: \"Решена\" или \"Отменена\"" +
                        " - не может быть изменена !!!";

                ApiResponse apiResponse = ApiResponse.builder()
                        .message(responseMessage)
                        .status(400)
                        .httpStatus(HttpStatus.BAD_REQUEST)
                        .timestamp(now)
                        .data(defaultAdminUser.getUsername())
                        .build();

                String result = mockMvc.perform(MockMvcRequestBuilders.patch(
                                        BASE_URL + BREAKAGE_URL +
                                                ADMIN_URL + PRIORITY_URL + "/" + defaultAdminUser.getUsername()
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(CURRENT_USER_ID_HEADER, defaultAdminUser.getId())
                                .header(BREAKAGE_ID_HEADER, employeeCurrentBreakage.getId())
                                .content(objectMapper.writeValueAsString(updateBreakagePriorityDto)))
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                ApiResponse actualApiResponse = objectMapper.readValue(result, ApiResponse.class);

                assertThat(actualApiResponse.message()).isEqualTo(apiResponse.message());
            }
        }

        @Nested
        @Transactional
        class WhenBreakageExecutorAdding {

            private LocalDateTime afterNowDateTime;
            private LocalDate afterNowDate;
            private AppointBreakageExecutorDto appointBreakageExecutorDto;

            @BeforeEach
            void setUp() {

                afterNowDateTime = now.plusDays(1).plusHours(10).plusMinutes(59).plusSeconds(59);
                afterNowDate = afterNowDateTime.toLocalDate();
            }

            @Test
            @SneakyThrows
            void whenAddBreakageExecutorThenReturnOk() {

                appointBreakageExecutorDto = new AppointBreakageExecutorDto(
                        technicianUser.getId(), afterNowDate, Status.NEW
                );

                String responseMessage = "Исполнитель заявки на неисправность и срок исполнения были успешно назначены.";

                ApiResponse apiResponse = ApiResponse.builder()
                        .message(responseMessage)
                        .status(200)
                        .httpStatus(HttpStatus.OK)
                        .timestamp(now)
                        .data(defaultAdminUser.getUsername())
                        .build();

                String result = mockMvc.perform(MockMvcRequestBuilders.patch(
                                        BASE_URL + BREAKAGE_URL +
                                                ADMIN_URL + EXECUTOR_URL + "/" + defaultAdminUser.getUsername()
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(CURRENT_USER_ID_HEADER, defaultAdminUser.getId())
                                .header(BREAKAGE_ID_HEADER, employeeCurrentBreakage.getId())
                                .content(objectMapper.writeValueAsString(appointBreakageExecutorDto)))
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(200))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value(HttpStatus.OK.name()))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(dtf.format(now)))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                ApiResponse actualApiResponse = objectMapper.readValue(result, ApiResponse.class);

                assertThat(actualApiResponse.message()).isEqualTo(apiResponse.message());
                assertThat(actualApiResponse.status()).isEqualTo(apiResponse.status());
                assertThat(actualApiResponse.httpStatus()).isEqualTo(apiResponse.httpStatus());
                assertThat(actualApiResponse.timestamp()).isEqualTo(apiResponse.timestamp());
                assertThat(actualApiResponse.data()).isEqualTo(apiResponse.data());

                Breakage updatedBreakage = breakageRepository.findById(employeeCurrentBreakage.getId()).get();

                assertThat(updatedBreakage.getExecutor().getId()).isEqualTo(technicianUser.getId());
                assertThat(updatedBreakage.getExecutorAppointedBy().getId()).isEqualTo(defaultAdminUser.getId());
                assertThat(updatedBreakage.getDeadline()).isEqualTo(afterNowDateTime);
                assertThat(updatedBreakage.getLastUpdatedBy()).isEqualTo(defaultAdminUser.getId());
            }

            @Test
            @SneakyThrows
            void whenAddBreakageExecutorIfBreakageNotExistThenReturnNotFoundException() {

                appointBreakageExecutorDto = new AppointBreakageExecutorDto(
                        defaultAdminUser.getId(), afterNowDate, Status.NEW
                );

                ApiResponse apiResponse = ApiResponse.builder()
                        .message(BREAKAGE_NOT_EXIST)
                        .status(404)
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .timestamp(now)
                        .data(defaultAdminUser.getUsername())
                        .build();

                String result = mockMvc.perform(MockMvcRequestBuilders.patch(
                                        BASE_URL + BREAKAGE_URL +
                                                ADMIN_URL + EXECUTOR_URL + "/" + defaultAdminUser.getUsername()
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(CURRENT_USER_ID_HEADER, defaultAdminUser.getId())
                                .header(BREAKAGE_ID_HEADER, SOME_NOT_EXIST_ID)
                                .content(objectMapper.writeValueAsString(appointBreakageExecutorDto)))
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(BREAKAGE_NOT_EXIST))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                ApiResponse actualApiResponse = objectMapper.readValue(result, ApiResponse.class);

                assertThat(actualApiResponse.message()).isEqualTo(apiResponse.message());
            }

            @Test
            @SneakyThrows
            void whenAddBreakageExecutorIfExecutorNotExistThenReturnNotFoundException() {

                appointBreakageExecutorDto = new AppointBreakageExecutorDto(
                        SOME_NOT_EXIST_ID, afterNowDate, Status.NEW
                );

                String responseMessage =
                        "Пользователь, который назначается исполнителем заявки на неисправность, не существует.";

                ApiResponse apiResponse = ApiResponse.builder()
                        .message(responseMessage)
                        .status(404)
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .timestamp(now)
                        .build();

                String result = mockMvc.perform(MockMvcRequestBuilders.patch(
                                        BASE_URL + BREAKAGE_URL +
                                                ADMIN_URL + EXECUTOR_URL + "/" + defaultAdminUser.getUsername()
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(CURRENT_USER_ID_HEADER, defaultAdminUser.getId())
                                .header(BREAKAGE_ID_HEADER, employeeCurrentBreakage.getId())
                                .content(objectMapper.writeValueAsString(appointBreakageExecutorDto)))
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                ApiResponse actualApiResponse = objectMapper.readValue(result, ApiResponse.class);

                assertThat(actualApiResponse.message()).isEqualTo(apiResponse.message());
            }

            @Test
            @SneakyThrows
            void whenAddBreakageExecutorIfDeadlineIsNotCorrectThenReturnNotCorrectParameter() {

                LocalDateTime beforeNow = now.minusDays(1);
                LocalDate beforeNowDate = beforeNow.toLocalDate();

                appointBreakageExecutorDto = new AppointBreakageExecutorDto(
                        DEFAULT_ADMIN_USER_ID, beforeNowDate, Status.NEW
                );

                String responseMessage = "Необходимо указать корректный срок исполнения заявки на неисправность.";

                ApiResponse apiResponse = ApiResponse.builder()
                        .message(responseMessage)
                        .status(400)
                        .httpStatus(HttpStatus.BAD_REQUEST)
                        .timestamp(now)
                        .build();

                String result = mockMvc.perform(MockMvcRequestBuilders.patch(
                                        BASE_URL + BREAKAGE_URL +
                                                ADMIN_URL + EXECUTOR_URL + "/" + defaultAdminUser.getUsername()
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(CURRENT_USER_ID_HEADER, DEFAULT_ADMIN_USER_ID)
                                .header(BREAKAGE_ID_HEADER, employeeCurrentBreakage.getId())
                                .content(objectMapper.writeValueAsString(appointBreakageExecutorDto)))
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                ApiResponse actualApiResponse = objectMapper.readValue(result, ApiResponse.class);

                assertThat(actualApiResponse.message()).isEqualTo(apiResponse.message());
            }

            @Test
            @SneakyThrows
            void whenAddBreakageExecutorIfStatusIsNotCorrectThenReturnNotCorrectParameter() {

                appointBreakageExecutorDto = new AppointBreakageExecutorDto(USER_TEST_ID, afterNowDate, Status.SOLVED);

                String responseMessage = "Заявке на неисправность со статусами: \"В ожидании\", \"Передана\"" +
                        ", \"Решена\" или \"Отменена\" - не может быть назначен исполнитель !!!";

                ApiResponse apiResponse = ApiResponse.builder()
                        .message(responseMessage)
                        .status(400)
                        .httpStatus(HttpStatus.BAD_REQUEST)
                        .timestamp(now)
                        .build();

                String result = mockMvc.perform(MockMvcRequestBuilders.patch(
                                        BASE_URL + BREAKAGE_URL +
                                                ADMIN_URL + EXECUTOR_URL + "/" + defaultAdminUser.getUsername()
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(CURRENT_USER_ID_HEADER, defaultAdminUser.getId())
                                .header(BREAKAGE_ID_HEADER, employeeCurrentBreakage.getId())
                                .content(objectMapper.writeValueAsString(appointBreakageExecutorDto)))
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                ApiResponse actualApiResponse = objectMapper.readValue(result, ApiResponse.class);

                assertThat(actualApiResponse.message()).isEqualTo(apiResponse.message());
            }
        }

        @Nested
        @Transactional
        class WhenBreakageExecutorDropping {

            @Test
            @SneakyThrows
            void whenDropBreakageExecutorThenReturnOk() {

                when(breakageClock.getZone()).thenReturn(NOW_ZDT.plusHours(1).getZone());
                when(breakageClock.instant()).thenReturn(NOW_ZDT.plusHours(1).toInstant());
                afterNow = now.plusHours(1);

                LocalDateTime appointedDeadline = now.plusDays(1).plusHours(10).plusMinutes(59).plusSeconds(59);

                Breakage breakage = breakageRepository.findById(employeeCurrentBreakage.getId()).get();
                breakage.setExecutor(technicianUser);
                breakage.setExecutorAppointedBy(defaultAdminUser);
                breakage.setDeadline(appointedDeadline);
                breakage.setLastUpdatedBy(defaultAdminUser.getId());

                breakageRepository.saveAndFlush(breakage);

                String responseMessage = "Исполнитель заявки на неисправность и срок исполнения были успешно удалены.";

                ApiResponse apiResponse = ApiResponse.builder()
                        .message(responseMessage)
                        .status(200)
                        .httpStatus(HttpStatus.OK)
                        .timestamp(afterNow)
                        .data(defaultAdminUser.getUsername())
                        .build();

                String result = mockMvc.perform(MockMvcRequestBuilders.patch(
                                        BASE_URL + BREAKAGE_URL +
                                                ADMIN_URL + EXECUTOR_URL + DELETE_URL + "/" +
                                                defaultAdminUser.getUsername()
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(CURRENT_USER_ID_HEADER, defaultAdminUser.getId())
                                .header(BREAKAGE_ID_HEADER, employeeCurrentBreakage.getId()))
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(200))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value(HttpStatus.OK.name()))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(dtf.format(afterNow)))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                ApiResponse actualApiResponse = objectMapper.readValue(result, ApiResponse.class);

                assertThat(actualApiResponse.message()).isEqualTo(apiResponse.message());
                assertThat(actualApiResponse.status()).isEqualTo(apiResponse.status());
                assertThat(actualApiResponse.httpStatus()).isEqualTo(apiResponse.httpStatus());
                assertThat(actualApiResponse.timestamp()).isEqualTo(apiResponse.timestamp());
                assertThat(actualApiResponse.data()).isEqualTo(apiResponse.data());

                entityManager.clear();
                Breakage updatedBreakage = breakageRepository.findById(employeeCurrentBreakage.getId()).get();

                assertThat(updatedBreakage.getExecutor()).isNull();
                assertThat(updatedBreakage.getExecutorAppointedBy()).isNull();
                assertThat(updatedBreakage.getDeadline()).isNull();
                assertThat(updatedBreakage.getLastUpdatedBy()).isEqualTo(defaultAdminUser.getId());
            }

            @Test
            @SneakyThrows
            void whenDropBreakageExecutorThenReturnNotFoundException() {

                ApiResponse apiResponse = ApiResponse.builder()
                        .message(BREAKAGE_NOT_EXIST)
                        .status(404)
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .timestamp(now)
                        .build();

                String result = mockMvc.perform(MockMvcRequestBuilders.patch(
                                        BASE_URL + BREAKAGE_URL +
                                                ADMIN_URL + EXECUTOR_URL + DELETE_URL + "/" +
                                                defaultAdminUser.getUsername()
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(CURRENT_USER_ID_HEADER, defaultAdminUser.getId())
                                .header(BREAKAGE_ID_HEADER, SOME_NOT_EXIST_ID))
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(BREAKAGE_NOT_EXIST))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                ApiResponse actualApiResponse = objectMapper.readValue(result, ApiResponse.class);

                assertThat(actualApiResponse.message()).isEqualTo(apiResponse.message());
            }
        }

        //    @Test
        //    void getAllBreakages() {
        //    }

        @Nested
        class WhenBreakageByEmployeeGetting {

            @Test
            @SneakyThrows
            void whenGetBreakageByEmployeeThenReturnBreakage() {

                BreakageEmployeeDto expectedBreakageEmployeeDto = BreakageEmployeeDto.builder()
                        .id(employeeCurrentBreakage.getId())
                        .departmentId(employeeCurrentBreakage.getDepartment().getId())
                        .departmentName(employeeCurrentBreakage.getDepartment().getName())
                        .room(employeeCurrentBreakage.getRoom())
                        .breakageTopic(employeeCurrentBreakage.getBreakageTopic())
                        .breakageText(employeeCurrentBreakage.getBreakageText())
                        .status(employeeCurrentBreakage.getStatus())
                        .breakageExecutor(NO_APPOINTED_EXECUTOR)
                        .createdBy(USER_TEST_NAME)
                        .createdDate(employeeCurrentBreakage.getCreatedDate())
                        .build();

                String result = mockMvc.perform(MockMvcRequestBuilders.get(
                                        BASE_URL + BREAKAGE_URL + EMPLOYEE_URL + CURRENT_URL
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(USER_DEPARTMENT_ID_HEADER, employeeCurrentBreakage.getDepartment().getId())
                                .header(BREAKAGE_ID_HEADER, employeeCurrentBreakage.getId()))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.id")
                                .value(expectedBreakageEmployeeDto.getId()))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.departmentId")
                                .value(expectedBreakageEmployeeDto.getDepartmentId()))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.departmentName")
                                .value(expectedBreakageEmployeeDto.getDepartmentName()))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.room")
                                .value(expectedBreakageEmployeeDto.getRoom()))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.breakageTopic")
                                .value(expectedBreakageEmployeeDto.getBreakageTopic()))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.breakageText")
                                .value(expectedBreakageEmployeeDto.getBreakageText()))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.status")
                                .value(expectedBreakageEmployeeDto.getStatus().name()))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.breakageExecutor")
                                .value(expectedBreakageEmployeeDto.getBreakageExecutor()))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.createdBy")
                                .value(expectedBreakageEmployeeDto.getCreatedBy()))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.createdDate")
                                .value(dtf.format(expectedBreakageEmployeeDto.getCreatedDate())))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                BreakageEmployeeDto actualBreakageEmployeeDto =
                        objectMapper.readValue(result, BreakageEmployeeDto.class);

                assertThat(actualBreakageEmployeeDto.getId())
                        .isEqualTo(expectedBreakageEmployeeDto.getId());
                assertThat(actualBreakageEmployeeDto.getDepartmentId())
                        .isEqualTo(expectedBreakageEmployeeDto.getDepartmentId());
                assertThat(actualBreakageEmployeeDto.getDepartmentName())
                        .isEqualTo(expectedBreakageEmployeeDto.getDepartmentName());
                assertThat(actualBreakageEmployeeDto.getRoom())
                        .isEqualTo(expectedBreakageEmployeeDto.getRoom());
                assertThat(actualBreakageEmployeeDto.getBreakageTopic())
                        .isEqualTo(expectedBreakageEmployeeDto.getBreakageTopic());
                assertThat(actualBreakageEmployeeDto.getBreakageText())
                        .isEqualTo(expectedBreakageEmployeeDto.getBreakageText());
                assertThat(actualBreakageEmployeeDto.getStatus())
                        .isEqualTo(expectedBreakageEmployeeDto.getStatus());
                assertThat(actualBreakageEmployeeDto.getBreakageExecutor())
                        .isEqualTo(expectedBreakageEmployeeDto.getBreakageExecutor());
                assertThat(actualBreakageEmployeeDto.getCreatedBy())
                        .isEqualTo(expectedBreakageEmployeeDto.getCreatedBy());
                assertThat(actualBreakageEmployeeDto.getCreatedDate())
                        .isEqualTo(expectedBreakageEmployeeDto.getCreatedDate());
            }

            @Test
            @SneakyThrows
            void whenGetBreakageByEmployeeThenReturnForbiddenException() {

                String message = "Данный пользователь не имеет право на получение информации по " +
                        "этой заявке на неисправность !!!";

                ApiResponse apiResponse = ApiResponse.builder()
                        .message(message)
                        .status(403)
                        .httpStatus(HttpStatus.FORBIDDEN)
                        .timestamp(now)
                        .build();

                String result = mockMvc.perform(MockMvcRequestBuilders.get(
                                        BASE_URL + BREAKAGE_URL + EMPLOYEE_URL + CURRENT_URL
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(USER_DEPARTMENT_ID_HEADER, employeeOtherDepartment.getId())
                                .header(BREAKAGE_ID_HEADER, employeeCurrentBreakage.getId()))
                        .andExpect(MockMvcResultMatchers.status().isForbidden())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(message))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                ApiResponse actualApiResponse = objectMapper.readValue(result, ApiResponse.class);

                assertThat(actualApiResponse.message()).isEqualTo(apiResponse.message());
            }

            @Test
            @SneakyThrows
            void whenGetBreakageByEmployeeThenReturnNotFoundException() {

                ApiResponse apiResponse = ApiResponse.builder()
                        .message(GET_BREAKAGE_NOT_EXIST)
                        .status(404)
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .timestamp(now)
                        .build();

                String result = mockMvc.perform(MockMvcRequestBuilders.get(
                                        BASE_URL + BREAKAGE_URL + EMPLOYEE_URL + CURRENT_URL
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(USER_DEPARTMENT_ID_HEADER, employeeCurrentBreakage.getDepartment().getId())
                                .header(BREAKAGE_ID_HEADER, SOME_NOT_EXIST_ID))
                        .andExpect(MockMvcResultMatchers.status().isNotFound())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                                .value(GET_BREAKAGE_NOT_EXIST))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                ApiResponse actualApiResponse = objectMapper.readValue(result, ApiResponse.class);

                assertThat(actualApiResponse.message()).isEqualTo(apiResponse.message());
            }
        }

        @Nested
        @Transactional
        class WhenBreakageGetting {

            @Test
            @SneakyThrows
            void whenGetBreakageThenReturnBreakage() {

                BreakageCommentFrontDto comment = BreakageCommentFrontDto.builder()
                        .id(breakageComment.getId())
                        .comment(breakageComment.getComment())
                        .actionEnabled(false)
                        .creatorName(defaultAdminUser.getUsername())
                        .createdDate(breakageComment.getCreatedDate())
                        .lastUpdatedDate(breakageComment.getLastUpdatedDate())
                        .build();

                BreakageFullDto breakageFullDto = BreakageFullDto.builder()
                        .id(employeeCurrentBreakage.getId())
                        .departmentId(employeeCurrentBreakage.getDepartment().getId())
                        .breakageExecutorId("")
                        .departmentName(employeeCurrentBreakage.getDepartment().getName())
                        .room(employeeCurrentBreakage.getRoom())
                        .breakageTopic(employeeCurrentBreakage.getBreakageTopic())
                        .breakageText(employeeCurrentBreakage.getBreakageText())
                        .status(employeeCurrentBreakage.getStatus())
                        .priority(employeeCurrentBreakage.getPriority())
                        .breakageExecutor(NO_APPOINTED_EXECUTOR)
                        .executorAppointedBy(EXECUTOR_APPOINTED_BY)
                        .createdBy(employeeCurrentUser.getUsername())
                        .createdDate(employeeCurrentBreakage.getCreatedDate())
                        .lastUpdatedBy(employeeCurrentUser.getUsername())
                        .lastUpdatedDate(employeeCurrentBreakage.getCreatedDate())
                        .comments(List.of(comment))
                        .build();

                String responseMessage = "Заявка на неисправность с ID=" + employeeCurrentBreakage.getId() +
                        ", получена успешно";

                ApiResponse apiResponse = ApiResponse.builder()
                        .message(responseMessage)
                        .status(200)
                        .httpStatus(HttpStatus.OK)
                        .timestamp(now)
                        .data(breakageFullDto)
                        .build();

                String result = mockMvc.perform(MockMvcRequestBuilders.get(
                                        BASE_URL + BREAKAGE_URL + TECHNICIAN_URL + CURRENT_URL
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(CURRENT_USER_ID_HEADER, technicianUser.getId())
                                .header(BREAKAGE_ID_HEADER, employeeCurrentBreakage.getId()))
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(200))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value(HttpStatus.OK.name()))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(dtf.format(now)))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                LinkedHashMap<String, Object> dataMap = JsonPath.read(result, "$.data");
                BreakageFullDto returnedBreakageFullDto = objectMapper.convertValue(dataMap, BreakageFullDto.class);

                ApiResponse actualApiResponse = objectMapper.readValue(result, ApiResponse.class);

                assertThat(actualApiResponse.message()).isEqualTo(apiResponse.message());
                assertThat(actualApiResponse.status()).isEqualTo(apiResponse.status());
                assertThat(actualApiResponse.httpStatus()).isEqualTo(apiResponse.httpStatus());
                assertThat(actualApiResponse.timestamp()).isEqualTo(apiResponse.timestamp());
                assertThat(returnedBreakageFullDto).isEqualTo(apiResponse.data());

                assertThat(returnedBreakageFullDto.id())
                        .isEqualTo(breakageFullDto.id());
                assertThat(returnedBreakageFullDto.departmentId())
                        .isEqualTo(breakageFullDto.departmentId());
                assertThat(returnedBreakageFullDto.departmentName())
                        .isEqualTo(breakageFullDto.departmentName());
                assertThat(returnedBreakageFullDto.breakageExecutorId())
                        .isEqualTo(breakageFullDto.breakageExecutorId());
                assertThat(returnedBreakageFullDto.room())
                        .isEqualTo(breakageFullDto.room());
                assertThat(returnedBreakageFullDto.breakageTopic())
                        .isEqualTo(breakageFullDto.breakageTopic());
                assertThat(returnedBreakageFullDto.breakageText())
                        .isEqualTo(breakageFullDto.breakageText());
                assertThat(returnedBreakageFullDto.status())
                        .isEqualTo(breakageFullDto.status());
                assertThat(returnedBreakageFullDto.priority())
                        .isEqualTo(breakageFullDto.priority());
                assertThat(returnedBreakageFullDto.breakageExecutor())
                        .isEqualTo(breakageFullDto.breakageExecutor());
                assertThat(returnedBreakageFullDto.executorAppointedBy())
                        .isEqualTo(breakageFullDto.executorAppointedBy());
                assertThat(returnedBreakageFullDto.createdBy())
                        .isEqualTo(breakageFullDto.createdBy());
                assertThat(returnedBreakageFullDto.createdDate())
                        .isEqualTo(breakageFullDto.createdDate());
                assertThat(returnedBreakageFullDto.lastUpdatedBy())
                        .isEqualTo(breakageFullDto.lastUpdatedBy());
                assertThat(returnedBreakageFullDto.lastUpdatedDate())
                        .isEqualTo(breakageFullDto.lastUpdatedDate());
                assertThat(returnedBreakageFullDto.deadline())
                        .isEqualTo(breakageFullDto.deadline());
                assertThat(returnedBreakageFullDto.comments().getFirst())
                        .isEqualTo(breakageFullDto.comments().getFirst());
            }

            @Test
            @SneakyThrows
            void whenGetBreakageThenReturnNotFoundException() {

                ApiResponse apiResponse = ApiResponse.builder()
                        .message(GET_BREAKAGE_NOT_EXIST)
                        .status(404)
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .timestamp(now)
                        .build();

                String result = mockMvc.perform(MockMvcRequestBuilders.get(
                                        BASE_URL + BREAKAGE_URL +
                                                TECHNICIAN_URL + CURRENT_URL
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(CURRENT_USER_ID_HEADER, defaultAdminUser.getId())
                                .header(BREAKAGE_ID_HEADER, SOME_NOT_EXIST_ID))
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isNotFound())
                        .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(GET_BREAKAGE_NOT_EXIST))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                ApiResponse actualApiResponse = objectMapper.readValue(result, ApiResponse.class);

                assertThat(actualApiResponse.message()).isEqualTo(apiResponse.message());
            }
        }

        @Nested
        @Transactional
        class WhenBreakageCommentMethodsAreInvoked {

            private CreateBreakageCommentDto createBreakageCommentDto;

            @BeforeEach
            void setUp() {

                createBreakageCommentDto =
                        new CreateBreakageCommentDto(BREAKAGE_COMMENT_TEST_TEXT, Status.NEW);
            }

            @Test
            @SneakyThrows
            void whenCreateBreakageCommentThenReturnCreated() {

                String responseMessage = "Комментарий к заявке о неисправности - был успешно создан.";

                ApiResponse apiResponse = ApiResponse.builder()
                        .message(responseMessage)
                        .status(201)
                        .httpStatus(HttpStatus.CREATED)
                        .timestamp(now)
                        .build();

                String result = mockMvc.perform(MockMvcRequestBuilders.post(
                                        BASE_URL + BREAKAGE_URL + TECHNICIAN_URL + BREAKAGE_COMMENT_URL
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(CURRENT_USER_ID_HEADER, technicianUser.getId())
                                .header(BREAKAGE_ID_HEADER, employeeCurrentBreakage.getId())
                                .content(objectMapper.writeValueAsString(createBreakageCommentDto)))
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(201))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value(HttpStatus.CREATED.name()))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(dtf.format(now)))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                List<BreakageComment> breakageComments = breakageCommentRepository.findAll();
                assertThat(breakageComments.size()).isEqualTo(2);

                ApiResponse actualApiResponse = objectMapper.readValue(result, ApiResponse.class);

                assertThat(actualApiResponse.message()).isEqualTo(apiResponse.message());
                assertThat(actualApiResponse.status()).isEqualTo(apiResponse.status());
                assertThat(actualApiResponse.httpStatus()).isEqualTo(apiResponse.httpStatus());
                assertThat(actualApiResponse.timestamp()).isEqualTo(apiResponse.timestamp());
                assertThat(actualApiResponse.data()).isEqualTo(apiResponse.data());
            }

            @Test
            @SneakyThrows
            void whenCreateBreakageCommentThenReturnThenReturnNotFoundException() {

                String responseMessage = "Заявки на неисправность не существует !!!";

                ApiResponse apiResponse = ApiResponse.builder()
                        .message(responseMessage)
                        .status(404)
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .timestamp(now)
                        .build();

                String result = mockMvc.perform(MockMvcRequestBuilders.post(
                                        BASE_URL + BREAKAGE_URL + TECHNICIAN_URL + BREAKAGE_COMMENT_URL
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(CURRENT_USER_ID_HEADER, technicianUser.getId())
                                .header(BREAKAGE_ID_HEADER, SOME_NOT_EXIST_ID)
                                .content(objectMapper.writeValueAsString(createBreakageCommentDto)))
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isNotFound())
                        .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                ApiResponse actualApiResponse = objectMapper.readValue(result, ApiResponse.class);

                assertThat(actualApiResponse.message()).isEqualTo(apiResponse.message());
            }

            @Test
            @SneakyThrows
            void whenCreateBreakageCommentThenReturnThenReturnNotCorrectParameter() {

                CreateBreakageCommentDto commentDto =
                        new CreateBreakageCommentDto(BREAKAGE_COMMENT_TEST_TEXT, Status.SOLVED);

                String responseMessage = "Комментарии к заявке о неисправности со статусами " +
                        "\"Решена\" и \"Отменена\" - не создаются !!!";

                ApiResponse apiResponse = ApiResponse.builder()
                        .message(responseMessage)
                        .status(400)
                        .httpStatus(HttpStatus.BAD_REQUEST)
                        .timestamp(now)
                        .build();

                String result = mockMvc.perform(MockMvcRequestBuilders.post(
                                        BASE_URL + BREAKAGE_URL + TECHNICIAN_URL + BREAKAGE_COMMENT_URL
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(CURRENT_USER_ID_HEADER, technicianUser.getId())
                                .header(BREAKAGE_ID_HEADER, employeeCurrentBreakage.getId())
                                .content(objectMapper.writeValueAsString(commentDto)))
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isBadRequest())
                        .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                ApiResponse actualApiResponse = objectMapper.readValue(result, ApiResponse.class);

                assertThat(actualApiResponse.message()).isEqualTo(apiResponse.message());
            }

            @Test
            @SneakyThrows
            void whenUpdateBreakageCommentThenReturnOk() {

                String responseMessage = "Комментарий к заявке на неисправность был успешно обновлен.";

                ApiResponse apiResponse = ApiResponse.builder()
                        .message(responseMessage)
                        .status(200)
                        .httpStatus(HttpStatus.OK)
                        .timestamp(now)
                        .build();

                String result = mockMvc.perform(MockMvcRequestBuilders.patch(
                                        BASE_URL + BREAKAGE_URL + TECHNICIAN_URL + BREAKAGE_COMMENT_URL
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(CURRENT_USER_ID_HEADER, technicianUser.getId())
                                .header(BREAKAGE_COMMENT_ID_HEADER, breakageComment.getId())
                                .content(objectMapper.writeValueAsString(createBreakageCommentDto)))
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(200))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value(HttpStatus.OK.name()))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(dtf.format(now)))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                ApiResponse actualApiResponse = objectMapper.readValue(result, ApiResponse.class);

                assertThat(actualApiResponse).isEqualTo(apiResponse);
            }

            @Test
            @SneakyThrows
            void whenUpdateBreakageCommentThenReturnNotFoundException() {

                ApiResponse apiResponse = ApiResponse.builder()
                        .message(BREAKAGE_COMMENT_NOT_EXIST)
                        .status(404)
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .timestamp(now)
                        .build();

                String result = mockMvc.perform(MockMvcRequestBuilders.patch(
                                        BASE_URL + BREAKAGE_URL + TECHNICIAN_URL + BREAKAGE_COMMENT_URL
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(CURRENT_USER_ID_HEADER, technicianUser.getId())
                                .header(BREAKAGE_COMMENT_ID_HEADER, SOME_NOT_EXIST_ID)
                                .content(objectMapper.writeValueAsString(createBreakageCommentDto)))
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isNotFound())
                        .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(BREAKAGE_COMMENT_NOT_EXIST))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                ApiResponse actualApiResponse = objectMapper.readValue(result, ApiResponse.class);

                assertThat(actualApiResponse.message()).isEqualTo(apiResponse.message());
            }

            @Test
            @SneakyThrows
            void whenDeleteBreakageCommentThenReturnOk() {

                String responseMessage = "Комментарий к заявке на неисправность был успешно удален.";

                ApiResponse apiResponse = ApiResponse.builder()
                        .message(responseMessage)
                        .status(200)
                        .httpStatus(HttpStatus.OK)
                        .timestamp(now)
                        .build();

                String result = mockMvc.perform(MockMvcRequestBuilders.delete(
                                        BASE_URL + BREAKAGE_URL + TECHNICIAN_URL + BREAKAGE_COMMENT_URL
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(BREAKAGE_COMMENT_ID_HEADER, breakageComment.getId()))
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(200))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value(HttpStatus.OK.name()))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(dtf.format(now)))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                ApiResponse actualApiResponse = objectMapper.readValue(result, ApiResponse.class);

                assertThat(actualApiResponse).isEqualTo(apiResponse);
            }
        }
    }
}