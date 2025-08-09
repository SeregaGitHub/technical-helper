package ru.kraser.technical_helper.breakage_server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.kraser.technical_helper.breakage_server.service.BreakageService;
import ru.kraser.technical_helper.common_module.dto.api.ApiResponse;
import ru.kraser.technical_helper.common_module.dto.api.AppPage;
import ru.kraser.technical_helper.common_module.dto.breakage.CreateBreakageDto;

import static ru.kraser.technical_helper.common_module.util.Constant.*;

@RestController
@RequestMapping(path = BASE_URL + BREAKAGE_URL)
@RequiredArgsConstructor
public class BreakageController {
    private final BreakageService breakageService;

    @PostMapping(path = EMPLOYEE_URL)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse createBreakage(@RequestBody CreateBreakageDto createBreakageDto) {
        return breakageService.createBreakage(createBreakageDto);
    }

    @PatchMapping(path = EMPLOYEE_URL)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse cancelBreakage(@RequestHeader(BREAKAGE_ID_HEADER) String breakageId,
                                      @RequestHeader(DEPARTMENT_ID_HEADER) String breakageDepartmentId) {
        return breakageService.cancelBreakage(breakageId, breakageDepartmentId);
    }

    @GetMapping(path = EMPLOYEE_URL)
    @ResponseStatus(HttpStatus.OK)
    public AppPage getAllBreakages(@RequestParam(value = "pageSize", defaultValue = "10")
                                                     Integer pageSize,
                                   @RequestParam(value = "pageIndex", defaultValue = "0")
                                                     Integer pageIndex,
                                   @RequestParam(value = "sortBy", defaultValue = "lastUpdatedDate")
                                                     String sortBy,
                                   @RequestParam(value = "direction", defaultValue = "DESC")
                                                     String direction
    ) {
        return breakageService.getAllBreakages(pageSize, pageIndex, sortBy, direction);
    }
}
