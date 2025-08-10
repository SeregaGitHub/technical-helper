package ru.kraser.technical_helper.breakage_server.service.service_impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kraser.technical_helper.breakage_server.repository.BreakageRepository;
import ru.kraser.technical_helper.breakage_server.service.BreakageService;
import ru.kraser.technical_helper.breakage_server.util.error_handler.ThrowBreakageServerException;
import ru.kraser.technical_helper.common_module.enums.Executor;
import ru.kraser.technical_helper.common_module.enums.Priority;
import ru.kraser.technical_helper.common_module.util.AppPageMapper;
import ru.kraser.technical_helper.breakage_server.util.mapper.BreakageMapper;
import ru.kraser.technical_helper.common_module.dto.api.ApiResponse;
import ru.kraser.technical_helper.common_module.dto.api.AppPage;
import ru.kraser.technical_helper.common_module.dto.breakage.BreakageDto;
import ru.kraser.technical_helper.common_module.dto.breakage.CreateBreakageDto;
import ru.kraser.technical_helper.common_module.enums.Role;
import ru.kraser.technical_helper.common_module.enums.Status;
import ru.kraser.technical_helper.common_module.exception.ForbiddenException;
import ru.kraser.technical_helper.common_module.exception.NotFoundException;
import ru.kraser.technical_helper.common_module.util.AppPageUtil;
import ru.kraser.technical_helper.common_module.util.SecurityUtil;

import java.time.LocalDateTime;
import java.util.List;

import static ru.kraser.technical_helper.common_module.util.Constant.BREAKAGE_NOT_EXIST;

@Service
@RequiredArgsConstructor
public class BreakageServiceImpl implements BreakageService {
    private final BreakageRepository breakageRepository;

    @Override
    @Transactional
    public ApiResponse createBreakage(CreateBreakageDto createBreakageDto) {
        try {
            breakageRepository.saveAndFlush(BreakageMapper.toBreakage(createBreakageDto));
        } catch (Exception e) {
            ThrowBreakageServerException.breakageHandler(e.getMessage());
        }
        return ApiResponse.builder()
                .message("Заявка о неисправности с темой: " + createBreakageDto.breakageTopic() +
                        ", - была успешно создана.")
                .status(201)
                .httpStatus(HttpStatus.CREATED)
                .timestamp(LocalDateTime.now().withNano(0))
                .build();
    }

    @Override
    @Transactional
    public ApiResponse cancelBreakage(String breakageId, String breakageDepartmentId) {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        Role currentUserRole = SecurityUtil.getCurrentUserRole();
        String userDepartmentId = SecurityUtil.getCurrentUserDepartment().getId();

        if (userDepartmentId.equals(breakageDepartmentId) ||
                currentUserRole == Role.ADMIN || currentUserRole == Role.TECHNICIAN) {
            int response;
            response = breakageRepository.cancelBreakage(
                    breakageId,
                    Status.CANCELLED,
                    SecurityUtil.getCurrentUserId(),
                    now
            );

            if (response != 1) {
                throw new NotFoundException(BREAKAGE_NOT_EXIST);
            }
            return ApiResponse.builder()
                    .message("Заявка на неисправность была успешно отменена.")
                    .status(200)
                    .httpStatus(HttpStatus.OK)
                    .timestamp(now)
                    .build();
        } else {
            throw new ForbiddenException("Только технический специалист или сотрудник отдела, " +
                    "в котором произошла неисправность, могут отменить заявку !!!");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AppPage getAllBreakages(
            Integer pageSize, Integer pageIndex, String sortBy, String direction,
            boolean statusNew, boolean statusSolved, boolean statusInProgress,
            boolean statusPaused, boolean statusRedirected, boolean statusCancelled,
            boolean priorityUrgently, boolean priorityHigh,
            boolean priorityMedium, boolean priorityLow,
            String executor) {

        Sort.Direction breakagesDirection = direction.equals("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(breakagesDirection, sortBy);
        PageRequest pageRequest = PageRequest.of(pageIndex, pageSize, sort);
        // size: item on page (Angular: this.paginator.pageSize)

        // from: pageNumber * size (Angular: this.paginator.pageSize * paginator.pageIndex)
        // NOW - pageIndex (Angular: this.paginator.pageIndex)

        // length: count of all items (Angular: this.paginator.length)

        List<Status> statusList = AppPageUtil.createStatusList(statusNew, statusSolved, statusInProgress,
                statusPaused, statusRedirected, statusCancelled);
        List<Priority> priorityList = AppPageUtil.createPriorityList(priorityUrgently, priorityHigh,
                priorityMedium, priorityLow);

        if (SecurityUtil.getCurrentUserRole() == Role.EMPLOYEE) {
            String currentUserDepartmentId = SecurityUtil.getCurrentUserDepartment().getId();
            Page<BreakageDto> pageEmployeeBreakages =
                    breakageRepository.getAllEmployeeBreakages(
                            statusList, priorityList, currentUserDepartmentId, pageRequest);
            return AppPageMapper.toAppPage(pageEmployeeBreakages);

        } else if (executor != null && executor.equals(Executor.APPOINTED_TO_ME.name())) {
            String currentUserId = SecurityUtil.getCurrentUserId();
            Page<BreakageDto> pageBreakages =
                    breakageRepository.getAllBreakagesAppointedToMe(
                            statusList, priorityList, pageRequest, currentUserId);
            return AppPageMapper.toAppPage(pageBreakages);

        } else if (executor != null && executor.equals(Executor.APPOINTED_TO_OTHERS.name())) {
            String currentUserId = SecurityUtil.getCurrentUserId();
            Page<BreakageDto> pageBreakages =
                    breakageRepository.getAllBreakagesAppointedToOthers(
                            statusList, priorityList, pageRequest, currentUserId);
            return AppPageMapper.toAppPage(pageBreakages);

        } else if (executor != null && executor.equals(Executor.NO_APPOINTED.name())) {
            Page<BreakageDto> pageBreakages =
                    breakageRepository.getAllBreakagesNoAppointed(
                            statusList, priorityList, pageRequest);
            return AppPageMapper.toAppPage(pageBreakages);

        } else {
            Page<BreakageDto> pageBreakages =
                    breakageRepository.getAllBreakages(statusList, priorityList, pageRequest);
            return AppPageMapper.toAppPage(pageBreakages);
        }
    }
// breakage/employee?pageIndex=1&pageSize=10&sortBy=lastUpdatedDate&direction=DESC
// ?from=0&size=10&sortBy=room&direction=ASC
}
