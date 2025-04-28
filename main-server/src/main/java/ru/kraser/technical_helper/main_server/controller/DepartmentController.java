package ru.kraser.technical_helper.main_server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.kraser.technical_helper.common_module.dto.department.CreateDepartmentDto;
import ru.kraser.technical_helper.main_server.service.DepartmentService;

import static ru.kraser.technical_helper.common_module.util.Constant.ADMIN_URL;
import static ru.kraser.technical_helper.common_module.util.Constant.BASE_URL;

@RestController
@RequestMapping(path = BASE_URL + ADMIN_URL + "/department")
@RequiredArgsConstructor
public class DepartmentController {
    private final DepartmentService departmentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String createDepartment(@RequestBody CreateDepartmentDto createDepartmentDto) {
        return departmentService.createDepartment(createDepartmentDto);
    }

    /*@GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Department findDepartmentById(@RequestHeader ("X-TH-Department-Id") String departmentId) {
        return departmentService.getDepartment(departmentId);
    }*/
}
