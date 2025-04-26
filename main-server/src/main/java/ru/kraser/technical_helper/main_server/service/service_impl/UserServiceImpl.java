package ru.kraser.technical_helper.main_server.service.service_impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kraser.technical_helper.common_module.dto.user.CreateUserDto;
import ru.kraser.technical_helper.common_module.dto.user.NewUser;
import ru.kraser.technical_helper.main_server.repository.UserRepository;
import ru.kraser.technical_helper.main_server.service.UserService;
import ru.kraser.technical_helper.main_server.util.mapper.UserMapper;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public String createUser(CreateUserDto createUserDto) {
        NewUser newUser = UserMapper.toNewUser(createUserDto);
        try {
            userRepository.createUser(
                    newUser.id(),
                    newUser.username(),
                    newUser.password(),
                    newUser.enabled(),
                    newUser.departmentId(),
                    newUser.role().toString(),
                    newUser.createdBy(),
                    newUser.createdDate(),
                    newUser.lastUpdatedBy(),
                    newUser.lastUpdatedDate()
            );
        } catch (Exception e) {
            System.out.println("==========================================");
            System.out.println(e.getMessage());
            System.out.println("==========================================");
        }
        return "Пользователь \"" + createUserDto.username() + "\" - был успешно создан";
    }
}
