package ru.kraser.technical_helper.gateway.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.kraser.technical_helper.common_module.dto.api.ApiResponse;
import ru.kraser.technical_helper.common_module.dto.api.AppPage;
import ru.kraser.technical_helper.common_module.dto.breakage.*;
import ru.kraser.technical_helper.common_module.dto.breakage_comment.CreateBreakageCommentDto;
import ru.kraser.technical_helper.common_module.enums.Role;
import ru.kraser.technical_helper.common_module.util.SecurityUtil;

import static ru.kraser.technical_helper.common_module.util.Constant.*;

@Service
public class BreakageClient extends BaseClient {
    public BreakageClient(WebClient webClient) {
        super(webClient);
    }

    public ApiResponse createBreakage(CreateBreakageDto createBreakageDto) {
        return super.post(
                BREAKAGE_SERVER_URL + BASE_URL + BREAKAGE_URL + EMPLOYEE_URL,
                createBreakageDto,
                ParameterizedTypeReference.forType(ApiResponse.class)
        );
    }

    public ApiResponse cancelBreakage(String breakageHeaderName, String breakageId,
                                      String breakageDepartmentHeaderName, String breakageDepartmentId) {
        return super.patch(
                BREAKAGE_SERVER_URL + BASE_URL + BREAKAGE_URL + EMPLOYEE_URL +
                        "/" + SecurityUtil.getCurrentUsername(),
                breakageHeaderName,
                breakageId,
                breakageDepartmentHeaderName,
                breakageDepartmentId,
                USER_ROLE_HEADER,
                SecurityUtil.getCurrentUserRole().toString(),
                USER_DEPARTMENT_ID_HEADER,
                SecurityUtil.getCurrentUserDepartment().getId(),
                ParameterizedTypeReference.forType(ApiResponse.class)
        );
    }

    public ApiResponse updateBreakageStatus(String breakageHeaderName, String breakageId,
                                            UpdateBreakageStatusDto updatedStatus) {
        return super.patch(
                BREAKAGE_SERVER_URL + BASE_URL + BREAKAGE_URL + TECHNICIAN_URL + STATUS_URL +
                        "/" + SecurityUtil.getCurrentUsername(),
                updatedStatus,
                breakageHeaderName,
                breakageId,
                ParameterizedTypeReference.forType(ApiResponse.class)
        );
    }

    public ApiResponse updateBreakagePriority(String breakageHeaderName, String breakageId,
                                              UpdateBreakagePriorityDto updatedPriority) {
        return super.patch(
                BREAKAGE_SERVER_URL + BASE_URL + BREAKAGE_URL + ADMIN_URL + PRIORITY_URL +
                        "/" + SecurityUtil.getCurrentUsername(),
                updatedPriority,
                breakageHeaderName,
                breakageId,
                ParameterizedTypeReference.forType(ApiResponse.class)
        );
    }

    public ApiResponse addBreakageExecutor(String breakageHeaderName, String breakageId,
                                           AppointBreakageExecutorDto appointBreakageExecutorDto) {
        return super.patch(
                BREAKAGE_SERVER_URL + BASE_URL + BREAKAGE_URL + ADMIN_URL + EXECUTOR_URL +
                        "/" + SecurityUtil.getCurrentUsername(),
                appointBreakageExecutorDto,
                breakageHeaderName,
                breakageId,
                ParameterizedTypeReference.forType(ApiResponse.class)
        );
    }

    public ApiResponse dropBreakageExecutor(String breakageHeaderName, String breakageId) {
        return super.delete(
                BREAKAGE_SERVER_URL + BASE_URL + BREAKAGE_URL + ADMIN_URL + EXECUTOR_URL + DELETE_URL +
                        "/" + SecurityUtil.getCurrentUsername(),
                breakageHeaderName,
                breakageId,
                ParameterizedTypeReference.forType(ApiResponse.class)
        );
    }

    public AppPage getAllBreakages(
            Integer pageSize, Integer pageIndex, String sortBy, String direction,
            boolean statusNew, boolean statusSolved, boolean statusInProgress,
            boolean statusPaused, boolean statusRedirected, boolean statusCancelled,
            boolean priorityUrgently, boolean priorityHigh, boolean priorityMedium, boolean priorityLow,
            String executor, boolean deadline, String searchText) {

        Role role = SecurityUtil.getCurrentUserRole();
        String currentUserDepartmentId = null;
        String currentUserId = null;

        if (role.equals(Role.EMPLOYEE)) {
            currentUserDepartmentId = SecurityUtil.getCurrentUserDepartment().getId();
        } else {
            currentUserId = SecurityUtil.getCurrentUserId();
        }

        return super.getAllByPage(
                BREAKAGE_SERVER_URL + BASE_URL + BREAKAGE_URL + EMPLOYEE_URL,
                pageSize, pageIndex, sortBy, direction,
                statusNew, statusSolved, statusInProgress, statusPaused, statusRedirected, statusCancelled,
                priorityUrgently, priorityHigh, priorityMedium, priorityLow, executor, deadline, searchText,
                role, currentUserDepartmentId, currentUserId,
                ParameterizedTypeReference.forType(AppPage.class)
        );
    }

    public BreakageEmployeeDto getBreakageEmployee(String breakageHeaderName, String breakageId) {
        return super.get(
                BREAKAGE_SERVER_URL + BASE_URL + BREAKAGE_URL + EMPLOYEE_URL + CURRENT_URL,
                breakageHeaderName,
                breakageId,
                USER_DEPARTMENT_ID_HEADER,
                SecurityUtil.getCurrentUserDepartment().getId(),
                ParameterizedTypeReference.forType(BreakageEmployeeDto.class)
        );
    }

    public ApiResponse getBreakage(String breakageHeaderName, String breakageId) {
        return super.get(
                BREAKAGE_SERVER_URL + BASE_URL + BREAKAGE_URL + TECHNICIAN_URL + CURRENT_URL,
                breakageHeaderName,
                breakageId,
                CURRENT_USER_ID_HEADER,
                SecurityUtil.getCurrentUserId(),
                ParameterizedTypeReference.forType(ApiResponse.class)
        );
    }

    // BREAKAGE_COMMENT
    public ApiResponse createBreakageComment(CreateBreakageCommentDto createBreakageCommentDto,
                                             String breakageHeaderName, String breakageId) {
        return super.post(
                BREAKAGE_SERVER_URL + BASE_URL + BREAKAGE_URL + TECHNICIAN_URL + BREAKAGE_COMMENT_URL,
                createBreakageCommentDto,
                breakageHeaderName,
                breakageId,
                ParameterizedTypeReference.forType(ApiResponse.class)
        );
    }

    public ApiResponse updateBreakageComment(CreateBreakageCommentDto createBreakageCommentDto,
                                             String breakageCommentHeaderName, String breakageCommentId) {
        return super.patch(
                BREAKAGE_SERVER_URL + BASE_URL + BREAKAGE_URL + TECHNICIAN_URL + BREAKAGE_COMMENT_URL,
                createBreakageCommentDto,
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
