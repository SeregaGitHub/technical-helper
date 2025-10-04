package ru.kraser.technical_helper.main_server.util.mapper;

import lombok.experimental.UtilityClass;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.kraser.technical_helper.common_module.dto.user.CreateUserDto;
import ru.kraser.technical_helper.common_module.exception.NotFoundException;
import ru.kraser.technical_helper.common_module.model.Department;
import ru.kraser.technical_helper.common_module.model.User;
import ru.kraser.technical_helper.common_module.util.SecurityUtil;

import java.time.LocalDateTime;

@UtilityClass
public class UserMapper {
    public User toUser(CreateUserDto createUserDto, Department department, PasswordEncoder passwordEncoder) {
        if (!department.isEnabled()) {
            throw new NotFoundException("fk_users_department");
        }

        User user = new User();
        LocalDateTime now = LocalDateTime.now().withNano(0);
        String currentUserId = SecurityUtil.getCurrentUserId();

        user.setUsername(createUserDto.username());
        user.setPassword(passwordEncoder.encode(createUserDto.password()));
        user.setEnabled(true);
        user.setDepartment(department);
        user.setRole(createUserDto.role());
        user.setCreatedBy(currentUserId);
        user.setCreatedDate(now);
        user.setLastUpdatedBy(currentUserId);
        user.setLastUpdatedDate(now);

        return user;
    }

    /*public UserDto toUserDto (User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .departmentId(user.getDepartment().getId())
                .department(user.getDepartment().getName())
                .role(user.getRole())
                .createdBy(user.getCreatedBy())
                .createdDate(user.getCreatedDate())
                .lastUpdatedBy(user.getLastUpdatedBy())
                .lastUpdatedDate(user.getLastUpdatedDate())
                .build();
    }*/
}
