package ru.kraser.technical_helper.gateway.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.kraser.technical_helper.common_module.dto.department.CreateDepartmentDto;
import ru.kraser.technical_helper.gateway.client.DepartmentClient;

import static ru.kraser.technical_helper.common_module.util.Constant.*;

@RestController
@RequestMapping(path = BASE_URL + ADMIN_URL + DEPARTMENT_URL)
@RequiredArgsConstructor
@Validated
public class DepartmentGatewayController {
    private final DepartmentClient departmentClient;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String createDepartment(@Validated() @RequestBody CreateDepartmentDto createDepartmentDto,
                                          @RequestHeader(AUTHORIZATION) String jwt) {
        String response = departmentClient.createDepartment(createDepartmentDto, jwt);

        return response;
    }
}
