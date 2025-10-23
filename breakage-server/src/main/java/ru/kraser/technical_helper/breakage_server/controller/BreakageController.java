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
import ru.kraser.technical_helper.common_module.enums.Role;

import static ru.kraser.technical_helper.common_module.util.Constant.*;

//@CrossOrigin(origins = FRONT_URL)
@RestController
@RequestMapping(path = BASE_URL + BREAKAGE_URL)
@Slf4j
@RequiredArgsConstructor
public class BreakageController {
    private final BreakageService breakageService;

    @PostMapping(path = EMPLOYEE_URL)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse createBreakage(@RequestHeader (CURRENT_USER_ID_HEADER) String currentUserId,
                                      @RequestBody CreateBreakageFullDto createBreakageFullDto) {
        log.info("Creating Breakage with breakage topic - {}", createBreakageFullDto.breakageTopic());
        ApiResponse response = breakageService.createBreakage(createBreakageFullDto, currentUserId);
        log.info("Breakage with breakage topic - {}, successfully created", createBreakageFullDto.breakageTopic());
        return response;
    }

    @PatchMapping(path = EMPLOYEE_URL + "/{currentUsername}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse cancelBreakage(@RequestHeader (CURRENT_USER_ID_HEADER) String currentUserId,
                                      @RequestHeader (BREAKAGE_ID_HEADER) String breakageId,
                                      @RequestHeader (DEPARTMENT_ID_HEADER) String breakageDepartmentId,
                                      @RequestHeader (USER_ROLE_HEADER) Role currentUserRole,
                                      @RequestHeader (USER_DEPARTMENT_ID_HEADER) String currentUserDepartmentId,
                                      @PathVariable ("currentUsername") String currentUsername) {
        log.info("Canceling Breakage with Id={}", breakageId);
        ApiResponse response = breakageService.cancelBreakage(breakageId, breakageDepartmentId, currentUserId,
                currentUserRole, currentUserDepartmentId, currentUsername);
        log.info("Breakage with Id={}, successfully created", breakageId);
        return response;
    }

    @PatchMapping(path = TECHNICIAN_URL + STATUS_URL + "/{currentUsername}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse updateBreakageStatus(@RequestHeader (CURRENT_USER_ID_HEADER) String currentUserId,
                                            @RequestHeader (BREAKAGE_ID_HEADER) String breakageId,
                                            @RequestBody UpdateBreakageStatusDto updatedStatus,
                                            @PathVariable ("currentUsername") String currentUsername) {
        log.info("Updating Status of breakage with Id={}", breakageId);
        ApiResponse response = breakageService.updateBreakageStatus(
                breakageId, updatedStatus, currentUserId, currentUsername);
        log.info("Status of breakage with Id={}, successfully updated", breakageId);
        return response;
    }

    @PatchMapping(path = ADMIN_URL + PRIORITY_URL + "/{currentUsername}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse updateBreakagePriority(@RequestHeader (CURRENT_USER_ID_HEADER) String currentUserId,
                                              @RequestHeader (BREAKAGE_ID_HEADER) String breakageId,
                                              @RequestBody UpdateBreakagePriorityDto updateBreakagePriorityDto,
                                              @PathVariable ("currentUsername") String currentUsername) {
        log.info("Updating Priority of breakage with Id={}", breakageId);
        ApiResponse response = breakageService.updateBreakagePriority(
                breakageId, updateBreakagePriorityDto, currentUserId, currentUsername);
        log.info("Priority of breakage with Id={}, successfully updated", breakageId);
        return response;
    }

    @PatchMapping(path = ADMIN_URL + EXECUTOR_URL + "/{currentUsername}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse addBreakageExecutor(@RequestHeader (CURRENT_USER_ID_HEADER) String currentUserId,
                                           @RequestHeader (BREAKAGE_ID_HEADER) String breakageId,
                                           @RequestBody AppointBreakageExecutorDto appointBreakageExecutorDto,
                                           @PathVariable ("currentUsername") String currentUsername) {
        log.info("Adding breakageExecutor and deadline of breakage with Id={}", breakageId);
        ApiResponse response = breakageService.addBreakageExecutor(
                breakageId, appointBreakageExecutorDto, currentUserId, currentUsername);
        log.info("Executor and deadline of breakage with Id={}, successfully added", breakageId);
        return response;
    }

    @PatchMapping(path = ADMIN_URL + EXECUTOR_URL + DELETE_URL + "/{currentUsername}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse dropBreakageExecutor(@RequestHeader (CURRENT_USER_ID_HEADER) String currentUserId,
                                            @RequestHeader (BREAKAGE_ID_HEADER) String breakageId,
                                            @PathVariable ("currentUsername") String currentUsername) {
        log.info("Dropping breakageExecutor of breakage with Id={}", breakageId);
        ApiResponse response = breakageService.dropBreakageExecutor(breakageId, currentUserId, currentUsername);
        log.info("Executor and deadline of breakage with Id={}, successfully dropped", breakageId);
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
                                       String searchText,
                                   @RequestHeader (USER_ROLE_HEADER) Role currentUserRole,
                                   @RequestHeader (value = USER_DEPARTMENT_ID_HEADER, required = false)
                                       String currentUserDepartmentId,
                                   @RequestHeader (value = CURRENT_USER_ID_HEADER, required = false)
                                       String currentUserId

    ) {
        log.info("Getting Breakages");
        AppPage appPage = breakageService.getAllBreakages(pageSize, pageIndex, sortBy, direction,
                statusNew, statusSolved, statusInProgress, statusPaused, statusRedirected, statusCancelled,
                priorityUrgently, priorityHigh, priorityMedium, priorityLow, executor, deadline, searchText,
                currentUserRole, currentUserDepartmentId, currentUserId);
        log.info("Breakages received successfully");
        return appPage;
    }

    @GetMapping(path = EMPLOYEE_URL + CURRENT_URL)
    @ResponseStatus(HttpStatus.OK)
    public BreakageEmployeeDto getBreakageEmployee(@RequestHeader(BREAKAGE_ID_HEADER) String breakageId,
                                       @RequestHeader (USER_DEPARTMENT_ID_HEADER) String currentUserDepartmentId) {
        log.info("Getting Breakage for Employee with Id={}", breakageId);
        BreakageEmployeeDto breakageEmployeeDto = breakageService.getBreakageEmployee(
                breakageId, currentUserDepartmentId);
        log.info("Breakage for Employee with Id={}, received successfully", breakageId);
        return breakageEmployeeDto;
    }

    @GetMapping(path = TECHNICIAN_URL + CURRENT_URL)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse getBreakage(@RequestHeader (CURRENT_USER_ID_HEADER) String currentUserId,
                                   @RequestHeader(BREAKAGE_ID_HEADER) String breakageId) {
        log.info("Getting Breakage with Id={}", breakageId);
        ApiResponse response = breakageService.getBreakage(breakageId, currentUserId);
        log.info("Breakage with Id={}, received successfully", breakageId);
        return response;
    }

    // BREAKAGE_COMMENT
    @PostMapping(path = TECHNICIAN_URL + BREAKAGE_COMMENT_URL)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse createBreakageComment(@RequestHeader (CURRENT_USER_ID_HEADER) String currentUserId,
                                             @RequestHeader(BREAKAGE_ID_HEADER) String breakageId,
                                             @RequestBody CreateBreakageCommentDto createBreakageCommentDto) {
        log.info("Creating Comment with breakage Id={}", breakageId);
        ApiResponse apiResponse =  breakageService.createBreakageComment(
                createBreakageCommentDto, breakageId, currentUserId);
        log.info("Comment with breakage Id={}, successfully created", breakageId);
        return apiResponse;
    }

    @PatchMapping(path = TECHNICIAN_URL + BREAKAGE_COMMENT_URL)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse updateBreakageComment(@RequestHeader (CURRENT_USER_ID_HEADER) String currentUserId,
                                             @RequestHeader(BREAKAGE_COMMENT_ID_HEADER) String breakageCommentId,
                                             @RequestBody CreateBreakageCommentDto createBreakageCommentDto) {
        log.info("Updating Comment with Id={}", breakageCommentId);
        ApiResponse apiResponse = breakageService.updateBreakageComment(
                createBreakageCommentDto, breakageCommentId, currentUserId);
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
