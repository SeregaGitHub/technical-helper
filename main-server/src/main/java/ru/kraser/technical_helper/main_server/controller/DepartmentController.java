package ru.kraser.technical_helper.main_server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.kraser.technical_helper.common_module.dto.api.ApiResponse;
import ru.kraser.technical_helper.common_module.dto.department.CreateDepartmentDto;
import ru.kraser.technical_helper.common_module.dto.department.DepartmentDto;
import ru.kraser.technical_helper.main_server.service.DepartmentService;

import java.util.List;

import static ru.kraser.technical_helper.common_module.util.Constant.*;

@RestController
@RequestMapping(path = BASE_URL + ADMIN_URL + DEPARTMENT_URL)
@RequiredArgsConstructor
public class DepartmentController {
    private final DepartmentService departmentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse createDepartment(@RequestBody CreateDepartmentDto createDepartmentDto) {
        return departmentService.createDepartment(createDepartmentDto);
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.OK)
    public String updateDepartment(@RequestHeader (DEPARTMENT_ID_HEADER) String departmentId,
                                   @RequestBody CreateDepartmentDto departmentDto) {
        return departmentService.updateDepartment(departmentId, departmentDto);
    }

    @GetMapping(path = ALL_URL)
    @ResponseStatus(HttpStatus.OK)
    public List<DepartmentDto> getAllDepartments() {
        return departmentService.getAllDepartments();
    }

    @GetMapping(path = CURRENT_URL)
    @ResponseStatus(HttpStatus.OK)
    public DepartmentDto getDepartment(@RequestHeader (DEPARTMENT_ID_HEADER) String departmentId) {
        return departmentService.getDepartment(departmentId);
    }

    @PatchMapping(path = DELETE_URL)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse deleteDepartment(@RequestHeader (DEPARTMENT_ID_HEADER) String departmentId) {
        return departmentService.deleteDepartment(departmentId);
    }
}
