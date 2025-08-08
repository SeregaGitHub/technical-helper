package ru.kraser.technical_helper.breakage_server.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.kraser.technical_helper.common_module.dto.breakage.EmployeeBreakageDto;
import ru.kraser.technical_helper.common_module.enums.Status;
import ru.kraser.technical_helper.common_module.model.Breakage;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BreakageRepository extends JpaRepository<Breakage, String> {

    String GET_ALL_BREAKAGES = "SELECT new ru.kraser.technical_helper.common_module.dto.breakage.EmployeeBreakageDto " +
            "(b.id, d.id, b.room, b.breakageTopic, b.breakageText, b.status, b.priority, " +
            "COALESCE (ue.username, 'Не назначен') AS executor, " +
            "COALESCE (ua.username, 'Отсутствует') AS executorAppointedBy, " +
            "uc.username AS createdBy, b.createdDate, uu.username AS lastUpdatedBy, b.lastUpdatedDate) " +
            "FROM Breakage as b " +
            "LEFT JOIN FETCH User as ue ON ue.id = b.executor.id " +
            "LEFT JOIN FETCH User as ua ON ua.id = b.executorAppointedBy.id " +
            "JOIN FETCH User as uc ON uc.id = b.createdBy " +
            "JOIN FETCH User as uu ON uu.id = b.lastUpdatedBy " +
            "JOIN FETCH Department as d ON d.id = b.department.id";

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
            value = GET_ALL_BREAKAGES + " WHERE d.id = :currentUserDepartmentId"
    )
    List<EmployeeBreakageDto> getAllEmployeeBreakages(String currentUserDepartmentId, PageRequest pageRequest);

    @Query(
            value = GET_ALL_BREAKAGES
    )
    List<EmployeeBreakageDto> getAllBreakages(PageRequest pageRequest);
}
