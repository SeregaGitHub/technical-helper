package ru.kraser.technical_helper.main_server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static ru.kraser.technical_helper.common_module.util.Constant.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class DepartmentControllerIntegrationTest {

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

        defaultAdminUser = userRepository.findById("u1u11111-11u1-1u11-1111-u111111u1111").get();

        now = LocalDateTime.of(
                2025,
                9,
                29,
                13,
                0,
                0);

        dtf = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");

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

            when(clock.getZone()).thenReturn(NOW_ZDT.getZone());
            when(clock.instant()).thenReturn(NOW_ZDT.toInstant());

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
    }
}
