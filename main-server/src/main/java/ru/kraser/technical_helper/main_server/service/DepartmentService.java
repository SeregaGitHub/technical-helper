package ru.kraser.technical_helper.main_server.service;

import ru.kraser.technical_helper.common_module.dto.department.CreateDepartmentDto;
import ru.kraser.technical_helper.common_module.dto.department.DepartmentDto;

import java.util.List;

public interface DepartmentService {

    String createDepartment(CreateDepartmentDto createDepartmentDto);

    String updateDepartment(String departmentId, CreateDepartmentDto departmentDto);

    List<DepartmentDto> getAllDepartments();

    String deleteDepartment(String departmentId);

    //Department getDepartment(String departmentId);
}
