package ru.kraser.technical_helper.breakage_server.controller;

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
import ru.kraser.technical_helper.breakage_server.repository.BreakageRepository;
import ru.kraser.technical_helper.common_module.dto.api.ApiResponse;
import ru.kraser.technical_helper.common_module.dto.breakage.CreateBreakageFullDto;
import ru.kraser.technical_helper.common_module.dto.breakage.UpdateBreakageStatusDto;
import ru.kraser.technical_helper.common_module.dto.user.CreateUserDto;
import ru.kraser.technical_helper.common_module.enums.Priority;
import ru.kraser.technical_helper.common_module.enums.Role;
import ru.kraser.technical_helper.common_module.enums.Status;
import ru.kraser.technical_helper.common_module.exception.NotFoundException;
import ru.kraser.technical_helper.common_module.model.Breakage;
import ru.kraser.technical_helper.common_module.model.Department;
import ru.kraser.technical_helper.common_module.model.User;
import ru.kraser.technical_helper.main_server.repository.DepartmentRepository;
import ru.kraser.technical_helper.main_server.repository.UserRepository;
import ru.kraser.technical_helper.main_server.util.mapper.UserMapper;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

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

    //private Breakage testBreakage;
    private Breakage employeeCurrentBreakage;



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
    class WhenBreakageRepositoryDataModifyingMethodsAreInvoked {

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
                    .username(USER_TEST_OTHER_NAME)
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
                    .username(USER_TEST_NAME)
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
        }

        @AfterAll
        void cleanupData() {

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
        @Transactional()
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
        @Transactional()
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
        @Transactional()
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


    }


//
//    @Test
//    void updateBreakagePriority() {
//    }
//
//    @Test
//    void addBreakageExecutor() {
//    }
//
//    @Test
//    void dropBreakageExecutor() {
//    }
//
//    @Test
//    void getAllBreakages() {
//    }
//
//    @Test
//    void getBreakageEmployee() {
//    }
//
//    @Test
//    void getBreakage() {
//    }
//
//    @Test
//    void createBreakageComment() {
//    }
//
//    @Test
//    void updateBreakageComment() {
//    }
//
//    @Test
//    void deleteBreakageComment() {
//    }
}