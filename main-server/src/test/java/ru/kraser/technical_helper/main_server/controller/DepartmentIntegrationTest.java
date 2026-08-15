package ru.kraser.technical_helper.main_server.controller;

import com.fasterxml.jackson.core.type.TypeReference;
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
import ru.kraser.technical_helper.common_module.dto.department.CreateDepartmentDto;
import ru.kraser.technical_helper.common_module.dto.department.DepartmentDto;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.kraser.technical_helper.common_module.util.Constant.*;
import static ru.kraser.technical_helper.common_module.util.ConstantForTests.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class DepartmentIntegrationTest {

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

    private Department department;
    private Department enabledDepartment;
    private Department notEnabledDepartment;
    private CreateDepartmentDto createDepartmentDto;
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

        when(clock.getZone()).thenReturn(NOW_ZDT.getZone());
        when(clock.instant()).thenReturn(NOW_ZDT.toInstant());
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class WhenDepartmentMethods {

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

            dtf = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");

            department = Department.builder()
                    .name(DEPARTMENT_TEST_NAME)
                    .enabled(true)
                    .createdBy(defaultAdminUser.getId())
                    .createdDate(now)
                    .lastUpdatedBy(defaultAdminUser.getId())
                    .lastUpdatedDate(now)
                    .build();

            Department toSaveNotEnabledDepartment = Department.builder()
                    .name(DEPARTMENT_TEST_OTHER_NAME)
                    .enabled(false)
                    .createdBy(defaultAdminUser.getId())
                    .createdDate(now)
                    .lastUpdatedBy(defaultAdminUser.getId())
                    .lastUpdatedDate(now)
                    .build();

            createDepartmentDto = new CreateDepartmentDto(DEPARTMENT_TEST_NEW_NAME);

            enabledDepartment = departmentRepository.saveAndFlush(department);
            notEnabledDepartment = departmentRepository.saveAndFlush(toSaveNotEnabledDepartment);
        }

        @Nested
        class WhenDepartmentCreating {

            @Test
            @Transactional
            @SneakyThrows
            void whenCreateDepartmentThenReturnCreated() {

                String responseMessage = "Отдел: " + DEPARTMENT_TEST_NEW_NAME + ", - был успешно создан.";

                ApiResponse apiResponse = ApiResponse.builder()
                        .message(responseMessage)
                        .status(201)
                        .httpStatus(HttpStatus.CREATED)
                        .timestamp(now)
                        .build();

                String result = mockMvc.perform(MockMvcRequestBuilders.post(
                                        BASE_URL + ADMIN_URL + DEPARTMENT_URL
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(CURRENT_USER_ID_HEADER, defaultAdminUser.getId())
                                .content(objectMapper.writeValueAsString(createDepartmentDto)))
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(201))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value("CREATED"))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(dtf.format(now)))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                Example<Department> example = Example.of(department);
                Department savedDepartment = departmentRepository.findOne(example).get();

                assertThat(savedDepartment.getId()).isNotNull();
                assertThat(savedDepartment.getName()).isEqualTo(department.getName());
                assertThat(savedDepartment.isEnabled()).isEqualTo(department.isEnabled());
                assertThat(savedDepartment.getCreatedBy()).isEqualTo(department.getCreatedBy());
                assertThat(savedDepartment.getCreatedDate()).isEqualTo(department.getCreatedDate());
                assertThat(savedDepartment.getLastUpdatedBy()).isEqualTo(department.getLastUpdatedBy());
                assertThat(savedDepartment.getLastUpdatedDate()).isEqualTo(department.getLastUpdatedDate());

                ApiResponse actualApiResponse = objectMapper.readValue(result, ApiResponse.class);
                assertThat(actualApiResponse).isEqualTo(apiResponse);
            }

            @Test
            @SneakyThrows
            void whenCreateDepartmentThenReturnAlreadyExistsException() {

                String responseMessage = "Отдел: " + defaultAdminDepartment.getName() + ", - уже существует. " +
                        "Используйте другое имя !!!";

                CreateDepartmentDto createDepartmentDtoWithNotUniqueName =
                        new CreateDepartmentDto(defaultAdminDepartment.getName());

                String result = mockMvc.perform(MockMvcRequestBuilders.post(
                        BASE_URL + ADMIN_URL + DEPARTMENT_URL
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(CURRENT_USER_ID_HEADER, defaultAdminUser.getId())
                                .content(objectMapper.writeValueAsString(createDepartmentDtoWithNotUniqueName)))
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                                .value(responseMessage))
                        .andExpect(status().isUnprocessableEntity())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                AlreadyExistsException exception = objectMapper.readValue(result, AlreadyExistsException.class);
                assertThat(exception.getMessage()).isEqualTo(responseMessage);
            }
        }

        @Nested
        class WhenDepartmentUpdating {

            @Test
            @Transactional
            @SneakyThrows
            void whenUpdateDepartmentThenReturnOk() {

                CreateDepartmentDto updateDepartmentDto = new CreateDepartmentDto(DEPARTMENT_TEST_NEW_NAME);

                String responseMessage = "Отдел: " + updateDepartmentDto.name() + " - был успешно изменен.";

                ApiResponse apiResponse = ApiResponse.builder()
                        .message(responseMessage)
                        .status(200)
                        .httpStatus(HttpStatus.OK)
                        .timestamp(now)
                        .build();

                String result = mockMvc.perform(MockMvcRequestBuilders.patch(
                        BASE_URL + ADMIN_URL + DEPARTMENT_URL
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(CURRENT_USER_ID_HEADER, defaultAdminUser.getId())
                                .header(DEPARTMENT_ID_HEADER, enabledDepartment.getId())
                                .content(objectMapper.writeValueAsString(updateDepartmentDto)))
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
                Department updatedDepartment = departmentRepository.findById(enabledDepartment.getId()).get();

                assertThat(result).isEqualTo(objectMapper.writeValueAsString(apiResponse));
                assertThat(updatedDepartment.getName()).isEqualTo(updateDepartmentDto.name());
            }


            @Test
            @Transactional
            @SneakyThrows
            void whenUpdateDepartmentThenReturnAlreadyExistsException() {

                String responseMessage = "Отдел: " + defaultAdminDepartment.getName() + ", - уже существует. " +
                        "Используйте другое имя !!!";

                CreateDepartmentDto departmentDtoWithNotUniqueName =
                        new CreateDepartmentDto(defaultAdminDepartment.getName());

                String result = mockMvc.perform(MockMvcRequestBuilders.patch(
                        BASE_URL + ADMIN_URL + DEPARTMENT_URL
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(CURRENT_USER_ID_HEADER, defaultAdminUser.getId())
                                .header(DEPARTMENT_ID_HEADER, enabledDepartment.getId())
                                .content(objectMapper.writeValueAsString(departmentDtoWithNotUniqueName)))
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                                .value(responseMessage))
                        .andExpect(status().isUnprocessableEntity())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                AlreadyExistsException exception = objectMapper.readValue(result, AlreadyExistsException.class);
                assertThat(exception.getMessage()).isEqualTo(responseMessage);
            }

            @Test
            @SneakyThrows
            void whenUpdateDepartmentThenReturnNotFound() {

                String responseMessage = "Данный отдел не существует !!!";

                String result = mockMvc.perform(MockMvcRequestBuilders.patch(
                        BASE_URL + ADMIN_URL + DEPARTMENT_URL
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(CURRENT_USER_ID_HEADER, defaultAdminUser.getId())
                                .header(DEPARTMENT_ID_HEADER, SOME_NOT_EXIST_ID)
                                .content(objectMapper.writeValueAsString(createDepartmentDto)))
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                                .value(responseMessage))
                        .andExpect(status().isNotFound())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                NotFoundException exception = objectMapper.readValue(result, NotFoundException.class);
                assertThat(exception.getMessage()).isEqualTo(responseMessage);
            }
        }

        @Nested
        class WhenGetMethodsAreExecuting {

            @Test
            @SneakyThrows
            void whenGetAllDepartmentsThenReturnDepartmentDtoList() {

                String result = mockMvc.perform(MockMvcRequestBuilders.get(
                                        BASE_URL + ADMIN_URL + DEPARTMENT_URL + ALL_URL)
                                .accept(MediaType.APPLICATION_JSON))
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                List<DepartmentDto> departments = objectMapper.readValue(result, new TypeReference<>() {});
                assertThat(departments).hasSize(2);

                List<Department> departmentList = departmentRepository.findAll();
                assertThat(departmentList).hasSize(3);
            }

            @Test
            @SneakyThrows
            void whenGetDepartmentByIdThenReturnDepartmentDto() {

                DepartmentDto expectedDepartmentDto = DepartmentDto.builder()
                        .id(defaultAdminDepartment.getId())
                        .name(defaultAdminDepartment.getName())
                        .createdBy(defaultAdminUser.getUsername())
                        .createdDate(defaultAdminDepartment.getCreatedDate())
                        .lastUpdatedBy(defaultAdminUser.getUsername())
                        .lastUpdatedDate(defaultAdminDepartment.getLastUpdatedDate())
                        .build();

                String result = mockMvc.perform(MockMvcRequestBuilders.get(
                                        BASE_URL + ADMIN_URL + DEPARTMENT_URL + CURRENT_URL)
                                .accept(MediaType.APPLICATION_JSON)
                                .header(DEPARTMENT_ID_HEADER, expectedDepartmentDto.id()))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                DepartmentDto departmentDto = objectMapper.readValue(result, new TypeReference<>() {});

                assertThat(departmentDto.id()).isEqualTo(expectedDepartmentDto.id());
                assertThat(departmentDto.name()).isEqualTo(expectedDepartmentDto.name());
                assertThat(departmentDto.createdBy()).isEqualTo(expectedDepartmentDto.createdBy());
                assertThat(departmentDto.createdDate()).isEqualTo(expectedDepartmentDto.createdDate());
                assertThat(departmentDto.lastUpdatedBy()).isEqualTo(expectedDepartmentDto.lastUpdatedBy());
                assertThat(departmentDto.lastUpdatedDate()).isEqualTo(expectedDepartmentDto.lastUpdatedDate());
            }

            @Test
            @SneakyThrows
            void whenGetDepartmentWhichNotEnabledThenReturnNotFoundException() {

                String responseMessage = "Данного отдела не существует !!!";

                String result = mockMvc.perform(MockMvcRequestBuilders.get(
                                        BASE_URL + ADMIN_URL + DEPARTMENT_URL + CURRENT_URL)
                                .accept(MediaType.APPLICATION_JSON)
                                .header(DEPARTMENT_ID_HEADER, notEnabledDepartment.getId()))
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isNotFound())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                NotFoundException notFoundException = objectMapper.readValue(result, new TypeReference<>() {});

                assertThat(notFoundException.getMessage()).isEqualTo(responseMessage);
            }

            @Test
            @SneakyThrows
            void whenGetDepartmentWhichNotExistThenReturnNotFoundException() {

                String responseMessage = "Данного отдела не существует !!!";

                String result = mockMvc.perform(MockMvcRequestBuilders.get(
                                        BASE_URL + ADMIN_URL + DEPARTMENT_URL + CURRENT_URL)
                                .accept(MediaType.APPLICATION_JSON)
                                .header(DEPARTMENT_ID_HEADER, SOME_NOT_EXIST_ID))
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isNotFound())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

                NotFoundException notFoundException = objectMapper.readValue(result, new TypeReference<>() {});

                assertThat(notFoundException.getMessage()).isEqualTo(responseMessage);
            }
        }

        @Test
        @Transactional
        @SneakyThrows
        void whenDeleteDepartmentThenReturnOk() {

            String responseMessage = "Отдел - был успешно удалён.";

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(responseMessage)
                    .status(200)
                    .httpStatus(HttpStatus.OK)
                    .timestamp(now)
                    .build();

            String result = mockMvc.perform(MockMvcRequestBuilders.patch(
                                    BASE_URL + ADMIN_URL + DEPARTMENT_URL + DELETE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(CURRENT_USER_ID_HEADER, defaultAdminUser.getId())
                            .header(DEPARTMENT_ID_HEADER, enabledDepartment.getId()))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(responseMessage))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(200))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus").value("OK"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(dtf.format(now)))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            ApiResponse actualApiResponse = objectMapper.readValue(result, ApiResponse.class);
            assertThat(actualApiResponse).isEqualTo(apiResponse);

            entityManager.clear();
            Department deletedDepartment = departmentRepository.findById(enabledDepartment.getId()).get();

            assertThat(deletedDepartment.isEnabled()).isFalse();
        }
    }
}
