package ru.kraser.technical_helper.main_server.service;

import ru.kraser.technical_helper.common_module.dto.api.ApiResponse;
import ru.kraser.technical_helper.common_module.dto.department.CreateDepartmentDto;
import ru.kraser.technical_helper.common_module.dto.department.DepartmentDto;

import java.util.List;

public interface DepartmentService {

    ApiResponse createDepartment(CreateDepartmentDto createDepartmentDto, String currentUserId);

    ApiResponse updateDepartment(String departmentId, CreateDepartmentDto departmentDto, String currentUserId);

    List<DepartmentDto> getAllDepartments();

    DepartmentDto getDepartment(String headerName, String departmentName);

    ApiResponse deleteDepartment(String departmentId);
}
