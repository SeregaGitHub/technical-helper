package ru.kraser.technical_helper.main_server.service.service_impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.kraser.technical_helper.common_module.dto.department.CreateDepartmentDto;
import ru.kraser.technical_helper.common_module.exception.AlreadyExistsException;
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
        try {
            departmentRepository.saveAndFlush(DepartmentMapper.toDepartment(createDepartmentDto));
        } catch (Exception e) {
            throw new AlreadyExistsException("Отдел: " + createDepartmentDto.name() + ", - уже существует." +
                    " Используйте другое имя !!!");
        }
        return "Отдел: " + createDepartmentDto.name() + ", - был успешно создан.";
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public String deleteDepartment(String departmentId) {
        departmentRepository.deleteDepartment(departmentId);
        return "Отдел - был успешно удалён.";
    }

}
