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

    Optional<User> findTop1ByRoleAndEnabledTrue(Role role);

    @Modifying
    @Query(
            value = """
                    UPDATE User
                    SET
                    username = :username,
                    department = :departmentId,
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
                    password = :newPassword
                    WHERE id = :userId
                    AND enabled = true
                    """
    )
    int changeUserPassword(String userId, String newPassword);

    @Query(
            value = """
                    SELECT new ru.kraser.technical_helper.common_module.dto.user.UserDto
                    (u.id, u.username, d.name, u.role, u.createdBy, u.createdDate, u.lastUpdatedBy, u.lastUpdatedDate)
                    FROM User AS u
                    JOIN FETCH Department AS d ON d.id = u.department.id
                    WHERE u.enabled = true
                    ORDER BY name
                    """
    )
    List<UserDto> getAllUsers();

    Optional<User> findByIdAndEnabledTrue(String userId);

    @Modifying
    @Query(
            value = """
                    UPDATE User
                    SET enabled = false
                    WHERE id = :userId
                    """
    )
    int deleteUser(String userId);
}
