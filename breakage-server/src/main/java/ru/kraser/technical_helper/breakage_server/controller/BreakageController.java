package ru.kraser.technical_helper.breakage_server.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.kraser.technical_helper.breakage_server.service.BreakageService;
import ru.kraser.technical_helper.common_module.dto.api.ApiResponse;
import ru.kraser.technical_helper.common_module.dto.api.AppPage;
import ru.kraser.technical_helper.common_module.dto.breakage.*;
import ru.kraser.technical_helper.common_module.dto.breakage_comment.CreateBreakageCommentDto;

import static ru.kraser.technical_helper.common_module.util.Constant.*;

@RestController
@RequestMapping(path = BASE_URL + BREAKAGE_URL)
@Slf4j
@RequiredArgsConstructor
public class BreakageController {
    private final BreakageService breakageService;

    @PostMapping(path = EMPLOYEE_URL)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse createBreakage(@RequestBody CreateBreakageDto createBreakageDto) {
        log.info("Creating Breakage with breakage topic - {}", createBreakageDto.breakageTopic());
        ApiResponse response = breakageService.createBreakage(createBreakageDto);
        log.info("Breakage with breakage topic - {}, successfully created", createBreakageDto.breakageTopic());
        return response;
    }

    @PatchMapping(path = EMPLOYEE_URL)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse cancelBreakage(@RequestHeader(BREAKAGE_ID_HEADER) String breakageId,
                                      @RequestHeader(DEPARTMENT_ID_HEADER) String breakageDepartmentId) {
        log.info("Canceling Breakage with Id={}", breakageId);
        ApiResponse response = breakageService.cancelBreakage(breakageId, breakageDepartmentId);
        log.info("Breakage with Id={}, successfully created", breakageId);
        return response;
    }

    @PatchMapping(path = TECHNICIAN_URL + STATUS_URL)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse updateBreakageStatus(@RequestHeader(BREAKAGE_ID_HEADER) String breakageId,
                                            @RequestBody UpdateBreakageStatusDto updatedStatus) {
        log.info("Updating Status of breakage with Id={}", breakageId);
        ApiResponse response = breakageService.updateBreakageStatus(breakageId, updatedStatus);
        log.info("Status of breakage with Id={}, successfully updated", breakageId);
        return response;
    }

    @PatchMapping(path = ADMIN_URL + PRIORITY_URL)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse updateBreakagePriority(@RequestHeader(BREAKAGE_ID_HEADER) String breakageId,
                                              @RequestBody UpdateBreakagePriorityDto updateBreakagePriorityDto) {
        log.info("Updating Priority of breakage with Id={}", breakageId);
        ApiResponse response = breakageService.updateBreakagePriority(breakageId, updateBreakagePriorityDto);
        log.info("Priority of breakage with Id={}, successfully updated", breakageId);
        return response;
    }

    @PatchMapping(path = ADMIN_URL)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse addBreakageExecutor(@RequestHeader(BREAKAGE_ID_HEADER) String breakageId,
                                           @RequestBody AppointBreakageExecutorDto appointBreakageExecutorDto) {
        log.info("Adding breakageExecutor and deadline of breakage with Id={}", breakageId);
        ApiResponse response = breakageService.addBreakageExecutor(breakageId, appointBreakageExecutorDto);
        log.info("Executor and deadline of breakage with Id={}, successfully added", breakageId);
        return response;
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
                                   @RequestParam(value = "breakageExecutor", required = false)
                                       String executor,
                                   @RequestParam(value = "deadline", defaultValue = "false")
                                       boolean deadline,
                                   @RequestParam(value = "searchText", required = false)
                                       String searchText
    ) {
        log.info("Getting Breakages");
        AppPage appPage = breakageService.getAllBreakages(pageSize, pageIndex, sortBy, direction,
                statusNew, statusSolved, statusInProgress, statusPaused, statusRedirected, statusCancelled,
                priorityUrgently, priorityHigh, priorityMedium, priorityLow, executor, deadline, searchText);
        log.info("Breakages received successfully");
        return appPage;
    }

    /*@GetMapping(path = EMPLOYEE_URL + "/{text}")
    @ResponseStatus(HttpStatus.OK)
    public AppPage getBreakagesByText(@PathVariable("text") String text,
                                      @RequestParam(value = "pageSize", defaultValue = "10")
                                      Integer pageSize,
                                      @RequestParam(value = "pageIndex", defaultValue = "0")
                                      Integer pageIndex,
                                      @RequestParam(value = "sortBy", defaultValue = "lastUpdatedDate")
                                      String sortBy,
                                      @RequestParam(value = "direction", defaultValue = "DESC")
                                      String direction
    ) {
        log.info("Getting Breakages by text");
        AppPage appPage = breakageService.getBreakagesByText(text, pageIndex, pageSize, sortBy, direction);
        log.info("Breakages contains text, received successfully");
        return appPage;
    }*/

    @GetMapping(path = EMPLOYEE_URL + CURRENT_URL)
    @ResponseStatus(HttpStatus.OK)
    public BreakageFullDto getBreakage(@RequestHeader(BREAKAGE_ID_HEADER) String breakageId) {
        log.info("Getting Breakage with Id={}", breakageId);
        BreakageFullDto breakageFullDto = breakageService.getBreakage(breakageId);
        log.info("Breakage with Id={}, received successfully", breakageId);
        return breakageFullDto;
    }

    // BREAKAGE_COMMENT
    @PostMapping(path = TECHNICIAN_URL + BREAKAGE_COMMENT_URL)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse createBreakageComment(@RequestHeader(BREAKAGE_ID_HEADER) String breakageId,
                                             @RequestBody CreateBreakageCommentDto createBreakageCommentDto) {
        log.info("Creating Comment with breakage Id={}", breakageId);
        ApiResponse apiResponse =  breakageService.createBreakageComment(createBreakageCommentDto, breakageId);
        log.info("Comment with breakage Id={}, successfully created", breakageId);
        return apiResponse;
    }

    @PatchMapping(path = TECHNICIAN_URL + BREAKAGE_COMMENT_URL)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse updateBreakageComment(@RequestHeader(BREAKAGE_COMMENT_ID_HEADER) String breakageCommentId,
                                             @RequestBody CreateBreakageCommentDto createBreakageCommentDto) {
        log.info("Updating Comment with Id={}", breakageCommentId);
        ApiResponse apiResponse =   breakageService.updateBreakageComment(createBreakageCommentDto, breakageCommentId);
        log.info("Comment with Id={}, successfully updated", breakageCommentId);
        return apiResponse;
    }

    @DeleteMapping(path = TECHNICIAN_URL + BREAKAGE_COMMENT_URL)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse deleteBreakageComment(@RequestHeader(BREAKAGE_COMMENT_ID_HEADER) String breakageCommentId) {
        log.info("Deleting Comment with Id={}", breakageCommentId);
        ApiResponse apiResponse = breakageService.deleteBreakageComment(breakageCommentId);
        log.info("Comment with Id={}, successfully deleted", breakageCommentId);
        return apiResponse;
    }
}
