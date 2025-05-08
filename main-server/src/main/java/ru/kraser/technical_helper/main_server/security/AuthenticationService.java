package ru.kraser.technical_helper.main_server.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import ru.kraser.technical_helper.common_module.dto.auth.AuthenticationRequest;
import ru.kraser.technical_helper.common_module.dto.auth.AuthenticationResponse;
import ru.kraser.technical_helper.common_module.exception.NotFoundException;
import ru.kraser.technical_helper.main_server.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        var user = userRepository.findUserByUsernameAndEnabledTrue(request.getUsername()).orElseThrow(
                () -> new NotFoundException("Пользователь с логином - " + request.getUsername() + ", не был найден.")
        );
        var jwtToken = jwtService.generateToken(new JwtUserDetails(user));
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }
}
