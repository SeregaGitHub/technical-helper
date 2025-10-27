package ru.kraser.technical_helper.main_server.util.mapper;

import lombok.experimental.UtilityClass;
import ru.kraser.technical_helper.common_module.dto.user.CreateUserDto;
import ru.kraser.technical_helper.common_module.dto.user.UserFullDto;
import ru.kraser.technical_helper.common_module.exception.NotFoundException;
import ru.kraser.technical_helper.common_module.model.Department;
import ru.kraser.technical_helper.common_module.model.User;

import java.time.LocalDateTime;

@UtilityClass
public class UserMapper {
    public User toUser(CreateUserDto createUserDto, Department department, String currentUserId) {
        if (!department.isEnabled()) {
            throw new NotFoundException("fk_users_department");
        }

        User user = new User();
        LocalDateTime now = LocalDateTime.now().withNano(0);

        user.setUsername(createUserDto.username());
        user.setPassword(createUserDto.password());
        user.setEnabled(true);
        user.setDepartment(department);
        user.setRole(createUserDto.role());
        user.setCreatedBy(currentUserId);
        user.setCreatedDate(now);
        user.setLastUpdatedBy(currentUserId);
        user.setLastUpdatedDate(now);

        return user;
    }

    public User toUserFull(UserFullDto userFullDto) {
        Department department = Department.builder()
                .id(userFullDto.departmentId())
                .name(userFullDto.departmentName())
                .enabled(userFullDto.departmentEnabled())
                .createdBy(userFullDto.departmentCreatedBy())
                .createdDate(userFullDto.departmentCreatedDate())
                .lastUpdatedBy(userFullDto.departmentLastUpdatedBy())
                .lastUpdatedDate(userFullDto.departmentLastUpdatedDate())
                .build();

        return User.builder()
                .id(userFullDto.id())
                .username(userFullDto.username())
                .password(userFullDto.password())
                .enabled(userFullDto.enabled())
                .department(department)
                .role(userFullDto.role())
                .createdBy(userFullDto.createdBy())
                .createdDate(userFullDto.createdDate())
                .lastUpdatedBy(userFullDto.lastUpdatedBy())
                .lastUpdatedDate(userFullDto.lastUpdatedDate())
                .build();
    }
}
