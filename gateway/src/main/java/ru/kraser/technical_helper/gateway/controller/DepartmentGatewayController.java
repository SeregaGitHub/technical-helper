package ru.kraser.technical_helper.gateway.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.kraser.technical_helper.common_module.dto.api.ApiResponse;
import ru.kraser.technical_helper.common_module.dto.department.CreateDepartmentDto;
import ru.kraser.technical_helper.common_module.dto.department.DepartmentDto;
import ru.kraser.technical_helper.gateway.client.DepartmentClient;

import java.util.List;

import static ru.kraser.technical_helper.common_module.util.Constant.*;

@RestController
@RequestMapping(path = BASE_URL + ADMIN_URL + DEPARTMENT_URL)
@RequiredArgsConstructor
@Slf4j
@Validated
public class DepartmentGatewayController {
    private final DepartmentClient departmentClient;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse createDepartment(@Validated() @RequestBody CreateDepartmentDto createDepartmentDto) {
        log.info("Creating Department with name - {}", createDepartmentDto.name());
        ApiResponse response = departmentClient.createDepartment(createDepartmentDto);
        log.info("Department with name - {}, successfully created", createDepartmentDto.name());
        return response;
    }

    @PatchMapping()
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse updateDepartment(@Validated() @RequestBody CreateDepartmentDto createDepartmentDto,
                                        @RequestHeader(DEPARTMENT_ID_HEADER) String departmentId) {
        log.info("Updating Department with Id={}", departmentId);
        ApiResponse response = departmentClient.updateDepartment(
                DEPARTMENT_ID_HEADER, departmentId, createDepartmentDto);
        log.info("Department with Id={}, successfully updated", departmentId);
        return response;
    }

    @GetMapping(path = ALL_URL)
    @ResponseStatus(HttpStatus.OK)
    public List<DepartmentDto> getAllDepartments() {
        log.info("Getting all Departments");
        List<DepartmentDto> departmentDtoList = departmentClient.getAllDepartments();
        log.info("All Departments received successfully");
        return departmentDtoList;
    }

    @GetMapping(path = CURRENT_URL)
    @ResponseStatus(HttpStatus.OK)
    public DepartmentDto getDepartmentById(@RequestHeader(DEPARTMENT_ID_HEADER) String departmentId) {
        log.info("Getting Department with Id={}", departmentId);
        DepartmentDto departmentDto = departmentClient.getDepartmentById(departmentId, DEPARTMENT_ID_HEADER);
        log.info("Department with Id={}, received successfully", departmentId);
        return departmentDto;
    }

    @PatchMapping(path = DELETE_URL)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse deleteDepartment(@RequestHeader(DEPARTMENT_ID_HEADER) String departmentId) {
        log.info("Deleting Department with Id={}", departmentId);
        ApiResponse response = departmentClient.deleteDepartment(DEPARTMENT_ID_HEADER, departmentId);
        log.info("Department with Id={}, successfully deleted", departmentId);
        return response;
    }
}
