package ru.kraser.technical_helper.breakage_server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.kraser.technical_helper.common_module.dto.api.ApiResponse;
import ru.kraser.technical_helper.main_server.repository.DepartmentRepository;

import java.time.LocalDateTime;

import static ru.kraser.technical_helper.common_module.util.Constant.*;

@RestController
@RequestMapping(path = BASE_URL + BREAKAGE_URL)
@RequiredArgsConstructor
public class BreakageController {
    private final DepartmentRepository departmentRepository;

    @PostMapping(path = EMPLOYEE_URL)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse createBreakage() {
        //Department department = departmentRepository.getReferenceById("ac140a02-96c2-1909-8196-c2aaf9f10001");
        //System.out.println(department.getName());

        //return departmentService.createDepartment(createBreakageDto);
        return ApiResponse.builder()
                .message("Created")
                .status(201)
                .httpStatus(HttpStatus.CREATED)
                .timestamp(LocalDateTime.now().withNano(0))
                .build();
    }

    @GetMapping(path = EMPLOYEE_URL)
    @ResponseStatus(HttpStatus.OK)
    public String getEmployeeTest() {
        return "Hello from Employee-Breakage Backend !!!";
    }

    @GetMapping(path = ADMIN_URL)
    @ResponseStatus(HttpStatus.OK)
    public String getEmployeeT() {
        return "Hello from Admin-Breakage Backend !!!";
    }
}
