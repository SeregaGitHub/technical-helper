package ru.kraser.technical_helper.main_server.util.mapper;

import lombok.experimental.UtilityClass;
import ru.kraser.technical_helper.common_module.dto.department.CreateDepartmentDto;
import ru.kraser.technical_helper.common_module.dto.department.DepartmentDto;
import ru.kraser.technical_helper.main_server.model.Department;
import ru.kraser.technical_helper.main_server.security.SecurityUtil;

import java.time.LocalDateTime;

@UtilityClass
public class DepartmentMapper {
    public Department toDepartment(CreateDepartmentDto createDepartmentDto) {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        String currentUserId = SecurityUtil.getCurrentUserId();
        Department department = new Department();

        department.setName(createDepartmentDto.name());
        department.setEnabled(true);
        department.setCreatedBy(currentUserId);
        department.setCreatedDate(now);
        department.setLastUpdatedBy(currentUserId);
        department.setLastUpdatedDate(now);
        return department;
    }

    public DepartmentDto toDepartmentDto(Department department) {
        return DepartmentDto.builder()
                .id(department.getId())
                .name(department.getName())
                .createdBy(department.getCreatedBy())
                .createdDate(department.getCreatedDate())
                .lastUpdatedBy(department.getLastUpdatedBy())
                .lastUpdatedDate(department.getLastUpdatedDate())
                .build();
    }
}
