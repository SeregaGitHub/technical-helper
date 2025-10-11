package ru.kraser.technical_helper.gateway.util.user_util;

import lombok.experimental.UtilityClass;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.kraser.technical_helper.common_module.dto.user.UserPasswordDto;
import ru.kraser.technical_helper.common_module.dto.user.CreateUserDto;

@UtilityClass
public class UserUtil {
    public CreateUserDto hashCreateUserDtoPassword(CreateUserDto createUserDto, PasswordEncoder passwordEncoder) {
        return CreateUserDto.builder()
                .username(createUserDto.username())
                .password(passwordEncoder.encode(createUserDto.password()))
                .departmentId(createUserDto.departmentId())
                .role(createUserDto.role())
                .build();
    }

    public UserPasswordDto hashUserPasswordDto(UserPasswordDto userPasswordDto, PasswordEncoder passwordEncoder) {
        return new UserPasswordDto(passwordEncoder.encode(userPasswordDto.newPassword()));
    }
}
