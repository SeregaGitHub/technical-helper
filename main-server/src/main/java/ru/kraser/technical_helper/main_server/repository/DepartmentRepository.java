package ru.kraser.technical_helper.main_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.kraser.technical_helper.common_module.dto.department.DepartmentDto;
import ru.kraser.technical_helper.common_module.model.Department;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, String> {

    String GET_DEPARTMENT = "SELECT new ru.kraser.technical_helper.common_module.dto.department.DepartmentDto " +
            "(d.id, d.name, " +
            "uc.username AS createdBy, d.createdDate, uu.username AS lastUpdatedBy, d.lastUpdatedDate) " +
            "FROM Department as d " +
            "JOIN FETCH User as uc ON uc.id = d.createdBy " +
            "JOIN FETCH User as uu ON uu.id = d.lastUpdatedBy ";

    Optional<Department> findByName(String name);

    @Modifying
    @Query(
            value = """
                    UPDATE Department
                    SET
                    name = :departmentName,
                    lastUpdatedBy = :lastUpdatedBy,
                    lastUpdatedDate = :lastUpdatedDate
                    WHERE id = :departmentId
                    AND enabled = true
                    """
    )
    int updateDepartment(String departmentId,
                          String departmentName,
                          String lastUpdatedBy,
                          LocalDateTime lastUpdatedDate);

    @Query(
            value = GET_DEPARTMENT + "WHERE d.enabled = true ORDER BY name"
    )
    List<DepartmentDto> getAllDepartments();

//    @Query(
//            value = """
//                    SELECT new ru.kraser.technical_helper.common_module.dto.department.DepartmentDto
//                    (d.id, d.name, d.createdBy, d.createdDate, d.lastUpdatedBy, d.lastUpdatedDate)
//                    FROM Department as d
//                    WHERE d.enabled = true
//                    ORDER BY name
//                    """
//    )
//    List<DepartmentDto> getAllDepartments();

    @Query(
            value = GET_DEPARTMENT + "WHERE d.id = :departmentId AND d.enabled = true"
    )
    Optional<DepartmentDto> getDepartmentById(String departmentId);

//    Optional<Department> findByNameAndEnabledTrue(String departmentName);

    @Modifying
    @Query(
            value = """
                    UPDATE Department
                    SET
                    enabled = false,
                    lastUpdatedBy = :currentUserId,
                    lastUpdatedDate = :lastUpdatedDate
                    WHERE id = :departmentId
                    """
    )
    int deleteDepartment(String departmentId, String currentUserId, LocalDateTime lastUpdatedDate);
}
