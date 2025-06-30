package ru.kraser.technical_helper.main_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.kraser.technical_helper.common_module.dto.user.UserDto;
import ru.kraser.technical_helper.common_module.enums.Role;
import ru.kraser.technical_helper.main_server.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findUserByUsername(String username);

    Optional<User> findUserByUsernameAndEnabledTrue(String username);

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
            value = """
                    SELECT new ru.kraser.technical_helper.common_module.dto.user.UserDto
                    (u.id, u.username, d.name AS department, u.role,
                    uc.username AS createdBy, u.createdDate, uu.username AS lastUpdatedBy, u.lastUpdatedDate)
                    FROM User AS u
                    JOIN FETCH Department AS d ON d.id = u.department.id
                    JOIN FETCH User AS uc ON uc.id = u.createdBy
                    JOIN FETCH User AS uu ON uu.id = u.lastUpdatedBy
                    WHERE u.enabled = true
                    ORDER BY u.username
                    """
    )
    List<UserDto> getAllUsers();

    Optional<User> findByIdAndEnabledTrue(String userId);

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
}
