package ru.kraser.technical_helper.main_server.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import ru.kraser.technical_helper.common_module.dto.auth.AuthenticationRequest;
import ru.kraser.technical_helper.common_module.dto.auth.AuthenticationResponse;
import ru.kraser.technical_helper.common_module.exception.NotFoundException;
import ru.kraser.technical_helper.common_module.exception.AuthException;
import ru.kraser.technical_helper.common_module.model.JwtUserDetails;
import ru.kraser.technical_helper.main_server.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username(),
                            request.password()
                    )
            );
        } catch (AuthenticationException e) {
            throw new AuthException("Неверное имя пользователя или пароль !!!");
        }
        var user = userRepository.findUserByUsernameAndEnabledTrue(request.username()).orElseThrow(
                () -> new NotFoundException("Пользователь с логином - " + request.username() + ", не был найден.")
        );
        var jwtToken = jwtService.generateToken(new JwtUserDetails(user));
        return AuthenticationResponse.builder()
                .thJwt(jwtToken)
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }
}
