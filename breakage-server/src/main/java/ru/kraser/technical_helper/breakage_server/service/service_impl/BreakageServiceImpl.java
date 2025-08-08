package ru.kraser.technical_helper.breakage_server.service.service_impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kraser.technical_helper.breakage_server.repository.BreakageRepository;
import ru.kraser.technical_helper.breakage_server.service.BreakageService;
import ru.kraser.technical_helper.breakage_server.util.error_handler.ThrowBreakageServerException;
import ru.kraser.technical_helper.breakage_server.util.mapper.BreakageMapper;
import ru.kraser.technical_helper.common_module.dto.api.ApiResponse;
import ru.kraser.technical_helper.common_module.dto.breakage.BreakageDto;
import ru.kraser.technical_helper.common_module.dto.breakage.CreateBreakageDto;
import ru.kraser.technical_helper.common_module.enums.Role;
import ru.kraser.technical_helper.common_module.enums.Status;
import ru.kraser.technical_helper.common_module.exception.ForbiddenException;
import ru.kraser.technical_helper.common_module.exception.NotFoundException;
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
    public List<BreakageDto> getAllBreakages(
            Integer size, Integer from, String sortBy, String direction) {

        Sort.Direction breakagesDirection = direction.equals("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(breakagesDirection, sortBy);
        PageRequest pageRequest = PageRequest.of(from / size, size, sort);
        // size - item on page
        // from - size * pageNumber
        String currentUserDepartmentId = SecurityUtil.getCurrentUserDepartment().getId();

        if (SecurityUtil.getCurrentUserRole() == Role.EMPLOYEE) {
            return breakageRepository.getAllEmployeeBreakages(currentUserDepartmentId, pageRequest);
        } else {
            return breakageRepository.getAllBreakages(pageRequest);
        }
    }
    // ?from=0&size=10&sortBy=room&direction=ASC
}
