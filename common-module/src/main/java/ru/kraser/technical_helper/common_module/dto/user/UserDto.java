package ru.kraser.technical_helper.common_module.dto.user;

import lombok.Builder;
import ru.kraser.technical_helper.common_module.dto.department.DepartmentDto;
import ru.kraser.technical_helper.common_module.enums.Role;

import java.time.LocalDateTime;

@Builder
public record UserDto(String id,
                      String username,
                      DepartmentDto departmentDto,
                      Role role,
                      String createdBy,
                      LocalDateTime createdDate,
                      String lastUpdatedBy,
                      LocalDateTime lastUpdatedDate) {
}
