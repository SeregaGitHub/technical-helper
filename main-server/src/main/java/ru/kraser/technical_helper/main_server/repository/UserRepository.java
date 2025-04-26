package ru.kraser.technical_helper.main_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.kraser.technical_helper.main_server.model.User;

import java.time.LocalDateTime;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    @Modifying
    @Query(
            value = """
                        INSERT INTO users(id, username, password, enabled, department, role,
                                          created_by, created_date, last_updated_by, last_updated_date)
                        VALUES
                        (
                         :id, :username, :password, :enabled, :departmentId, :role,
                         :createdBy, :createdDate, :lastUpdatedBy, :lastUpdatedDate
                        );
                        """, nativeQuery = true
    )
    void createUser(String id, String username, String password, boolean enabled, String departmentId, String role,
                    String createdBy, LocalDateTime createdDate, String lastUpdatedBy, LocalDateTime lastUpdatedDate);
}
