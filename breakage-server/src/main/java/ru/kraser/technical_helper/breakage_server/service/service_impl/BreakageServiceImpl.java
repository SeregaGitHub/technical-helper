package ru.kraser.technical_helper.breakage_server.service.service_impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kraser.technical_helper.breakage_server.repository.BreakageCommentRepository;
import ru.kraser.technical_helper.breakage_server.repository.BreakageRepository;
import ru.kraser.technical_helper.breakage_server.service.BreakageService;
import ru.kraser.technical_helper.breakage_server.util.error_handler.ThrowBreakageServerException;
import ru.kraser.technical_helper.breakage_server.util.mapper.BreakageCommentMapper;
import ru.kraser.technical_helper.breakage_server.util.mapper.BreakageMapper;
import ru.kraser.technical_helper.common_module.dto.api.ApiResponse;
import ru.kraser.technical_helper.common_module.dto.api.AppPage;
import ru.kraser.technical_helper.common_module.dto.breakage.*;
import ru.kraser.technical_helper.common_module.dto.breakage_comment.BreakageCommentBackendDto;
import ru.kraser.technical_helper.common_module.dto.breakage_comment.CreateBreakageCommentDto;
import ru.kraser.technical_helper.common_module.enums.Executor;
import ru.kraser.technical_helper.common_module.enums.Priority;
import ru.kraser.technical_helper.common_module.enums.Role;
import ru.kraser.technical_helper.common_module.enums.Status;
import ru.kraser.technical_helper.common_module.exception.ForbiddenException;
import ru.kraser.technical_helper.common_module.exception.NotCorrectParameter;
import ru.kraser.technical_helper.common_module.exception.NotFoundException;
import ru.kraser.technical_helper.common_module.model.Breakage;
import ru.kraser.technical_helper.common_module.util.AppPageMapper;
import ru.kraser.technical_helper.common_module.util.AppPageUtil;
import ru.kraser.technical_helper.common_module.util.SecurityUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static ru.kraser.technical_helper.common_module.util.Constant.BREAKAGE_COMMENT_NOT_EXIST;
import static ru.kraser.technical_helper.common_module.util.Constant.BREAKAGE_NOT_EXIST;

@Service
@RequiredArgsConstructor
public class BreakageServiceImpl implements BreakageService {
    private final BreakageRepository breakageRepository;
    private final BreakageCommentRepository breakageCommentRepository;

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
            response = breakageRepository.updateBreakageStatus(
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
    @Transactional
    public ApiResponse updateBreakageStatus(String breakageId, UpdateBreakageStatusDto updatedStatus) {
        LocalDateTime now = LocalDateTime.now().withNano(0);

        int response;
        response = breakageRepository.updateBreakageStatus(
                breakageId,
                updatedStatus.status(),
                SecurityUtil.getCurrentUserId(),
                now
        );
        if (response != 1) {
            throw new NotFoundException(BREAKAGE_NOT_EXIST);
        }
        return ApiResponse.builder()
                .message("Статус заявки на неисправность была успешно изменен на - " + updatedStatus.status())
                .status(200)
                .httpStatus(HttpStatus.OK)
                .timestamp(now)
                .build();
    }

    @Override
    @Transactional
    public ApiResponse updateBreakagePriority(String breakageId, UpdateBreakagePriorityDto updatedPriority) {
        LocalDateTime now = LocalDateTime.now().withNano(0);

        int response;
        response = breakageRepository.updateBreakagePriority(
                breakageId,
                updatedPriority.priority(),
                SecurityUtil.getCurrentUserId(),
                now
        );
        if (response != 1) {
            throw new NotFoundException(BREAKAGE_NOT_EXIST);
        }
        return ApiResponse.builder()
                .message("Приоритет заявки на неисправность был успешно изменен на - " + updatedPriority.priority())
                .status(200)
                .httpStatus(HttpStatus.OK)
                .timestamp(now)
                .build();
    }

    @Override
    @Transactional
    public ApiResponse addBreakageExecutor(String breakageId, AppointBreakageExecutorDto appointBreakageExecutorDto) {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        LocalDate deadline = appointBreakageExecutorDto.deadline();

        if (deadline.isBefore(now.toLocalDate())) {
            throw new NotCorrectParameter("Необходимо указать корректный срок исполнения заявки на неисправность.");
        } else {
            int response;
            try {
                response = breakageRepository.addBreakageExecutor(
                        breakageId,
                        appointBreakageExecutorDto.executor(),
                        LocalDateTime.of(deadline, LocalTime.of(23, 59, 59)),
                        SecurityUtil.getCurrentUserId(),
                        now
                );
            } catch (Exception e) {
                throw new NotFoundException("Пользователь, который назначается исполнителем заявки на неисправность, " +
                        "не существует.");
            }
            if (response != 1) {
                throw new NotFoundException(BREAKAGE_NOT_EXIST);
            }
        }
        return ApiResponse.builder()
                .message("Исполнитель заявки на неисправность был успешно назначен.")
                .status(200)
                .httpStatus(HttpStatus.OK)
                .timestamp(now)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AppPage getAllBreakages(
            Integer pageSize, Integer pageIndex, String sortBy, String direction,
            boolean statusNew, boolean statusSolved, boolean statusInProgress,
            boolean statusPaused, boolean statusRedirected, boolean statusCancelled,
            boolean priorityUrgently, boolean priorityHigh,
            boolean priorityMedium, boolean priorityLow,
            String executor, boolean deadline, String searchText) {

        Role currentUserRole = SecurityUtil.getCurrentUserRole();

        PageRequest pageRequest = AppPageUtil.createPageRequest(pageSize, pageIndex, sortBy, direction);

        List<Status> statusList = AppPageUtil.createStatusList(statusNew, statusSolved, statusInProgress,
                statusPaused, statusRedirected, statusCancelled, deadline, currentUserRole, executor);

        List<Priority> priorityList = AppPageUtil.createPriorityList(priorityUrgently, priorityHigh,
                priorityMedium, priorityLow);

        if (currentUserRole == Role.EMPLOYEE) {
            String currentUserDepartmentId = SecurityUtil.getCurrentUserDepartment().getId();
            Page<BreakageDto> pageEmployeeBreakages;
            if (searchText == null || searchText.length() < 3) {
                pageEmployeeBreakages = breakageRepository.getAllEmployeeBreakages(
                                statusList, priorityList, currentUserDepartmentId, pageRequest);
            } else {
                pageEmployeeBreakages = breakageRepository.getAllEmployeeBreakagesByText(
                                statusList, priorityList, currentUserDepartmentId, pageRequest, searchText);
            }
            return AppPageMapper.toAppPage(pageEmployeeBreakages);

        } else if (executor != null && executor.equals(Executor.APPOINTED_TO_ME.name())) {
            String currentUserId = SecurityUtil.getCurrentUserId();
            Page<BreakageDto> pageBreakages;
            if (deadline) {
                LocalDateTime now = LocalDateTime.now().withNano(0);
                if (searchText == null || searchText.length() < 3) {
                    pageBreakages = breakageRepository.getAllDeadlineExpiredBreakagesAppointedToMe(
                            statusList, priorityList, pageRequest, currentUserId, now);
                } else {
                    pageBreakages = breakageRepository.getAllDeadlineExpiredBreakagesByTextAppointedToMe(
                            statusList, priorityList, pageRequest, currentUserId, now, searchText);
                }
            } else {
                if (searchText == null || searchText.length() < 3) {
                    pageBreakages = breakageRepository.getAllBreakagesAppointedToMe(
                            statusList, priorityList, pageRequest, currentUserId);
                } else {
                    pageBreakages = breakageRepository.getAllBreakagesByTextAppointedToMe(
                            statusList, priorityList, pageRequest, currentUserId, searchText);
                }
            }
            return AppPageMapper.toAppPage(pageBreakages);

        } else if (executor != null && executor.equals(Executor.APPOINTED_TO_OTHERS.name())) {
            String currentUserId = SecurityUtil.getCurrentUserId();
            Page<BreakageDto> pageBreakages;
            if (deadline) {
                LocalDateTime now = LocalDateTime.now().withNano(0);
                if (searchText == null || searchText.length() < 3) {
                    pageBreakages = breakageRepository.getAllDeadlineExpiredBreakagesAppointedToOthers(
                            statusList, priorityList, pageRequest, currentUserId, now);
                } else {
                    pageBreakages = breakageRepository.getAllDeadlineExpiredBreakagesByTextAppointedToOthers(
                            statusList, priorityList, pageRequest, currentUserId, now, searchText);
                }
            } else {
                if (searchText == null || searchText.length() < 3) {
                    pageBreakages = breakageRepository.getAllBreakagesAppointedToOthers(
                            statusList, priorityList, pageRequest, currentUserId);
                } else {
                    pageBreakages = breakageRepository.getAllBreakagesByTextAppointedToOthers(
                            statusList, priorityList, pageRequest, currentUserId, searchText);
                }
            }
            return AppPageMapper.toAppPage(pageBreakages);

        } else if (executor != null && executor.equals(Executor.NO_APPOINTED.name())) {
            Page<BreakageDto> pageBreakages;
            if (searchText == null || searchText.length() < 3) {
                pageBreakages = breakageRepository.getAllBreakagesNoAppointed(
                        statusList, priorityList, pageRequest);
            } else {
                pageBreakages = breakageRepository.getAllBreakagesByTextNoAppointed(
                        statusList, priorityList, pageRequest, searchText);
            }
            return AppPageMapper.toAppPage(pageBreakages);

        } else {
            Page<BreakageDto> pageBreakages;
            if (deadline) {
                LocalDateTime now = LocalDateTime.now().withNano(0);
                if (searchText == null || searchText.length() < 3) {
                    pageBreakages =
                            breakageRepository.getAllDeadlineExpiredBreakages(
                                    statusList, priorityList, pageRequest, now);
                } else {
                    pageBreakages =
                            breakageRepository.getAllDeadlineExpiredBreakagesByText(
                                    statusList, priorityList, pageRequest, now, searchText);
                }
            } else {
                if (searchText == null || searchText.length() < 3) {
                    pageBreakages =
                            breakageRepository.getAllBreakages(statusList, priorityList, pageRequest);
                } else {
                    pageBreakages =
                            breakageRepository.getAllBreakagesByText(statusList, priorityList, pageRequest, searchText);
                }
            }
            return AppPageMapper.toAppPage(pageBreakages);
        }
    }

    /*@Override
    @Transactional(readOnly = true)
    public AppPage getBreakagesByText(
            String text, Integer pageIndex, Integer pageSize, String sortBy, String direction) {

        PageRequest pageRequest = AppPageUtil.createPageRequest(pageSize, pageIndex, sortBy, direction);

        Page<BreakageDto> pageBreakages = breakageRepository.getBreakagesByText(text, pageRequest);

        return AppPageMapper.toAppPage(pageBreakages);
    }*/

    @Override
    @Transactional(readOnly = true)
    public BreakageFullDto getBreakage(String breakageId) {
        BreakageDto breakageDto = breakageRepository.getBreakage(breakageId).orElseThrow(
                () -> new NotFoundException("Данная заявка на неисправность не существует.")
        );

        BreakageFullDto breakageFullDto;
        if (SecurityUtil.getCurrentUserRole().equals(Role.EMPLOYEE)) {
            breakageFullDto = BreakageMapper.toBreakageFullDto(breakageDto, Collections.emptyList());
        } else {
            List<BreakageCommentBackendDto> backComments = breakageCommentRepository.getAllBreakageComments(breakageId);
            breakageFullDto = BreakageMapper.toBreakageFullDto(breakageDto, backComments);
        }

        return breakageFullDto;
    }

    // BREAKAGE_COMMENT
    @Override
    @Transactional
    public ApiResponse createBreakageComment(CreateBreakageCommentDto createBreakageCommentDto, String breakageId) {
        try {
            Breakage breakage = breakageRepository.getReferenceById(breakageId);
            breakageCommentRepository.saveAndFlush(BreakageCommentMapper.toBreakageComment(
                    createBreakageCommentDto, breakage));
        } catch (Exception e) {
            throw new NotFoundException("Заявки на неисправность не существует !!!");
        }

        return ApiResponse.builder()
                .message("Комментарий к заявке о неисправности - был успешно создан.")
                .status(201)
                .httpStatus(HttpStatus.CREATED)
                .timestamp(LocalDateTime.now().withNano(0))
                .build();
    }

    @Override
    @Transactional
    public ApiResponse updateBreakageComment(CreateBreakageCommentDto createBreakageCommentDto,
                                             String breakageCommentId) {
        LocalDateTime now = LocalDateTime.now().withNano(0);

        int response = breakageCommentRepository.updateBreakageComment(breakageCommentId,
                    createBreakageCommentDto.comment(), SecurityUtil.getCurrentUserId(), now);

        if (response != 1) {
            throw new NotFoundException(BREAKAGE_COMMENT_NOT_EXIST);
        }

        return ApiResponse.builder()
                .message("Комментарий к заявке на неисправность был успешно обновлен.")
                .status(200)
                .httpStatus(HttpStatus.OK)
                .timestamp(LocalDateTime.now().withNano(0))
                .build();
    }

    @Override
    @Transactional
    public ApiResponse deleteBreakageComment(String breakageCommentId) {
        breakageCommentRepository.deleteById(breakageCommentId);
        return ApiResponse.builder()
                .message("Комментарий к заявке на неисправность был успешно удален.")
                .status(200)
                .httpStatus(HttpStatus.OK)
                .timestamp(LocalDateTime.now().withNano(0))
                .build();
    }

}
