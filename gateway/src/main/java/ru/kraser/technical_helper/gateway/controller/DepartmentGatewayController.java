package ru.kraser.technical_helper.gateway.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.kraser.technical_helper.common_module.dto.api.ApiResponse;
import ru.kraser.technical_helper.common_module.dto.department.CreateDepartmentDto;
import ru.kraser.technical_helper.common_module.dto.department.DepartmentDto;
import ru.kraser.technical_helper.gateway.client.DepartmentClient;

import java.util.List;

import static ru.kraser.technical_helper.common_module.util.Constant.*;

@CrossOrigin(origins = FRONT_URL)
@RestController
@RequestMapping(path = BASE_URL + ADMIN_URL + DEPARTMENT_URL)
@RequiredArgsConstructor
@Validated
public class DepartmentGatewayController {
    private final DepartmentClient departmentClient;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse createDepartment(@Validated() @RequestBody CreateDepartmentDto createDepartmentDto,
                                          @RequestHeader(AUTH_HEADER) String jwt) {
        ApiResponse response = departmentClient.createDepartment(createDepartmentDto, jwt);

        return response;
    }

    @PatchMapping()
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse updateDepartment(@Validated() @RequestBody CreateDepartmentDto createDepartmentDto,
                                   @RequestHeader(AUTH_HEADER) String jwt,
                                   @RequestHeader(DEPARTMENT_ID_HEADER) String departmentId) {
        ApiResponse response = departmentClient.updateDepartment(DEPARTMENT_ID_HEADER, departmentId, createDepartmentDto, jwt);

        return response;
    }

    @GetMapping(path = ALL_URL)
    @ResponseStatus(HttpStatus.OK)
    public List<DepartmentDto> getAllDepartments(@RequestHeader(AUTH_HEADER) String jwt) {
        List<DepartmentDto> departmentDtoList = departmentClient.getAllDepartments(jwt);

        return departmentDtoList;
    }

    @GetMapping(path = CURRENT_URL)
    @ResponseStatus(HttpStatus.OK)
    public DepartmentDto getDepartment(@RequestHeader(AUTH_HEADER) String jwt,
                                       @RequestHeader(DEPARTMENT_NAME_HEADER) String departmentName) {
        DepartmentDto departmentDto = departmentClient.getDepartment(departmentName, jwt, DEPARTMENT_NAME_HEADER);

        return departmentDto;
    }

    @PatchMapping(path = DELETE_URL)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse deleteDepartment(@RequestHeader(AUTH_HEADER) String jwt,
                                        @RequestHeader(DEPARTMENT_ID_HEADER) String departmentId) {
        ApiResponse response = departmentClient.deleteDepartment(DEPARTMENT_ID_HEADER, departmentId, jwt);

        return response;
    }
}
