package ru.kraser.technical_helper.main_server.service;

import ru.kraser.technical_helper.common_module.dto.department.CreateDepartmentDto;
import ru.kraser.technical_helper.main_server.model.Department;

public interface DepartmentService {

    String createDepartment(CreateDepartmentDto createDepartmentDto);

    String deleteDepartment(String departmentId);

    //Department getDepartment(String departmentId);
}
