package ru.kraser.technical_helper.main_server.service.service_impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kraser.technical_helper.common_module.dto.department.CreateDepartmentDto;
import ru.kraser.technical_helper.common_module.dto.department.DepartmentDto;
import ru.kraser.technical_helper.main_server.model.Department;
import ru.kraser.technical_helper.main_server.repository.DepartmentRepository;
import ru.kraser.technical_helper.main_server.service.DepartmentService;
import ru.kraser.technical_helper.main_server.util.mapper.DepartmentMapper;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public DepartmentDto createDepartment(CreateDepartmentDto createDepartmentDto) {
        //TODO - add try/catch
        Department department = departmentRepository.save(DepartmentMapper.toDepartment(createDepartmentDto));
        return DepartmentMapper.toDepartmentDto(department);
    }
}
