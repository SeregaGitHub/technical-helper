package ru.kraser.technical_helper.main_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.kraser.technical_helper.common_module.dto.department.DepartmentDto;
import ru.kraser.technical_helper.main_server.model.Department;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, String> {

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
            value = """
                    SELECT new ru.kraser.technical_helper.common_module.dto.department.DepartmentDto
                    (d.id, d.name, d.createdBy, d.createdDate, d.lastUpdatedBy, d.lastUpdatedDate)
                    FROM Department as d
                    WHERE d.enabled = true
                    ORDER BY name
                    """
    )
    List<DepartmentDto> getAllDepartments();

    Optional<Department> findByIdAndEnabledTrue(String departmentId);

    @Modifying
    @Query(
            value = """
                    UPDATE Department
                    SET enabled = false
                    WHERE id = :departmentId
                    """
    )
    int deleteDepartment(String departmentId);
}
