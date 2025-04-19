package ru.kraser.technical_helper.main_server.util.mapper;

import lombok.experimental.UtilityClass;
import ru.kraser.technical_helper.common_module.dto.user.CreateUserDto;
import ru.kraser.technical_helper.common_module.dto.user.UserDto;
import ru.kraser.technical_helper.main_server.model.Department;
import ru.kraser.technical_helper.main_server.model.User;

import java.time.LocalDateTime;

@UtilityClass
public class UserMapper {
    public User toUser(CreateUserDto createUserDto, Department department) {
        LocalDateTime localDateTime = LocalDateTime.now().withNano(0);
        User user = new User();

        user.setUsername(createUserDto.username());
        user.setPassword(createUserDto.password());
        user.setDepartment(department);
        user.setRole(createUserDto.role());
        // TODO - change to the current user
        user.setCreatedBy("some_id");
        user.setCreatedDate(localDateTime);
        // TODO - change to the current user
        user.setLastUpdatedBy("some_id");
        user.setLastUpdatedDate(localDateTime);
        return user;
    }

    public UserDto toUserDto (User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .departmentDto(DepartmentMapper.toDepartmentDto(user.getDepartment()))
                .role(user.getRole())
                .createdBy(user.getCreatedBy())
                .createdDate(user.getCreatedDate())
                .lastUpdatedBy(user.getLastUpdatedBy())
                .lastUpdatedDate(user.getLastUpdatedDate())
                .build();
    }
}
