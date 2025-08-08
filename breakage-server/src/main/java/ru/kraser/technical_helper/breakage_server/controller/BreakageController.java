package ru.kraser.technical_helper.breakage_server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.kraser.technical_helper.breakage_server.service.BreakageService;
import ru.kraser.technical_helper.common_module.dto.api.ApiResponse;
import ru.kraser.technical_helper.common_module.dto.breakage.BreakageDto;
import ru.kraser.technical_helper.common_module.dto.breakage.CreateBreakageDto;

import java.util.List;

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
    public List<BreakageDto> getAllBreakages(@RequestParam(value = "size", defaultValue = "10")
                                                     Integer size,
                                             @RequestParam(value = "from", defaultValue = "0")
                                                     Integer from,
                                             @RequestParam(value = "sortBy", defaultValue = "lastUpdatedDate")
                                                     String sortBy,
                                             @RequestParam(value = "direction", defaultValue = "DESC")
                                                     String direction
    ) {
        return breakageService.getAllBreakages(size, from, sortBy, direction);
    }
}
