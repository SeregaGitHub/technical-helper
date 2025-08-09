package ru.kraser.technical_helper.gateway.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.kraser.technical_helper.common_module.dto.api.ApiResponse;
import ru.kraser.technical_helper.common_module.dto.api.AppPage;
import ru.kraser.technical_helper.common_module.dto.breakage.CreateBreakageDto;
import ru.kraser.technical_helper.gateway.client.BreakageClient;

import static ru.kraser.technical_helper.common_module.util.Constant.*;

@CrossOrigin(origins = FRONT_URL)
@RestController
@RequestMapping(path = BASE_URL + BREAKAGE_URL)
@RequiredArgsConstructor
@Validated
public class BreakageGatewayController {
    private final BreakageClient breakageClient;

    @PostMapping(path = EMPLOYEE_URL)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse createDepartment(@Validated() @RequestBody CreateBreakageDto createBreakageDto,
                                        @RequestHeader(AUTH_HEADER) String jwt) {
        ApiResponse response = breakageClient.createBreakage(createBreakageDto, jwt);

        return response;
    }

    @PatchMapping(path = EMPLOYEE_URL)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse cancelBreakage(@RequestHeader(AUTH_HEADER) String jwt,
                                      @RequestHeader(BREAKAGE_ID_HEADER) String breakageId,
                                      @RequestHeader(DEPARTMENT_ID_HEADER) String breakageDepartmentId) {
        ApiResponse response = breakageClient.cancelBreakage(BREAKAGE_ID_HEADER, breakageId,
                DEPARTMENT_ID_HEADER, breakageDepartmentId,
                jwt);

        return response;
    }

    @GetMapping(path = EMPLOYEE_URL)
    @ResponseStatus(HttpStatus.OK)
    public AppPage getAllBreakages(@RequestHeader(AUTH_HEADER) String jwt,
                                   @RequestParam(value = "pageSize", defaultValue = "10")
                                                     Integer pageSize,
                                   @RequestParam(value = "pageIndex", defaultValue = "0")
                                                     Integer pageIndex,
                                   @RequestParam(value = "sortBy", defaultValue = "lastUpdatedDate")
                                                     String sortBy,
                                   @RequestParam(value = "direction", defaultValue = "DESC")
                                                     String direction) {
        AppPage employeeBreakageDtoList = breakageClient.getAllBreakages(
                jwt, pageSize, pageIndex, sortBy, direction);

        return employeeBreakageDtoList;
    }
}
