package ru.kraser.technical_helper.main_server.service.service_impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.kraser.technical_helper.common_module.dto.user.CreateUserDto;
import ru.kraser.technical_helper.common_module.exception.AlreadyExistsException;
import ru.kraser.technical_helper.common_module.exception.NotFoundException;
import ru.kraser.technical_helper.main_server.model.Department;
import ru.kraser.technical_helper.main_server.repository.DepartmentRepository;
import ru.kraser.technical_helper.main_server.repository.UserRepository;
import ru.kraser.technical_helper.main_server.service.UserService;
import ru.kraser.technical_helper.main_server.util.mapper.UserMapper;

import static ru.kraser.technical_helper.common_module.util.Constant.MESSAGE_DEPARTMENT_NOT_FOUND_EXCEPTION;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public String createUser(CreateUserDto createUserDto) {
        Department department = departmentRepository.findByIdAndEnabledTrue(createUserDto.departmentId())
                .orElseThrow(() -> new NotFoundException(MESSAGE_DEPARTMENT_NOT_FOUND_EXCEPTION));
        try {
            userRepository.saveAndFlush(UserMapper.toUser(createUserDto, department));
        } catch (Exception e) {
            if (e.getMessage().contains("Reason code: Canceled on identification as a pivot, during write.")) {
                throw new NotFoundException(MESSAGE_DEPARTMENT_NOT_FOUND_EXCEPTION);
            } else {
                throw new AlreadyExistsException("Сотрудник: " + createUserDto.username() + ", - уже существует." +
                        " Используйте другое имя !!!");
            }
        }
        return "Сотрудник: " + createUserDto.username() + ", - был успешно создан.";
    }
}
