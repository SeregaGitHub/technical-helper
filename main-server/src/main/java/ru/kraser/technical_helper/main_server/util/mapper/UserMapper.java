package ru.kraser.technical_helper.main_server.util.mapper;

import lombok.experimental.UtilityClass;
import ru.kraser.technical_helper.common_module.dto.user.CreateUserDto;
import ru.kraser.technical_helper.common_module.dto.user.UserDto;
import ru.kraser.technical_helper.common_module.exception.NotFoundException;
import ru.kraser.technical_helper.main_server.model.Department;
import ru.kraser.technical_helper.main_server.model.User;

import java.time.LocalDateTime;

@UtilityClass
public class UserMapper {
    public User toUser(CreateUserDto createUserDto, Department department) {
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
        // TODO - change to the current user
        user.setCreatedBy("some_id");
        user.setCreatedDate(now);
        // TODO - change to the current user
        user.setLastUpdatedBy("some_id");
        user.setLastUpdatedDate(now);

        return user;
    }

    public UserDto toUserDto (User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .departmentDto(DepartmentMapper.toDepartmentShotDto(user.getDepartment()))
                .role(user.getRole())
                .createdBy(user.getCreatedBy())
                .createdDate(user.getCreatedDate())
                .lastUpdatedBy(user.getLastUpdatedBy())
                .lastUpdatedDate(user.getLastUpdatedDate())
                .build();
    }
}
