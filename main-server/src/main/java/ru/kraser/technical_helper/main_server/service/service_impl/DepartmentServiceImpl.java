package ru.kraser.technical_helper.main_server.service.service_impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kraser.technical_helper.common_module.dto.department.CreateDepartmentDto;
import ru.kraser.technical_helper.main_server.repository.DepartmentRepository;
import ru.kraser.technical_helper.main_server.service.DepartmentService;
import ru.kraser.technical_helper.main_server.util.error_handler.ThrowException;
import ru.kraser.technical_helper.main_server.util.mapper.DepartmentMapper;

import java.time.LocalDateTime;

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
            ThrowException.departmentUkHandler(e.getMessage(), createDepartmentDto.name());
        }
        return "Отдел: " + createDepartmentDto.name() + ", - был успешно создан.";
    }

    @Override
    @Transactional
    public String updateDepartment(String departmentId, CreateDepartmentDto departmentDto) {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        try {
            // TODO - change to the current user
            departmentRepository.updateDepartment(
                    departmentId, departmentDto.name(), "some_new_id", now);
        } catch (Exception e) {
            ThrowException.departmentUkHandler(e.getMessage(), departmentDto.name());
        }
        return "Отделу успешно присвоено имя - " + departmentDto.name() + ".";
    }

    @Override
    @Transactional
    public String deleteDepartment(String departmentId) {
        departmentRepository.deleteDepartment(departmentId);
        return "Отдел - был успешно удалён.";
    }

}
