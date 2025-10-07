package ru.kraser.technical_helper.main_server.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@RequiredArgsConstructor
public class DepartmentController {
    private final DepartmentService departmentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse createDepartment(@RequestHeader (CURRENT_USER_ID_HEADER) String currentUserId,
                                        @RequestBody CreateDepartmentDto createDepartmentDto) {
        log.info("Creating Department with name - {}", createDepartmentDto.name());
        ApiResponse apiResponse = departmentService.createDepartment(createDepartmentDto, currentUserId);
        log.info("Department with name - {}, successfully created", createDepartmentDto.name());
        return apiResponse;
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse updateDepartment(@RequestHeader (DEPARTMENT_ID_HEADER) String departmentId,
                                        @RequestHeader (CURRENT_USER_ID_HEADER) String currentUserId,
                                        @RequestBody CreateDepartmentDto departmentDto) {
        log.info("Updating Department with Id={}", departmentId);
        ApiResponse apiResponse =  departmentService.updateDepartment(departmentId, departmentDto, currentUserId);
        log.info("Department with Id={}, successfully updated", departmentId);
        return apiResponse;
    }

    @GetMapping(path = ALL_URL)
    @ResponseStatus(HttpStatus.OK)
    public List<DepartmentDto> getAllDepartments() {
        log.info("Getting all Departments");
        List<DepartmentDto> departments =  departmentService.getAllDepartments();
        log.info("All Departments received successfully");
        return departments;
    }

    @GetMapping(path = CURRENT_URL)
    @ResponseStatus(HttpStatus.OK)
    public DepartmentDto getDepartmentById(@RequestHeader (DEPARTMENT_ID_HEADER) String departmentId) {
        log.info("Getting Department with Id={}", departmentId);
        DepartmentDto department =  departmentService.getDepartment(DEPARTMENT_ID_HEADER, departmentId);
        log.info("Department with Id={}, received successfully", departmentId);
        return department;
    }

/*    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public DepartmentDto getDepartmentByName(@RequestHeader (DEPARTMENT_NAME_HEADER) String departmentName) {
        log.info("Getting Department with name - {}", departmentName);
        DepartmentDto department =  departmentService.getDepartment(DEPARTMENT_NAME_HEADER, departmentName);
        log.info("Department with name - {}, received successfully", departmentName);
        return department;
    }*/

    @PatchMapping(path = DELETE_URL)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse deleteDepartment(@RequestHeader (CURRENT_USER_ID_HEADER) String currentUserId,
                                        @RequestHeader (DEPARTMENT_ID_HEADER) String departmentId) {
        log.info("Deleting Department with Id={}", departmentId);
        ApiResponse apiResponse =  departmentService.deleteDepartment(departmentId, currentUserId);
        log.info("Department with Id={}, successfully deleted", departmentId);
        return apiResponse;
    }
}
