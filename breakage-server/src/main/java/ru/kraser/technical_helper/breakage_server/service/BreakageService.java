package ru.kraser.technical_helper.breakage_server.service;

import ru.kraser.technical_helper.common_module.dto.api.ApiResponse;
import ru.kraser.technical_helper.common_module.dto.api.AppPage;
import ru.kraser.technical_helper.common_module.dto.breakage.*;
import ru.kraser.technical_helper.common_module.dto.breakage_comment.CreateBreakageCommentDto;

public interface BreakageService {

    ApiResponse createBreakage(CreateBreakageDto createBreakageDto);

    ApiResponse cancelBreakage(String breakageId, String breakageDepartmentId);

    ApiResponse updateBreakageStatus(String breakageId, UpdateBreakageStatusDto updatedStatus);

    ApiResponse updateBreakagePriority(String breakageId, UpdateBreakagePriorityDto updateBreakagePriorityDto);

    ApiResponse addBreakageExecutor(String breakageId, AppointBreakageExecutorDto appointBreakageExecutorDto);

    AppPage getAllBreakages(
            Integer pageSize, Integer pageIndex, String sortBy, String direction,
            boolean statusNew, boolean statusSolved, boolean statusInProgress,
            boolean statusPaused, boolean statusRedirected, boolean statusCancelled,
            boolean priorityUrgently, boolean priorityHigh, boolean priorityMedium, boolean priorityLow,
            String executor, boolean deadline);

    // BREAKAGE_COMMENT
    ApiResponse createBreakageComment(CreateBreakageCommentDto createBreakageCommentDto, String breakageId);

    ApiResponse updateBreakageComment(CreateBreakageCommentDto createBreakageCommentDto, String breakageCommentId);

    ApiResponse deleteBreakageComment(String breakageCommentId);

    BreakageFullDto getBreakage(String breakageId);
}
