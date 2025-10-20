package ru.kraser.technical_helper.main_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.kraser.technical_helper.common_module.dto.user.UserDto;
import ru.kraser.technical_helper.common_module.dto.user.UserFullDto;
import ru.kraser.technical_helper.common_module.dto.user.UserShortDto;
import ru.kraser.technical_helper.common_module.enums.Role;
import ru.kraser.technical_helper.common_module.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    String GET_USER = "SELECT new ru.kraser.technical_helper.common_module.dto.user.UserDto " +
            "(u.id, u.username, d.id AS departmentId, d.name AS department, u.role, " +
            "uc.username AS createdBy, u.createdDate, uu.username AS lastUpdatedBy, u.lastUpdatedDate) " +
            "FROM User AS u " +
            "JOIN FETCH Department AS d ON d.id = u.department.id " +
            "JOIN FETCH User AS uc ON uc.id = u.createdBy " +
            "JOIN FETCH User AS uu ON uu.id = u.lastUpdatedBy ";

    Optional<User> findUserByUsername(String username);

    //Optional<User> findUserByUsernameAndEnabledTrue(String username);

    @Query(
            value = """
                    SELECT new ru.kraser.technical_helper.common_module.dto.user.UserFullDto
                    (u.id, u.username, u.password, u.enabled, u.role,
                    u.createdBy, u.createdDate, u.lastUpdatedBy, u.lastUpdatedDate,
                    d.id AS departmentId, d.name AS departmentName, d.enabled AS departmentEnabled,
                    d.createdBy AS departmentCreatedBy, d.createdDate AS departmentCreatedDate,
                    d.lastUpdatedBy AS departmentLastUpdatedBy, d.lastUpdatedDate AS departmentLastUpdatedDate)
                    FROM User AS u
                    JOIN FETCH Department AS d ON d.id = u.department.id
                    WHERE u.username = :username AND u.enabled = true
                    """
    )
    Optional<UserFullDto> getUserByUsernameAndEnabledTrue(String username);

    Optional<User> findTop1ByRoleAndEnabledTrue(Role role);

    @Modifying
    @Query(
            value = """
                    UPDATE User
                    SET
                    username = :username,
                    department.id = :departmentId,
                    role = :role,
                    lastUpdatedBy = :lastUpdatedBy,
                    lastUpdatedDate = :lastUpdatedDate
                    WHERE id = :userId
                    AND enabled = true
                    """
    )
    int updateUser(String userId,
                    String username,
                    String departmentId,
                    Role role,
                    String lastUpdatedBy,
                    LocalDateTime lastUpdatedDate);

    @Modifying
    @Query(
            value = """
                    UPDATE User
                    SET
                    password = :newPassword,
                    lastUpdatedBy = :currentUserId,
                    lastUpdatedDate = :lastUpdatedDate
                    WHERE id = :userId
                    AND enabled = true
                    """
    )
    int changeUserPassword(String userId, String newPassword, String currentUserId, LocalDateTime lastUpdatedDate);

    @Query(
            value = GET_USER + "WHERE u.enabled = true ORDER BY u.role, d.name, u.username"
    )
    List<UserDto> getAllUsers();

    @Query(
            value = """
                    SELECT new ru.kraser.technical_helper.common_module.dto.user.UserShortDto
                    (u.id, u.username)
                    FROM User AS u
                    WHERE u.enabled = true AND (u.role = 'TECHNICIAN' OR u.role = 'ADMIN')
                    ORDER BY u.role, u.username
                    """
    )
    List<UserShortDto> getAdminAndTechnicianList();

    @Query(
            value = GET_USER + "WHERE u.id = :userId AND u.enabled = true"
    )
    Optional<UserDto> getUserById(String userId);

    @Modifying
    @Query(
            value = """
                    UPDATE User
                    SET
                    enabled = false,
                    lastUpdatedBy = :currentUserId,
                    lastUpdatedDate = :lastUpdatedDate
                    WHERE id = :userId
                    """
    )
    int deleteUser(String userId, String currentUserId, LocalDateTime lastUpdatedDate);

    @Procedure(procedureName = "drop_constraints")
    void dropConstraints();

    @Procedure(procedureName = "set_current_id")
    void setCurrentId(@Param("dep_id") String adminDepartmentId, @Param("adm_id") String adminId,
                      @Param("dep_created_by") String adminDepartmentCreatedBy,
                      @Param("adm_created_by") String adminCreatedBy,
                      @Param("dep_last_updated_by") String adminDepartmentLastUpdatedBy,
                      @Param("adm_last_updated_by") String adminLastUpdatedBy,
                      @Param("temporary_admin_id") String temporaryAdminId);
}
