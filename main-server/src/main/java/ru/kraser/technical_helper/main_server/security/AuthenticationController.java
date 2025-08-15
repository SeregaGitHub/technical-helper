package ru.kraser.technical_helper.main_server.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kraser.technical_helper.common_module.dto.auth.AuthenticationRequest;
import ru.kraser.technical_helper.common_module.dto.auth.AuthenticationResponse;

import static ru.kraser.technical_helper.common_module.util.Constant.AUTH_URL;
import static ru.kraser.technical_helper.common_module.util.Constant.BASE_URL;

@RestController
@RequestMapping(value = BASE_URL)
@Slf4j
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService service;

    @PostMapping(value = AUTH_URL)
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        log.info("Authenticating User with name - {}", request.username());
        ResponseEntity<AuthenticationResponse> response = ResponseEntity.ok(service.authenticate(request));
        log.info("User with name - {}, successfully authenticated", request.username());
        return response;
    }
}
