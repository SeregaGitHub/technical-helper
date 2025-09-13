package ru.kraser.technical_helper.gateway.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.kraser.technical_helper.common_module.dto.api.ApiResponse;
import ru.kraser.technical_helper.common_module.dto.api.AppPage;
import ru.kraser.technical_helper.common_module.dto.breakage.*;
import ru.kraser.technical_helper.common_module.dto.breakage_comment.CreateBreakageCommentDto;

import static ru.kraser.technical_helper.common_module.util.Constant.*;

@Service
public class BreakageClient extends BaseClient {
    public BreakageClient(WebClient webClient) {
        super(webClient);
    }

    public ApiResponse createBreakage(CreateBreakageDto createBreakageDto, String jwt) {
        return super.post(
                BREAKAGE_SERVER_URL + BASE_URL + BREAKAGE_URL + EMPLOYEE_URL,
                createBreakageDto,
                jwt,
                ParameterizedTypeReference.forType(ApiResponse.class)
        );
    }

    public ApiResponse cancelBreakage(String breakageHeaderName, String breakageId,
                                      String breakageDepartmentHeaderName, String breakageDepartmentId, String jwt) {
        return super.patch(
                BREAKAGE_SERVER_URL + BASE_URL + BREAKAGE_URL + EMPLOYEE_URL,
                jwt,
                breakageHeaderName,
                breakageId,
                breakageDepartmentHeaderName,
                breakageDepartmentId,
                ParameterizedTypeReference.forType(ApiResponse.class)
        );
    }

    public ApiResponse updateBreakageStatus(String breakageHeaderName, String breakageId,
                                            UpdateBreakageStatusDto updatedStatus, String jwt) {
        return super.patch(
                BREAKAGE_SERVER_URL + BASE_URL + BREAKAGE_URL + TECHNICIAN_URL + STATUS_URL,
                updatedStatus,
                jwt,
                breakageHeaderName,
                breakageId,
                ParameterizedTypeReference.forType(ApiResponse.class)
        );
    }

    public ApiResponse updateBreakagePriority(String breakageHeaderName, String breakageId,
                                              UpdateBreakagePriorityDto updatedPriority, String jwt) {
        return super.patch(
                BREAKAGE_SERVER_URL + BASE_URL + BREAKAGE_URL + ADMIN_URL + PRIORITY_URL,
                updatedPriority,
                jwt,
                breakageHeaderName,
                breakageId,
                ParameterizedTypeReference.forType(ApiResponse.class)
        );
    }

    public ApiResponse addBreakageExecutor(String breakageHeaderName, String breakageId,
                                           AppointBreakageExecutorDto appointBreakageExecutorDto, String jwt) {
        return super.patch(
                BREAKAGE_SERVER_URL + BASE_URL + BREAKAGE_URL + ADMIN_URL,
                appointBreakageExecutorDto,
                jwt,
                breakageHeaderName,
                breakageId,
                ParameterizedTypeReference.forType(ApiResponse.class)
        );
    }

    public AppPage getAllBreakages(
            String jwt, Integer pageSize, Integer pageIndex, String sortBy, String direction,
            boolean statusNew, boolean statusSolved, boolean statusInProgress,
            boolean statusPaused, boolean statusRedirected, boolean statusCancelled,
            boolean priorityUrgently, boolean priorityHigh, boolean priorityMedium, boolean priorityLow,
            String executor, boolean deadline, String searchText) {

        return super.getAllByPage(
                BREAKAGE_SERVER_URL + BASE_URL + BREAKAGE_URL + EMPLOYEE_URL,
                pageSize, pageIndex, sortBy, direction,
                statusNew, statusSolved, statusInProgress, statusPaused, statusRedirected, statusCancelled,
                priorityUrgently, priorityHigh, priorityMedium, priorityLow, executor, deadline, searchText,
                jwt,
                ParameterizedTypeReference.forType(AppPage.class)
        );
    }

    /*public AppPage getBreakagesByText(
            String jwt, String text, Integer pageIndex, Integer pageSize, String sortBy, String direction) {
        return super.getAllByText(
                BREAKAGE_SERVER_URL + BASE_URL + BREAKAGE_URL + EMPLOYEE_URL + "/" + text,
                jwt,
                pageIndex,
                pageSize,
                sortBy,
                direction,
                ParameterizedTypeReference.forType(AppPage.class)
        );
    }*/

    public BreakageFullDto getBreakage(String jwt, String breakageHeaderName, String breakageId) {
        return super.get(
                BREAKAGE_SERVER_URL + BASE_URL + BREAKAGE_URL + EMPLOYEE_URL + CURRENT_URL,
                jwt,
                breakageHeaderName,
                breakageId,
                ParameterizedTypeReference.forType(BreakageFullDto.class)
        );
    }

    public ApiResponse createBreakageComment(CreateBreakageCommentDto createBreakageCommentDto,
                                             String breakageHeaderName, String breakageId, String jwt) {
        return super.post(
                BREAKAGE_SERVER_URL + BASE_URL + BREAKAGE_URL + TECHNICIAN_URL + BREAKAGE_COMMENT_URL,
                createBreakageCommentDto,
                breakageHeaderName,
                breakageId,
                jwt,
                ParameterizedTypeReference.forType(ApiResponse.class)
        );
    }

    public ApiResponse updateBreakageComment(CreateBreakageCommentDto createBreakageCommentDto,
                                             String breakageCommentHeaderName, String breakageCommentId, String jwt) {
        return super.patch(
                BREAKAGE_SERVER_URL + BASE_URL + BREAKAGE_URL + TECHNICIAN_URL + BREAKAGE_COMMENT_URL,
                createBreakageCommentDto,
                jwt,
                breakageCommentHeaderName,
                breakageCommentId,
                ParameterizedTypeReference.forType(ApiResponse.class)
        );
    }

    public ApiResponse deleteBreakageComment(String breakageCommentHeaderName, String breakageCommentId, String jwt) {
        return super.hardDelete(
                BREAKAGE_SERVER_URL + BASE_URL + BREAKAGE_URL + TECHNICIAN_URL + BREAKAGE_COMMENT_URL,
                jwt,
                breakageCommentHeaderName,
                breakageCommentId,
                ParameterizedTypeReference.forType(ApiResponse.class)
        );
    }
}
