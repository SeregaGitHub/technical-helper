package ru.kraser.technical_helper.breakage_server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.kraser.technical_helper.common_module.dto.api.ApiResponse;
import ru.kraser.technical_helper.common_module.dto.user.UserIdsDto;
import ru.kraser.technical_helper.common_module.util.SecurityUtil;

import java.time.LocalDateTime;

import static ru.kraser.technical_helper.common_module.util.Constant.*;

@RestController
@RequestMapping(path = BASE_URL + BREAKAGE_URL)
@RequiredArgsConstructor
public class BreakageController {

    @PostMapping(path = EMPLOYEE_URL)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse createBreakage() {

        return ApiResponse.builder()
                .message("Created")
                .status(201)
                .httpStatus(HttpStatus.CREATED)
                .timestamp(LocalDateTime.now().withNano(0))
                .build();
    }

    @GetMapping(path = EMPLOYEE_URL)
    @ResponseStatus(HttpStatus.OK)
    public String getEmpTest() {
        return "Hello from Employee-Breakage Backend !!!";
    }

    @GetMapping(path = TECHNICIAN_URL)
    @ResponseStatus(HttpStatus.OK)
    public String getTechTest() {
        UserIdsDto userIds = SecurityUtil.getUserIds();
        return "Hello from Technician-Breakage Backend !!!\nUser Id = " + userIds.userId() +
                ". UserDepartment Id = " + userIds.departmentId();
    }

    @GetMapping(path = ADMIN_URL)
    @ResponseStatus(HttpStatus.OK)
    public String getAdmTest() {
        return "Hello from Admin-Breakage Backend !!!";
    }
}
