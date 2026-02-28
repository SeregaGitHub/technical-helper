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

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class DepartmentControllerIntegrationTest {

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

        defaultAdminDepartment = departmentRepository.findById("d1d11111-11d1-1d11-1111-d111111d1111").get();
        defaultAdminUser = userRepository.findById("u1u11111-11u1-1u11-1111-u111111u1111").get();

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

        department = Department.builder()
                .name("test_department")
                .enabled(true)
                .createdBy(defaultAdminUser.getId())
                .createdDate(now)
                .lastUpdatedBy(defaultAdminUser.getId())
                .lastUpdatedDate(now)
                .build();

        createDepartmentDto = new CreateDepartmentDto(department.getName());

    }

    @Nested
    class WhenDepartmentCreating {

        @SneakyThrows
        @Test
        void whenCreateDepartmentThenReturnCreated() {

            String responseMessage = "Отдел: " + createDepartmentDto.name() + ", - был успешно создан.";

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
            departmentRepository.deleteById(savedDepartment.getId());

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

        @SneakyThrows
        @Test
        void whenCreateDepartmentThenReturnAlreadyExistsException() {

            String responseMessage = "Отдел: " + defaultAdminDepartment.getName() + ", - уже существует. " +
                    "Используйте другое имя !!!";

            CreateDepartmentDto createDepartmentDtoWithNotUniqueName =
                    new CreateDepartmentDto(defaultAdminDepartment.getName());

            String result = mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + ADMIN_URL + DEPARTMENT_URL)
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

        @SneakyThrows
        @Test
        void whenUpdateDepartmentThenReturnOk() {

            Department savedDepartment = departmentRepository.saveAndFlush(department);
            CreateDepartmentDto updateDepartmentDto = new CreateDepartmentDto("new_department_name");

            String responseMessage = "Отдел: " + updateDepartmentDto.name() + " - был успешно изменен.";

            ApiResponse apiResponse = ApiResponse.builder()
                    .message(responseMessage)
                    .status(200)
                    .httpStatus(HttpStatus.OK)
                    .timestamp(now)
                    .build();

            String result = mockMvc.perform(MockMvcRequestBuilders.patch(BASE_URL + ADMIN_URL + DEPARTMENT_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(CURRENT_USER_ID_HEADER, defaultAdminUser.getId())
                            .header(DEPARTMENT_ID_HEADER, savedDepartment.getId())
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
            Department updatedDepartment = departmentRepository.findById(savedDepartment.getId()).get();
            departmentRepository.deleteById(updatedDepartment.getId());

            assertThat(result).isEqualTo(objectMapper.writeValueAsString(apiResponse));
            assertThat(updatedDepartment.getName()).isEqualTo(updateDepartmentDto.name());
        }

        @SneakyThrows
        @Test
        void whenUpdateDepartmentThenReturnAlreadyExistsException() {

            String responseMessage = "Отдел: " + defaultAdminDepartment.getName() + ", - уже существует. " +
                    "Используйте другое имя !!!";

            Department savedDepartment = departmentRepository.saveAndFlush(department);
            CreateDepartmentDto departmentDtoWithNotUniqueName =
                    new CreateDepartmentDto(defaultAdminDepartment.getName());

            String result = mockMvc.perform(MockMvcRequestBuilders.patch(BASE_URL + ADMIN_URL + DEPARTMENT_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(CURRENT_USER_ID_HEADER, defaultAdminUser.getId())
                            .header(DEPARTMENT_ID_HEADER, savedDepartment.getId())
                            .content(objectMapper.writeValueAsString(departmentDtoWithNotUniqueName)))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                            .value(responseMessage))
                    .andExpect(status().isUnprocessableEntity())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            departmentRepository.deleteById(savedDepartment.getId());

            AlreadyExistsException exception = objectMapper.readValue(result, AlreadyExistsException.class);
            assertThat(exception.getMessage()).isEqualTo(responseMessage);
        }

        @SneakyThrows
        @Test
        void whenUpdateDepartmentThenReturnNotFound() {

            String responseMessage = "Данный отдел не существует !!!";

            String result = mockMvc.perform(MockMvcRequestBuilders.patch(BASE_URL + ADMIN_URL + DEPARTMENT_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(CURRENT_USER_ID_HEADER, defaultAdminUser.getId())
                            .header(DEPARTMENT_ID_HEADER, "some_not_exist_id")
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

        DepartmentDto expectedDepartmentDto;
        Department notEnabledDepartment;

        @BeforeEach
        void initializeDepartmentDto() {

            expectedDepartmentDto = DepartmentDto.builder()
                    .id(defaultAdminDepartment.getId())
                    .name(defaultAdminDepartment.getName())
                    .createdBy(defaultAdminUser.getUsername())
                    .createdDate(defaultAdminDepartment.getCreatedDate())
                    .lastUpdatedBy(defaultAdminUser.getUsername())
                    .lastUpdatedDate(defaultAdminDepartment.getLastUpdatedDate())
                    .build();

            Department toSaveDepartment = Department.builder()
                    .name(department.getName())
                    .enabled(false)
                    .createdBy(department.getCreatedBy())
                    .createdDate(department.getCreatedDate())
                    .lastUpdatedBy(department.getLastUpdatedBy())
                    .lastUpdatedDate(department.getLastUpdatedDate())
                    .build();

            notEnabledDepartment = departmentRepository.saveAndFlush(toSaveDepartment);
        }

        @AfterEach
        void removeNotEnabledDepartment() {

            departmentRepository.deleteById(notEnabledDepartment.getId());
        }

        @SneakyThrows
        @Test
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
            assertThat(departments).hasSize(1);

            DepartmentDto departmentDto = departments.getFirst();

            assertThat(departmentDto.id()).isEqualTo(expectedDepartmentDto.id());
            assertThat(departmentDto.name()).isEqualTo(expectedDepartmentDto.name());
            assertThat(departmentDto.createdBy()).isEqualTo(expectedDepartmentDto.createdBy());
            assertThat(departmentDto.createdDate()).isEqualTo(expectedDepartmentDto.createdDate());
            assertThat(departmentDto.lastUpdatedBy()).isEqualTo(expectedDepartmentDto.lastUpdatedBy());
            assertThat(departmentDto.lastUpdatedDate()).isEqualTo(expectedDepartmentDto.lastUpdatedDate());
        }

        @SneakyThrows
        @Test
        void whenGetDepartmentByIdThenReturnDepartmentDto() {

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

        @SneakyThrows
        @Test
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

        @SneakyThrows
        @Test
        void whenGetDepartmentWhichNotExistThenReturnNotFoundException() {

            String responseMessage = "Данного отдела не существует !!!";

            String result = mockMvc.perform(MockMvcRequestBuilders.get(
                                    BASE_URL + ADMIN_URL + DEPARTMENT_URL + CURRENT_URL)
                            .accept(MediaType.APPLICATION_JSON)
                            .header(DEPARTMENT_ID_HEADER, "some_not_exist_department_id"))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            NotFoundException notFoundException = objectMapper.readValue(result, new TypeReference<>() {});

            assertThat(notFoundException.getMessage()).isEqualTo(responseMessage);
        }
    }

    @SneakyThrows
    @Test
    void whenDeleteDepartmentThenReturnOk() {

        String responseMessage = "Отдел - был успешно удалён.";

        Department savedDepartment = departmentRepository.saveAndFlush(department);

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
                        .header(DEPARTMENT_ID_HEADER, savedDepartment.getId()))
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
        departmentRepository.deleteDepartment(savedDepartment.getId(), defaultAdminUser.getId(), now);
        Department deletedDepartment = departmentRepository.findById(savedDepartment.getId()).get();

        assertThat(deletedDepartment.isEnabled()).isFalse();
    }
}
