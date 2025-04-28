package ru.kraser.technical_helper.main_server.service.service_impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kraser.technical_helper.common_module.dto.department.CreateDepartmentDto;
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
    public String createDepartment(CreateDepartmentDto createDepartmentDto) {
        //TODO - add exception
        try {
            departmentRepository.save(DepartmentMapper.toDepartment(createDepartmentDto));
        } catch (Exception e) {
            System.out.println("==========================================");
            System.out.println(e.getMessage());
            System.out.println("==========================================");
        }
        return "Отдел \"" + createDepartmentDto.name() + "\" - был успешно создан";
    }

    @Override
    public Department getDepartment(String departmentId) {
        //TODO - add exception
        return departmentRepository.findByIdAndEnabledTrue(departmentId)
                .orElseThrow(() -> new RuntimeException("Not Found !!!"));
    }
}
