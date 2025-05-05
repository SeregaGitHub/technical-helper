package ru.kraser.technical_helper.main_server.util.mapper;

import lombok.experimental.UtilityClass;
import ru.kraser.technical_helper.common_module.dto.department.CreateDepartmentDto;
import ru.kraser.technical_helper.common_module.dto.department.DepartmentDto;
import ru.kraser.technical_helper.main_server.model.Department;

import java.time.LocalDateTime;

@UtilityClass
public class DepartmentMapper {
    public Department toDepartment(CreateDepartmentDto createDepartmentDto) {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        Department department = new Department();

        department.setName(createDepartmentDto.name());
        department.setEnabled(true);
        // TODO - change to the current user
        department.setCreatedBy("some_id");
        department.setCreatedDate(now);
        // TODO - change to the current user
        department.setLastUpdatedBy("some_id");
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
