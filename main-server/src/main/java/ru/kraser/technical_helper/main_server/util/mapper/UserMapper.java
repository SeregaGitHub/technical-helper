package ru.kraser.technical_helper.main_server.util.mapper;

import lombok.experimental.UtilityClass;
import ru.kraser.technical_helper.common_module.dto.user.CreateUserDto;
import ru.kraser.technical_helper.common_module.dto.user.NewUser;
import ru.kraser.technical_helper.common_module.dto.user.UserDto;
import ru.kraser.technical_helper.main_server.model.User;

import java.time.LocalDateTime;
import java.util.UUID;

@UtilityClass
public class UserMapper {
    public NewUser toNewUser(CreateUserDto createUserDto) {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        UUID uuid = UUID.randomUUID();

        return NewUser.builder()
                .id(uuid.toString())
                .username(createUserDto.username())
                .password(createUserDto.password())
                .departmentId(createUserDto.departmentId())
                .role(createUserDto.role())
                .enabled(true)
                // TODO - change to the current user
                .createdBy("some_id")
                .createdDate(now)
                // TODO - change to the current user
                .lastUpdatedBy("some_id")
                .lastUpdatedDate(now)
                .build();
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
