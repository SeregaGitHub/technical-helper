package ru.kraser.technical_helper.gateway.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.kraser.technical_helper.common_module.dto.auth.AuthenticationRequest;
import ru.kraser.technical_helper.common_module.dto.auth.AuthenticationResponse;
import ru.kraser.technical_helper.gateway.security.AuthenticationService;

import static ru.kraser.technical_helper.common_module.util.Constant.*;

@CrossOrigin(origins = FRONT_URL)
@RestController
@RequestMapping(value = BASE_URL)
@RequiredArgsConstructor
@Slf4j
@Validated
public class AuthenticationGatewayController {
    //private final AuthenticationClient authenticationClient;
    private final AuthenticationService authenticationService;

    @PostMapping(value = AUTH_URL)
    public ResponseEntity<AuthenticationResponse> authenticate(
            @Validated() @RequestBody AuthenticationRequest request) {
        log.info("Authenticating User with name - {}", request.username());
        //ResponseEntity<AuthenticationResponse> response = ResponseEntity.ok(authenticationClient.authenticate(request));
        ResponseEntity<AuthenticationResponse> response =
                ResponseEntity.ok(authenticationService.authenticate(request));
        log.info("User with name - {}, successfully authenticated", request.username());
        return response;
    }
}
