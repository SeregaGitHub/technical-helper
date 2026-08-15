package ru.kraser.technical_helper.breakage_server.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.kraser.technical_helper.BreakageServer;
import ru.kraser.technical_helper.common_module.dto.breakage_comment.BreakageCommentBackendDto;
import ru.kraser.technical_helper.common_module.enums.Priority;
import ru.kraser.technical_helper.common_module.enums.Status;
import ru.kraser.technical_helper.common_module.model.Breakage;
import ru.kraser.technical_helper.common_module.model.BreakageComment;
import ru.kraser.technical_helper.common_module.model.Department;
import ru.kraser.technical_helper.common_module.model.User;
import ru.kraser.technical_helper.main_server.repository.DepartmentRepository;
import ru.kraser.technical_helper.main_server.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.kraser.technical_helper.common_module.util.ConstantForTests.*;

@Testcontainers
@SpringBootTest
@ContextConfiguration(classes = BreakageServer.class)
class BreakageCommentRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    BreakageRepository breakageRepository;
    @Autowired
    BreakageCommentRepository breakageCommentRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    DepartmentRepository departmentRepository;

    private LocalDateTime now;
    private User defaultAdminUser;
    private Department defaultAdminDepartment;
    private Breakage testBreakage;
    BreakageComment toSaveBreakageComment;

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
    class WhenBreakageCommentMethodsAreInvoked {

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

            Breakage toSaveBreakage = Breakage.builder()
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

            testBreakage = breakageRepository.saveAndFlush(toSaveBreakage);
        }

        @BeforeEach
        void setUp() {
            toSaveBreakageComment = BreakageComment.builder()
                    .breakage(testBreakage)
                    .comment(BREAKAGE_COMMENT_TEST_TEXT)
                    .createdBy(defaultAdminUser.getId())
                    .createdDate(now)
                    .lastUpdatedBy(defaultAdminUser.getId())
                    .lastUpdatedDate(now)
                    .build();
        }

        @Test
        @Transactional
        void whenCreateBreakageCommentThenReturnBreakageComment() {

            BreakageComment savedBreakageComment = breakageCommentRepository.saveAndFlush(toSaveBreakageComment);

            assertThat(savedBreakageComment.getId()).isNotNull();
            assertThat(savedBreakageComment.getBreakage().getId()).isEqualTo(toSaveBreakageComment.getBreakage().getId());
            assertThat(savedBreakageComment.getComment()).isEqualTo(toSaveBreakageComment.getComment());
            assertThat(savedBreakageComment.getCreatedBy()).isEqualTo(toSaveBreakageComment.getCreatedBy());
            assertThat(savedBreakageComment.getCreatedDate()).isEqualTo(toSaveBreakageComment.getCreatedDate());
            assertThat(savedBreakageComment.getLastUpdatedBy()).isEqualTo(toSaveBreakageComment.getLastUpdatedBy());
            assertThat(savedBreakageComment.getLastUpdatedDate()).isEqualTo(toSaveBreakageComment.getLastUpdatedDate());
        }

        @Test
        void whenCreateBreakageCommentWithNotExistBreakageThenThrowException() {

            Breakage notExistBreakage = Breakage.builder()
                    .id(SOME_NOT_EXIST_ID)
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

            BreakageComment toSaveBreakageCommentWithNotExistBreakage = BreakageComment.builder()
                    .breakage(notExistBreakage)
                    .comment(BREAKAGE_COMMENT_TEST_TEXT)
                    .createdBy(defaultAdminUser.getId())
                    .createdDate(now)
                    .lastUpdatedBy(defaultAdminUser.getId())
                    .lastUpdatedDate(now)
                    .build();

            assertThrows(
                    DataIntegrityViolationException.class,
                    () -> breakageCommentRepository.saveAndFlush(toSaveBreakageCommentWithNotExistBreakage)
            );
        }

        @Test
        @Transactional
        @Modifying(clearAutomatically = true)
        void whenUpdateBreakageCommentThenReturnOne() {

            BreakageComment savedBreakageComment = breakageCommentRepository.saveAndFlush(toSaveBreakageComment);
            LocalDateTime afterNow = now.plusMinutes(1);

            int response = breakageCommentRepository.updateBreakageComment(
                    savedBreakageComment.getId(),
                    BREAKAGE_COMMENT_UPDATE_TEST_TEXT,
                    defaultAdminUser.getId(),
                    afterNow
            );

            entityManager.clear();
            BreakageComment updatedBreakageComment =
                    breakageCommentRepository.findById(savedBreakageComment.getId()).get();

            assertThat(response).isEqualTo(1);
            assertThat(updatedBreakageComment.getComment()).isEqualTo(BREAKAGE_COMMENT_UPDATE_TEST_TEXT);
        }

        @Test
        @Transactional
        @Modifying(clearAutomatically = true)
        void whenUpdateBreakageCommentWithNotExistBreakageThenReturnZero() {

            int response = breakageCommentRepository.updateBreakageComment(
                    SOME_NOT_EXIST_ID,
                    BREAKAGE_COMMENT_UPDATE_TEST_TEXT,
                    defaultAdminUser.getId(),
                    now
            );

            assertThat(response).isEqualTo(0);
        }

        @Test
        @Transactional
        void whenGetAllBreakageCommentsThenReturnListOfComments() {

            breakageCommentRepository.saveAndFlush(toSaveBreakageComment);

            List<BreakageCommentBackendDto> list =
                    breakageCommentRepository.getAllBreakageComments(testBreakage.getId());

            assertThat(list.size()).isEqualTo(1);
        }

        @Test
        void whenGetAllBreakageCommentsThenReturnEmptyList() {

            List<BreakageCommentBackendDto> list =
                    breakageCommentRepository.getAllBreakageComments(testBreakage.getId());

            assertThat(list).isEmpty();
        }
    }
}