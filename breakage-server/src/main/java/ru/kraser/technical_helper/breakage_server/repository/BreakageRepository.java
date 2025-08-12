package ru.kraser.technical_helper.breakage_server.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.kraser.technical_helper.common_module.dto.breakage.BreakageDto;
import ru.kraser.technical_helper.common_module.dto.breakage.BreakageFullDto;
import ru.kraser.technical_helper.common_module.enums.Priority;
import ru.kraser.technical_helper.common_module.enums.Status;
import ru.kraser.technical_helper.common_module.model.Breakage;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BreakageRepository extends JpaRepository<Breakage, String> {

    String GET_BREAKAGE = "SELECT new ru.kraser.technical_helper.common_module.dto.breakage.BreakageDto " +
            "(b.id, d.id, d.name, b.room, b.breakageTopic, b.breakageText, b.status, b.priority, " +
            "COALESCE (ue.username, 'Не назначен') AS executor, " +
            "COALESCE (ua.username, 'Отсутствует') AS executorAppointedBy, " +
            "uc.username AS createdBy, b.createdDate, uu.username AS lastUpdatedBy, b.lastUpdatedDate) " +
            "FROM Breakage as b " +
            "LEFT JOIN FETCH User as ue ON ue.id = b.executor.id " +
            "LEFT JOIN FETCH User as ua ON ua.id = b.executorAppointedBy.id " +
            "JOIN FETCH User as uc ON uc.id = b.createdBy " +
            "JOIN FETCH User as uu ON uu.id = b.lastUpdatedBy " +
            "JOIN FETCH Department as d ON d.id = b.department.id";

    String GET_ALL_BREAKAGES = GET_BREAKAGE + " WHERE status IN (?1) AND priority IN (?2)";

    @Modifying
    @Query(
            value = """
                    UPDATE Breakage
                    SET
                    status = :status,
                    lastUpdatedBy = :lastUpdatedBy,
                    lastUpdatedDate = :lastUpdatedDate
                    WHERE id = :breakageId
                    """
    )
    int cancelBreakage(String breakageId, Status status, String lastUpdatedBy, LocalDateTime lastUpdatedDate);

    @Query(
            value = GET_ALL_BREAKAGES + " AND d.id = :currentUserDepartmentId"
    )
    Page<BreakageDto> getAllEmployeeBreakages(List<Status> statusList, List<Priority> priorityList,
                                              String currentUserDepartmentId, PageRequest pageRequest);

    @Query(
            value = GET_ALL_BREAKAGES + " AND b.executor.id = :currentUserId"
    )
    Page<BreakageDto> getAllBreakagesAppointedToMe(
            List<Status> statusList, List<Priority> priorityList, PageRequest pageRequest, String currentUserId);

    @Query(
            value = GET_ALL_BREAKAGES + " AND b.executor.id <> :currentUserId"
    )
    Page<BreakageDto> getAllBreakagesAppointedToOthers(
            List<Status> statusList, List<Priority> priorityList, PageRequest pageRequest, String currentUserId);

    @Query(
            value = GET_ALL_BREAKAGES + " AND b.executor.id IS NULL"
    )
    Page<BreakageDto> getAllBreakagesNoAppointed(
            List<Status> statusList, List<Priority> priorityList, PageRequest pageRequest);

    @Query(
            value = GET_ALL_BREAKAGES
    )
    Page<BreakageDto> getAllBreakages(List<Status> statusList, List<Priority> priorityList, PageRequest pageRequest);

    @Query(
            value = GET_BREAKAGE + " WHERE b.id = :breakageId"
    )
    BreakageDto getBreakage(String breakageId);

}
