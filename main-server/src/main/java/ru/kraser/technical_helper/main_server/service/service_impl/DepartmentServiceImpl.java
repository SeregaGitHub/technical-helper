package ru.kraser.technical_helper.main_server.service.service_impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kraser.technical_helper.common_module.dto.department.CreateDepartmentDto;
import ru.kraser.technical_helper.common_module.dto.department.DepartmentDto;
import ru.kraser.technical_helper.common_module.exception.NotFoundException;
import ru.kraser.technical_helper.main_server.model.Department;
import ru.kraser.technical_helper.main_server.repository.DepartmentRepository;
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
    public String createDepartment(CreateDepartmentDto createDepartmentDto) {
        try {
            departmentRepository.saveAndFlush(DepartmentMapper.toDepartment(createDepartmentDto));
        } catch (Exception e) {
            ThrowException.departmentHandler(e.getMessage(), createDepartmentDto.name());
        }
        return "Отдел: " + createDepartmentDto.name() + ", - был успешно создан.";
    }

    @Override
    @Transactional
    public String updateDepartment(String departmentId, CreateDepartmentDto departmentDto) {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        int response;
        try {
            // TODO - change to the current user
            response = departmentRepository.updateDepartment(
                    departmentId, departmentDto.name(), "some_new_id", now);
            ThrowException.isExist(response, "отдел");
        } catch (Exception e) {
            ThrowException.departmentHandler(e.getMessage(), departmentDto.name());
        }
        return "Отделу успешно присвоено имя - " + departmentDto.name() + ".";
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentDto> getAllDepartments() {
        return departmentRepository.getAllDepartments();
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentDto getDepartment(String departmentId) {
        Department department = departmentRepository.findByIdAndEnabledTrue(departmentId).orElseThrow(
                () -> new NotFoundException(DEPARTMENT_NOT_EXIST)
        );
        return DepartmentMapper.toDepartmentDto(department);
    }

    @Override
    @Transactional
    public String deleteDepartment(String departmentId) {
        int response;
        response = departmentRepository.deleteDepartment(departmentId);

        if (response != 1) {
            throw new NotFoundException(DEPARTMENT_NOT_EXIST);
        }
        return "Отдел - был успешно удалён.";
    }

}
