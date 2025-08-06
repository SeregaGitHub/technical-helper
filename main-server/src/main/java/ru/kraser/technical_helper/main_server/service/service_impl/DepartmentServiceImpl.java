package ru.kraser.technical_helper.main_server.service.service_impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kraser.technical_helper.common_module.dto.api.ApiResponse;
import ru.kraser.technical_helper.common_module.dto.department.CreateDepartmentDto;
import ru.kraser.technical_helper.common_module.dto.department.DepartmentDto;
import ru.kraser.technical_helper.common_module.exception.NotFoundException;
import ru.kraser.technical_helper.common_module.model.Department;
import ru.kraser.technical_helper.main_server.repository.DepartmentRepository;
import ru.kraser.technical_helper.common_module.util.SecurityUtil;
import ru.kraser.technical_helper.main_server.service.DepartmentService;
import ru.kraser.technical_helper.main_server.util.error_handler.ThrowException;
import ru.kraser.technical_helper.main_server.util.mapper.DepartmentMapper;

import java.time.LocalDateTime;
import java.util.List;

import static ru.kraser.technical_helper.common_module.util.Constant.DEPARTMENT_NOT_EXIST;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public ApiResponse createDepartment(CreateDepartmentDto createDepartmentDto) {
        try {
            departmentRepository.saveAndFlush(DepartmentMapper.toDepartment(createDepartmentDto));
        } catch (Exception e) {
            ThrowException.departmentHandler(e.getMessage(), createDepartmentDto.name());
        }
        return ApiResponse.builder()
                .message("Отдел: " + createDepartmentDto.name() + ", - был успешно создан.")
                .status(201)
                .httpStatus(HttpStatus.CREATED)
                .timestamp(LocalDateTime.now().withNano(0))
                .build();
    }

    @Override
    @Transactional
    public ApiResponse updateDepartment(String departmentId, CreateDepartmentDto departmentDto) {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        int response;
        try {
            response = departmentRepository.updateDepartment(
                    departmentId, departmentDto.name(), SecurityUtil.getCurrentUserId(), now);
            ThrowException.isExist(response, "отдел");
        } catch (Exception e) {
            ThrowException.departmentHandler(e.getMessage(), departmentDto.name());
        }
        return ApiResponse.builder()
                .message("Отдел: " + departmentDto.name() + " - был успешно изменен.")
                .status(200)
                .httpStatus(HttpStatus.OK)
                .timestamp(now)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentDto> getAllDepartments() {
        return departmentRepository.getAllDepartments();
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentDto getDepartment(String departmentName) {
        Department department = departmentRepository.findByNameAndEnabledTrue(departmentName).orElseThrow(
                () -> new NotFoundException(DEPARTMENT_NOT_EXIST)
        );
        return DepartmentMapper.toDepartmentDto(department);
    }

    @Override
    @Transactional
    public ApiResponse deleteDepartment(String departmentId) {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        int response;
        response = departmentRepository.deleteDepartment(departmentId, SecurityUtil.getCurrentUserId(), now);

        if (response != 1) {
            throw new NotFoundException(DEPARTMENT_NOT_EXIST);
        }
        return ApiResponse.builder()
                .message("Отдел - был успешно удалён.")
                .status(200)
                .httpStatus(HttpStatus.OK)
                .timestamp(LocalDateTime.now().withNano(0))
                .build();
    }

}
