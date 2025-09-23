package ru.kraser.technical_helper.breakage_server.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.kraser.technical_helper.common_module.dto.breakage.BreakageDto;
import ru.kraser.technical_helper.common_module.dto.breakage.BreakageShortDto;
import ru.kraser.technical_helper.common_module.dto.breakage.BreakageTechDto;
import ru.kraser.technical_helper.common_module.enums.Priority;
import ru.kraser.technical_helper.common_module.enums.Status;
import ru.kraser.technical_helper.common_module.model.Breakage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BreakageRepository extends JpaRepository<Breakage, String> {

    String GET_EMPLOYEE_BREAKAGE = "SELECT new ru.kraser.technical_helper.common_module.dto.breakage.BreakageShortDto " +
            "(b.id, d.id AS departmentId, " +
            "d.name, b.room, b.breakageTopic, b.breakageText, b.status, " +
            "COALESCE (ue.username, 'Не назначен') AS breakageExecutor, " +
            "uc.username AS createdBy, b.createdDate) " +
            "FROM Breakage as b " +
            "LEFT JOIN FETCH User as ue ON ue.id = b.executor.id " +
            "JOIN FETCH User as uc ON uc.id = b.createdBy " +
            "JOIN FETCH Department as d ON d.id = b.department.id";

    String GET_ALL_EMPLOYEE_BREAKAGES = GET_EMPLOYEE_BREAKAGE + " WHERE status IN (?1) AND priority IN (?2)";

    String GET_BREAKAGE = "SELECT new ru.kraser.technical_helper.common_module.dto.breakage.BreakageTechDto " +
            "(b.id, d.id AS departmentId, " +
            "d.name, b.room, b.breakageTopic, b.breakageText, b.status, b.priority, " +
            "COALESCE (ue.username, 'Не назначен') AS breakageExecutor, " +
            "uc.username AS createdBy, b.createdDate) " +
            "FROM Breakage as b " +
            "LEFT JOIN FETCH User as ue ON ue.id = b.executor.id " +
            "JOIN FETCH User as uc ON uc.id = b.createdBy " +
            "JOIN FETCH Department as d ON d.id = b.department.id";

    String GET_FULL_BREAKAGE = "SELECT new ru.kraser.technical_helper.common_module.dto.breakage.BreakageDto " +
            "(b.id, d.id AS departmentId, COALESCE (ue.id, '') AS breakageExecutorId, " +
            "d.name, b.room, b.breakageTopic, b.breakageText, b.status, b.priority, " +
            "COALESCE (ue.username, 'Не назначен') AS breakageExecutor, " +
            "COALESCE (ua.username, 'Отсутствует') AS executorAppointedBy, " +
            "uc.username AS createdBy, b.createdDate, uu.username AS lastUpdatedBy, b.lastUpdatedDate, b.deadline) " +
            "FROM Breakage as b " +
            "LEFT JOIN FETCH User as ue ON ue.id = b.executor.id " +
            "LEFT JOIN FETCH User as ua ON ua.id = b.executorAppointedBy.id " +
            "JOIN FETCH User as uc ON uc.id = b.createdBy " +
            "JOIN FETCH User as uu ON uu.id = b.lastUpdatedBy " +
            "JOIN FETCH Department as d ON d.id = b.department.id";

    String GET_ALL_BREAKAGES = GET_BREAKAGE + " WHERE status IN (?1) AND priority IN (?2)";

    String GET_ALL_DEADLINE_EXPIRED_BREAKAGES = GET_ALL_BREAKAGES + " AND b.deadline < :now";


    @Modifying
    @Query(
            value = """
                    UPDATE Breakage
                    SET
                    status = :updatedStatus,
                    lastUpdatedBy = :lastUpdatedBy,
                    lastUpdatedDate = :lastUpdatedDate
                    WHERE id = :breakageId
                    """
    )
    int updateBreakageStatus(
            String breakageId, Status updatedStatus, String lastUpdatedBy, LocalDateTime lastUpdatedDate);

    @Modifying
    @Query(
            value = """
                    UPDATE Breakage
                    SET
                    status = :updatedStatus,
                    executor = null,
                    executorAppointedBy = null,
                    deadline = null,
                    lastUpdatedBy = :lastUpdatedBy,
                    lastUpdatedDate = :lastUpdatedDate
                    WHERE id = :breakageId
                    """
    )
    int updateBreakageStatusAndResetExecutor(
            String breakageId, Status updatedStatus, String lastUpdatedBy, LocalDateTime lastUpdatedDate);

    @Modifying
    @Query(
            value = """
                    UPDATE Breakage
                    SET
                    priority = :updatedPriority,
                    lastUpdatedBy = :lastUpdatedBy,
                    lastUpdatedDate = :lastUpdatedDate
                    WHERE id = :breakageId
                    """
    )
    int updateBreakagePriority(
            String breakageId, Priority updatedPriority, String lastUpdatedBy, LocalDateTime lastUpdatedDate);

    @Modifying
    @Query(
            value = """
                    UPDATE Breakage
                    SET
                    executor.id = :executor,
                    deadline = :deadline,
                    executorAppointedBy.id = :currentUserId,
                    lastUpdatedBy = :currentUserId,
                    lastUpdatedDate = :now
                    WHERE id = :breakageId
                    """
    )
    int addBreakageExecutor(
            String breakageId, String executor, LocalDateTime deadline, String currentUserId, LocalDateTime now);

    @Modifying
    @Query(
            value = """
                    UPDATE Breakage
                    SET
                    executor = null,
                    executorAppointedBy = null,
                    deadline = null,
                    lastUpdatedBy = :lastUpdatedBy,
                    lastUpdatedDate = :lastUpdatedDate
                    WHERE id = :breakageId
                    """
    )
    int dropBreakageExecutor(String breakageId, String lastUpdatedBy, LocalDateTime lastUpdatedDate);

    /*@Query(
            value = GET_ALL_BREAKAGES + " AND d.id = :currentUserDepartmentId"
    )
    Page<BreakageDto> getAllEmployeeBreakages(List<Status> statusList, List<Priority> priorityList,
                                              String currentUserDepartmentId, PageRequest pageRequest);*/
    @Query(
            value = GET_ALL_EMPLOYEE_BREAKAGES + " AND d.id = :currentUserDepartmentId"
    )
    Page<BreakageShortDto> getAllEmployeeBreakages(List<Status> statusList, List<Priority> priorityList,
                                                   String currentUserDepartmentId, PageRequest pageRequest);

    /*@Query(
            value = GET_ALL_BREAKAGES + " AND d.id = :currentUserDepartmentId AND b.breakageText ILIKE %:searchText%"
    )
    Page<BreakageDto> getAllEmployeeBreakagesByText(List<Status> statusList, List<Priority> priorityList,
                                              String currentUserDepartmentId, PageRequest pageRequest,
                                              String searchText);*/

    @Query(
            value = GET_ALL_EMPLOYEE_BREAKAGES + " AND d.id = :currentUserDepartmentId AND b.breakageText ILIKE %:searchText%"
    )
    Page<BreakageShortDto> getAllEmployeeBreakagesByText(List<Status> statusList, List<Priority> priorityList,
                                                         String currentUserDepartmentId, PageRequest pageRequest,
                                                         String searchText);

    @Query(
            value = GET_ALL_BREAKAGES + " AND b.executor.id = :currentUserId"
    )
    Page<BreakageTechDto> getAllBreakagesAppointedToMe(
            List<Status> statusList, List<Priority> priorityList, PageRequest pageRequest, String currentUserId);

    @Query(
            value = GET_ALL_BREAKAGES + " AND b.executor.id = :currentUserId AND b.breakageText ILIKE %:searchText%"
    )
    Page<BreakageTechDto> getAllBreakagesByTextAppointedToMe(
            List<Status> statusList, List<Priority> priorityList, PageRequest pageRequest,
            String currentUserId, String searchText);

    @Query(
            value = GET_ALL_DEADLINE_EXPIRED_BREAKAGES + " AND b.executor.id = :currentUserId"
    )
    Page<BreakageTechDto> getAllDeadlineExpiredBreakagesAppointedToMe(
            List<Status> statusList, List<Priority> priorityList,
            PageRequest pageRequest, String currentUserId, LocalDateTime now);

    @Query(
            value = GET_ALL_DEADLINE_EXPIRED_BREAKAGES + " AND b.executor.id = :currentUserId " +
                    "AND b.breakageText ILIKE %:searchText%"
    )
    Page<BreakageTechDto> getAllDeadlineExpiredBreakagesByTextAppointedToMe(
            List<Status> statusList, List<Priority> priorityList,
            PageRequest pageRequest, String currentUserId, LocalDateTime now, String searchText);

    @Query(
            value = GET_ALL_BREAKAGES + " AND b.executor.id <> :currentUserId"
    )
    Page<BreakageTechDto> getAllBreakagesAppointedToOthers(
            List<Status> statusList, List<Priority> priorityList, PageRequest pageRequest, String currentUserId);

    @Query(
            value = GET_ALL_BREAKAGES + " AND b.executor.id <> :currentUserId AND b.breakageText ILIKE %:searchText%"
    )
    Page<BreakageTechDto> getAllBreakagesByTextAppointedToOthers(
            List<Status> statusList, List<Priority> priorityList, PageRequest pageRequest,
            String currentUserId, String searchText);

    @Query(
            value = GET_ALL_DEADLINE_EXPIRED_BREAKAGES + " AND b.executor.id <> :currentUserId"
    )
    Page<BreakageTechDto> getAllDeadlineExpiredBreakagesAppointedToOthers(
            List<Status> statusList, List<Priority> priorityList,
            PageRequest pageRequest, String currentUserId, LocalDateTime now);

    @Query(
            value = GET_ALL_DEADLINE_EXPIRED_BREAKAGES + " AND b.executor.id <> :currentUserId " +
                    "AND b.breakageText ILIKE %:searchText%"
    )
    Page<BreakageTechDto> getAllDeadlineExpiredBreakagesByTextAppointedToOthers(
            List<Status> statusList, List<Priority> priorityList,
            PageRequest pageRequest, String currentUserId, LocalDateTime now, String searchText);

    @Query(
            value = GET_ALL_BREAKAGES + " AND b.executor.id IS NULL"
    )
    Page<BreakageTechDto> getAllBreakagesNoAppointed(
            List<Status> statusList, List<Priority> priorityList, PageRequest pageRequest);

    @Query(
            value = GET_ALL_BREAKAGES + " AND b.executor.id IS NULL AND b.breakageText ILIKE %:searchText%"
    )
    Page<BreakageTechDto> getAllBreakagesByTextNoAppointed(
            List<Status> statusList, List<Priority> priorityList, PageRequest pageRequest, String searchText);

    @Query(
            value = GET_ALL_BREAKAGES
    )
    Page<BreakageTechDto> getAllBreakages(List<Status> statusList, List<Priority> priorityList, PageRequest pageRequest);

    @Query(
            value = GET_ALL_BREAKAGES + " AND b.breakageText ILIKE %:searchText%"
    )
    Page<BreakageTechDto> getAllBreakagesByText(
            List<Status> statusList, List<Priority> priorityList, PageRequest pageRequest, String searchText);

    @Query(
            value = GET_ALL_DEADLINE_EXPIRED_BREAKAGES
    )
    Page<BreakageTechDto> getAllDeadlineExpiredBreakages(
            List<Status> statusList, List<Priority> priorityList, PageRequest pageRequest, LocalDateTime now);

    @Query(
            value = GET_ALL_DEADLINE_EXPIRED_BREAKAGES + " AND b.breakageText ILIKE %:searchText%"
    )
    Page<BreakageTechDto> getAllDeadlineExpiredBreakagesByText(
            List<Status> statusList, List<Priority> priorityList, PageRequest pageRequest,
            LocalDateTime now, String searchText);

    /*@Query(
            value = GET_BREAKAGE + " WHERE b.breakageText ILIKE %?1%"
    )
    Page<BreakageDto> getBreakagesByText(String text, PageRequest pageRequest);*/

    @Query(
            value = GET_BREAKAGE + " WHERE b.id = :breakageId"
    )
    Optional<BreakageDto> getBreakage(String breakageId);

    @Query(
            value = GET_EMPLOYEE_BREAKAGE + " WHERE b.id = :breakageId"
    )
    Optional<BreakageShortDto> getBreakageEmployee(String breakageId);
}
