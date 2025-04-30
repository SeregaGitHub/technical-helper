package ru.kraser.technical_helper.main_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.kraser.technical_helper.main_server.model.Department;

import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, String> {
    /*@Query(
            value = "select new ru.th.technical_helper.common_module.dto.department.DepartmentDto" +
                    "(d.id, d.name) " +
                    "from Department as d " +
                    "where d.id = ?1 and d.enabled = true"
    )*/
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
