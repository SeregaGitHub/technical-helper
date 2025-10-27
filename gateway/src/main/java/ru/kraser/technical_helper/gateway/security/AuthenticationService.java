package ru.kraser.technical_helper.gateway.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import ru.kraser.technical_helper.common_module.dto.auth.AuthenticationRequest;
import ru.kraser.technical_helper.common_module.dto.auth.AuthenticationResponse;
import ru.kraser.technical_helper.common_module.exception.AuthException;
import ru.kraser.technical_helper.common_module.model.JwtUserDetails;
import ru.kraser.technical_helper.common_module.model.User;
import ru.kraser.technical_helper.gateway.client.AuthenticationClient;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final AuthenticationClient authenticationClient;
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

        User user = authenticationClient.authenticate(request);

        var jwtToken = jwtService.generateToken(new JwtUserDetails(user));

        return AuthenticationResponse.builder()
                .thJwt(jwtToken)
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }
}
