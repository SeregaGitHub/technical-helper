package ru.kraser.technical_helper.common_module.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
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
                      @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                      LocalDateTime createdDate,
                      @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                      String lastUpdatedBy,
                      LocalDateTime lastUpdatedDate) {
}
