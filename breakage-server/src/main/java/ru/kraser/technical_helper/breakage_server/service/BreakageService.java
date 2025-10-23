package ru.kraser.technical_helper.breakage_server.service;

import ru.kraser.technical_helper.common_module.dto.api.ApiResponse;
import ru.kraser.technical_helper.common_module.dto.api.AppPage;
import ru.kraser.technical_helper.common_module.dto.breakage.*;
import ru.kraser.technical_helper.common_module.dto.breakage_comment.CreateBreakageCommentDto;
import ru.kraser.technical_helper.common_module.enums.Role;

public interface BreakageService {

    ApiResponse createBreakage(CreateBreakageFullDto createBreakageFullDto, String currentUserId);

    ApiResponse cancelBreakage(String breakageId, String breakageDepartmentId, String currentUserId,
                               Role currentUserRole, String currentUserDepartmentId, String currentUsername);

    ApiResponse updateBreakageStatus(String breakageId, UpdateBreakageStatusDto updatedStatus,
                                     String currentUserId, String currentUsername);

    ApiResponse updateBreakagePriority(String breakageId, UpdateBreakagePriorityDto updateBreakagePriorityDto,
            String currentUserId, String currentUsername);

    ApiResponse addBreakageExecutor(String breakageId, AppointBreakageExecutorDto appointBreakageExecutorDto,
                                    String currentUserId, String currentUsername);

    ApiResponse dropBreakageExecutor(String breakageId, String currentUserId, String currentUsername);

    AppPage getAllBreakages(
            Integer pageSize, Integer pageIndex, String sortBy, String direction,
            boolean statusNew, boolean statusSolved, boolean statusInProgress,
            boolean statusPaused, boolean statusRedirected, boolean statusCancelled,
            boolean priorityUrgently, boolean priorityHigh, boolean priorityMedium, boolean priorityLow,
            String executor, boolean deadline, String searchText,
            Role currentUserRole, String currentUserDepartmentId, String currentUserId);

    BreakageEmployeeDto getBreakageEmployee(String breakageId, String currentUserDepartmentId);

    ApiResponse getBreakage(String breakageId, String currentUserId);

    // BREAKAGE_COMMENT
    ApiResponse createBreakageComment(
            CreateBreakageCommentDto createBreakageCommentDto, String breakageId, String currentUserId);

    ApiResponse updateBreakageComment(
            CreateBreakageCommentDto createBreakageCommentDto, String breakageCommentId, String currentUserId);

    ApiResponse deleteBreakageComment(String breakageCommentId);
}
