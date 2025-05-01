package ru.kraser.technical_helper.main_server.repository;

import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.kraser.technical_helper.common_module.dto.department.CreateDepartmentDto;
import ru.kraser.technical_helper.main_server.model.Department;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, String> {
    /*@Query(
            value = "select new ru.th.technical_helper.common_module.dto.department.DepartmentDto" +
                    "(d.id, d.name) " +
                    "from Department as d " +
                    "where d.id = ?1 and d.enabled = true"
    )*/
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
            /*value = """
                        UPDATE department
                        SET
                        name = ?2,
                        last_updated_by = ?3,
                        last_updated_date = ?4
                        WHERE id = ?1
                        AND enabled = true
                        RETURNING 1 AS answer
                        """, nativeQuery = true*/
    )
    void updateDepartment(String departmentId,
                                        String departmentName,
                                        String lastUpdatedBy,
                                        LocalDateTime lastUpdatedDate);

    Optional<Department> findByIdAndEnabledTrue(String departmentId);

    @Modifying
    @Query(
            value = """
                        UPDATE Department
                        SET enabled = false
                        WHERE id = :departmentId
                        """
    )
    void deleteDepartment(String departmentId);

    @Query(
            value = """
                        SELECT * FROM department
                        WHERE id = ?1
                        AND enabled = true
                        FOR SHARE;
                        """, nativeQuery = true
    )
    Optional<Department> getDepartmentForUserService(String departmentId);
}
