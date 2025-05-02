package ru.kraser.technical_helper.main_server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.kraser.technical_helper.common_module.dto.department.CreateDepartmentDto;
import ru.kraser.technical_helper.common_module.dto.department.DepartmentDto;
import ru.kraser.technical_helper.main_server.service.DepartmentService;

import java.util.List;

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

    @PatchMapping
    @ResponseStatus(HttpStatus.OK)
    public String updateDepartment(@RequestHeader ("X-TH-Department-Id") String departmentId,
                                   @RequestBody CreateDepartmentDto departmentDto) {
        return departmentService.updateDepartment(departmentId, departmentDto);
    }

    @GetMapping(path = "/all")
    @ResponseStatus(HttpStatus.OK)
    public List<DepartmentDto> getAllDepartments() {
        return departmentService.getAllDepartments();
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public DepartmentDto getDepartment(@RequestHeader ("X-TH-Department-Id") String departmentId) {
        return departmentService.getDepartment(departmentId);
    }

    @PatchMapping(path = "/delete")
    @ResponseStatus(HttpStatus.OK)
    public String deleteDepartment(@RequestHeader ("X-TH-Department-Id") String departmentId) {
        return departmentService.deleteDepartment(departmentId);
    }
}
