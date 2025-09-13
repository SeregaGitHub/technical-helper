package ru.kraser.technical_helper.gateway.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.kraser.technical_helper.common_module.dto.api.ApiResponse;
import ru.kraser.technical_helper.common_module.dto.api.AppPage;
import ru.kraser.technical_helper.common_module.dto.breakage.*;
import ru.kraser.technical_helper.common_module.dto.breakage_comment.CreateBreakageCommentDto;
import ru.kraser.technical_helper.gateway.client.BreakageClient;

import static ru.kraser.technical_helper.common_module.util.Constant.*;

@CrossOrigin(origins = FRONT_URL)
@RestController
@RequestMapping(path = BASE_URL + BREAKAGE_URL)
@RequiredArgsConstructor
@Slf4j
@Validated
public class BreakageGatewayController {
    private final BreakageClient breakageClient;

    @PostMapping(path = EMPLOYEE_URL)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse createBreakage(@Validated() @RequestBody CreateBreakageDto createBreakageDto,
                                      @RequestHeader(AUTH_HEADER) String jwt) {
        log.info("Creating Breakage with breakage topic - {}", createBreakageDto.breakageTopic());
        ApiResponse response = breakageClient.createBreakage(createBreakageDto, jwt);
        log.info("Breakage with breakage topic - {}, successfully created", createBreakageDto.breakageTopic());
        return response;
    }

    @PatchMapping(path = EMPLOYEE_URL)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse cancelBreakage(@RequestHeader(AUTH_HEADER) String jwt,
                                      @RequestHeader(BREAKAGE_ID_HEADER) String breakageId,
                                      @RequestHeader(DEPARTMENT_ID_HEADER) String breakageDepartmentId) {
        log.info("Canceling Breakage with Id={}", breakageId);
        ApiResponse response = breakageClient.cancelBreakage(BREAKAGE_ID_HEADER, breakageId,
                DEPARTMENT_ID_HEADER, breakageDepartmentId,
                jwt);
        log.info("Breakage with Id={}, successfully created", breakageId);
        return response;
    }

    @PatchMapping(path = TECHNICIAN_URL + STATUS_URL)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse updateBreakageStatus(@RequestHeader(AUTH_HEADER) String jwt,
                                            @RequestHeader(BREAKAGE_ID_HEADER) String breakageId,
                                            @Validated() @RequestBody UpdateBreakageStatusDto updatedStatus) {
        log.info("Updating Status of breakage with Id={}", breakageId);
        ApiResponse response = breakageClient.updateBreakageStatus(BREAKAGE_ID_HEADER, breakageId,
                updatedStatus, jwt);
        log.info("Status of breakage with Id={}, successfully updated", breakageId);
        return response;
    }

    @PatchMapping(path = ADMIN_URL + PRIORITY_URL)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse updateBreakagePriority(@RequestHeader(AUTH_HEADER) String jwt,
                                              @RequestHeader(BREAKAGE_ID_HEADER) String breakageId,
                                              @Validated() @RequestBody UpdateBreakagePriorityDto updatedPriority) {
        log.info("Updating Priority of breakage with Id={}", breakageId);
        ApiResponse response = breakageClient.updateBreakagePriority(BREAKAGE_ID_HEADER, breakageId,
                updatedPriority, jwt);
        log.info("Priority of breakage with Id={}, successfully updated", breakageId);
        return response;
    }

    @PatchMapping(path = ADMIN_URL)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse addBreakageExecutor(@RequestHeader(AUTH_HEADER) String jwt,
                                           @RequestHeader(BREAKAGE_ID_HEADER) String breakageId,
                                           @Validated() @RequestBody AppointBreakageExecutorDto appointBreakageExecutorDto) {
        log.info("Adding breakageExecutor and deadline of breakage with Id={}", breakageId);
        ApiResponse response = breakageClient.addBreakageExecutor(BREAKAGE_ID_HEADER, breakageId,
                appointBreakageExecutorDto, jwt);
        log.info("Executor and deadline of breakage with Id={}, successfully added", breakageId);
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
        AppPage employeeBreakageDtoList = breakageClient.getAllBreakages(
                jwt, pageSize, pageIndex, sortBy, direction,
                statusNew, statusSolved, statusInProgress, statusPaused, statusRedirected, statusCancelled,
                priorityUrgently, priorityHigh, priorityMedium, priorityLow, executor, deadline, searchText);
        log.info("Breakages received successfully");
        return employeeBreakageDtoList;
    }

    @GetMapping(path = EMPLOYEE_URL + CURRENT_URL)
    @ResponseStatus(HttpStatus.OK)
    public BreakageFullDto getBreakage(@RequestHeader(AUTH_HEADER) String jwt,
                                       @RequestHeader(BREAKAGE_ID_HEADER) String breakageId) {
        log.info("Getting Breakage with Id={}", breakageId);
        BreakageFullDto response = breakageClient.getBreakage(jwt, BREAKAGE_ID_HEADER, breakageId);
        log.info("Breakage with Id={}, received successfully", breakageId);
        return response;
    }

/*    @GetMapping(path = EMPLOYEE_URL + "/{text}")
    @ResponseStatus(HttpStatus.OK)
    public AppPage getBreakagesByText(@RequestHeader(AUTH_HEADER) String jwt,
                                      @PathVariable("text") String text,
                                      @RequestParam(value = "pageSize", defaultValue = "10")
                                          Integer pageSize,
                                      @RequestParam(value = "pageIndex", defaultValue = "0")
                                          Integer pageIndex,
                                      @RequestParam(value = "sortBy", defaultValue = "lastUpdatedDate")
                                          String sortBy,
                                      @RequestParam(value = "direction", defaultValue = "DESC")
                                          String direction) {
        log.info("Getting Breakages by text");
        AppPage response = breakageClient.getBreakagesByText(jwt, text, pageIndex, pageSize, sortBy, direction);
        log.info("Breakages contains text, received successfully");
        return response;
    }*/

    // BREAKAGE_COMMENT
    @PostMapping(path = TECHNICIAN_URL + BREAKAGE_COMMENT_URL)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse createBreakageComment(@RequestHeader(AUTH_HEADER) String jwt,
                                             @RequestHeader(BREAKAGE_ID_HEADER) String breakageId,
                                             @Validated() @RequestBody CreateBreakageCommentDto createBreakageCommentDto) {
        log.info("Creating Comment with breakage Id={}", breakageId);
        ApiResponse response = breakageClient.createBreakageComment(createBreakageCommentDto,
                BREAKAGE_ID_HEADER, breakageId, jwt);
        log.info("Comment with breakage Id={}, successfully created", breakageId);
        return response;
    }

    @PatchMapping(path = TECHNICIAN_URL + BREAKAGE_COMMENT_URL)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse updateBreakageComment(@RequestHeader(AUTH_HEADER) String jwt,
                                             @RequestHeader(BREAKAGE_COMMENT_ID_HEADER) String breakageCommentId,
                                             @Validated() @RequestBody CreateBreakageCommentDto createBreakageCommentDto) {
        log.info("Updating Comment with Id={}", breakageCommentId);
        ApiResponse response = breakageClient.updateBreakageComment(createBreakageCommentDto,
                BREAKAGE_COMMENT_ID_HEADER, breakageCommentId, jwt);
        log.info("Comment with Id={}, successfully updated", breakageCommentId);
        return response;
    }

    @DeleteMapping(path = TECHNICIAN_URL + BREAKAGE_COMMENT_URL)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse deleteBreakageComment(@RequestHeader(AUTH_HEADER) String jwt,
                                             @RequestHeader(BREAKAGE_COMMENT_ID_HEADER) String breakageCommentId) {
        log.info("Deleting Comment with Id={}", breakageCommentId);
        ApiResponse response = breakageClient.deleteBreakageComment(BREAKAGE_COMMENT_ID_HEADER, breakageCommentId, jwt);
        log.info("Comment with Id={}, successfully deleted", breakageCommentId);
        return response;
    }
}
