package ru.kraser.technical_helper.common_module.dto.user;

import lombok.Builder;
import ru.kraser.technical_helper.common_module.enums.Role;

import java.time.LocalDateTime;

@Builder
public record NewUser(String id,
                      String username,
                      String password,
                      String departmentId,
                      Role role,
                      boolean enabled,
                      String createdBy,
                      LocalDateTime createdDate,
                      String lastUpdatedBy,
                      LocalDateTime lastUpdatedDate
                      ) {
}
