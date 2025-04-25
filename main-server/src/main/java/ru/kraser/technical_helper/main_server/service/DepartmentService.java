package ru.kraser.technical_helper.main_server.service;

import ru.kraser.technical_helper.common_module.dto.department.CreateDepartmentDto;
import ru.kraser.technical_helper.common_module.dto.department.DepartmentDto;

public interface DepartmentService {

    DepartmentDto createDepartment(CreateDepartmentDto createDepartmentDto);
}
