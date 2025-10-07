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
import ru.kraser.technical_helper.main_server.util.error_handler.ThrowMainServerException;
import ru.kraser.technical_helper.main_server.util.mapper.DepartmentMapper;

import java.time.LocalDateTime;
import java.util.List;

import static ru.kraser.technical_helper.common_module.util.Constant.*;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public ApiResponse createDepartment(CreateDepartmentDto createDepartmentDto, String currentUserId) {
        try {
            departmentRepository.saveAndFlush(DepartmentMapper.toDepartment(createDepartmentDto, currentUserId));
        } catch (Exception e) {
            ThrowMainServerException.departmentHandler(e.getMessage(), createDepartmentDto.name());
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
    public ApiResponse updateDepartment(String departmentId, CreateDepartmentDto departmentDto, String currentUserId) {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        int response;
        try {
            response = departmentRepository.updateDepartment(
                    departmentId, departmentDto.name(), currentUserId, now);
            ThrowMainServerException.isExist(response, "отдел");
        } catch (Exception e) {
            ThrowMainServerException.departmentHandler(e.getMessage(), departmentDto.name());
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
    public DepartmentDto getDepartment(String headerName, String dep) {
/*        if (headerName.equals(DEPARTMENT_NAME_HEADER)) {
            Department department = departmentRepository.findByNameAndEnabledTrue(dep).orElseThrow(
                    () -> new NotFoundException(DEPARTMENT_NOT_EXIST)
            );
            return DepartmentMapper.toDepartmentDto(department);
        } else if (headerName.equals(DEPARTMENT_ID_HEADER)) {*/
            return departmentRepository.getDepartmentById(dep).orElseThrow(
                    () -> new NotFoundException(DEPARTMENT_NOT_EXIST)
            );
/*        } else {
            throw new NotFoundException(DEPARTMENT_NOT_EXIST);
        }*/
    }

    @Override
    @Transactional
    public ApiResponse deleteDepartment(String departmentId, String currentUserId) {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        int response;
        response = departmentRepository.deleteDepartment(departmentId, currentUserId, now);

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
