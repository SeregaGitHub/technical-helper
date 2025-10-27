package ru.kraser.technical_helper.main_server.util.mapper;

import lombok.experimental.UtilityClass;
import ru.kraser.technical_helper.common_module.dto.department.CreateDepartmentDto;
import ru.kraser.technical_helper.common_module.model.Department;

import java.time.LocalDateTime;

@UtilityClass
public class DepartmentMapper {
    public Department toDepartment(CreateDepartmentDto createDepartmentDto, String currentUserId) {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        Department department = new Department();

        department.setName(createDepartmentDto.name());
        department.setEnabled(true);
        department.setCreatedBy(currentUserId);
        department.setCreatedDate(now);
        department.setLastUpdatedBy(currentUserId);
        department.setLastUpdatedDate(now);
        return department;
    }
}
