package ru.kraser.technical_helper.breakage_server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.kraser.technical_helper.breakage_server.service.BreakageService;
import ru.kraser.technical_helper.common_module.dto.api.ApiResponse;
import ru.kraser.technical_helper.common_module.dto.api.AppPage;
import ru.kraser.technical_helper.common_module.dto.breakage.BreakageFullDto;
import ru.kraser.technical_helper.common_module.dto.breakage.CreateBreakageDto;
import ru.kraser.technical_helper.common_module.dto.breakage.UpdateBreakagePriorityDto;
import ru.kraser.technical_helper.common_module.dto.breakage.UpdateBreakageStatusDto;
import ru.kraser.technical_helper.common_module.dto.breakage_comment.CreateBreakageCommentDto;

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

    @PatchMapping(path = TECHNICIAN_URL + STATUS_URL)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse updateBreakageStatus(@RequestHeader(BREAKAGE_ID_HEADER) String breakageId,
                                            @RequestBody UpdateBreakageStatusDto updatedStatus) {
        return breakageService.updateBreakageStatus(breakageId, updatedStatus);
    }

    @PatchMapping(path = TECHNICIAN_URL + PRIORITY_URL)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse updateBreakagePriority(@RequestHeader(BREAKAGE_ID_HEADER) String breakageId,
                                              @RequestBody UpdateBreakagePriorityDto updateBreakagePriorityDto) {
        return breakageService.updateBreakagePriority(breakageId, updateBreakagePriorityDto);
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
                                       String direction,
                                   @RequestParam(value = "statusNew", defaultValue = "true")
                                       boolean statusNew,
                                   @RequestParam(value = "statusSolved", defaultValue = "false")
                                       boolean statusSolved,
                                   @RequestParam(value = "statusInProgress", defaultValue = "true")
                                       boolean statusInProgress,
                                   @RequestParam(value = "statusPaused", defaultValue = "true")
                                       boolean statusPaused,
                                   @RequestParam(value = "statusRedirected", defaultValue = "true")
                                       boolean statusRedirected,
                                   @RequestParam(value = "statusCancelled", defaultValue = "false")
                                       boolean statusCancelled,
                                   @RequestParam(value = "priorityUrgently", defaultValue = "true")
                                       boolean priorityUrgently,
                                   @RequestParam(value = "priorityHigh", defaultValue = "true")
                                       boolean priorityHigh,
                                   @RequestParam(value = "priorityMedium", defaultValue = "true")
                                       boolean priorityMedium,
                                   @RequestParam(value = "priorityLow", defaultValue = "true")
                                       boolean priorityLow,
                                   @RequestParam(value = "executor", required = false)
                                       String executor
    ) {
        return breakageService.getAllBreakages(pageSize, pageIndex, sortBy, direction,
                statusNew, statusSolved, statusInProgress, statusPaused, statusRedirected, statusCancelled,
                priorityUrgently, priorityHigh, priorityMedium, priorityLow, executor);
    }

    @GetMapping(path = EMPLOYEE_URL + CURRENT_URL)
    @ResponseStatus(HttpStatus.OK)
    public BreakageFullDto getBreakage(@RequestHeader(BREAKAGE_ID_HEADER) String breakageId) {

        return breakageService.getBreakage(breakageId);
    }

    // BREAKAGE_COMMENT
    @PostMapping(path = TECHNICIAN_URL + BREAKAGE_COMMENT_URL)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse createBreakageComment(@RequestHeader(BREAKAGE_ID_HEADER) String breakageId,
                                             @RequestBody CreateBreakageCommentDto createBreakageCommentDto) {
        return breakageService.createBreakageComment(createBreakageCommentDto, breakageId);
    }

    @PatchMapping(path = TECHNICIAN_URL + BREAKAGE_COMMENT_URL)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse updateBreakageComment(@RequestHeader(BREAKAGE_COMMENT_ID_HEADER) String breakageCommentId,
                                             @RequestBody CreateBreakageCommentDto createBreakageCommentDto) {
        return breakageService.updateBreakageComment(createBreakageCommentDto, breakageCommentId);
    }

    @DeleteMapping(path = TECHNICIAN_URL + BREAKAGE_COMMENT_URL)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse deleteBreakageComment(@RequestHeader(BREAKAGE_COMMENT_ID_HEADER) String breakageCommentId) {
        return breakageService.deleteBreakageComment(breakageCommentId);
    }
}
