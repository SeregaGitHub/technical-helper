package ru.kraser.technical_helper.gateway.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kraser.technical_helper.common_module.dto.auth.AuthenticationRequest;
import ru.kraser.technical_helper.common_module.dto.auth.AuthenticationResponse;
import ru.kraser.technical_helper.gateway.client.AuthenticationClient;

import static ru.kraser.technical_helper.common_module.util.Constant.*;

@RestController
@RequestMapping(value = BASE_URL)
@RequiredArgsConstructor
@Validated
public class AuthenticationGatewayController {
    private final AuthenticationClient authenticationClient;

    @PostMapping(value = AUTH_URL)
    public ResponseEntity<AuthenticationResponse> authenticate(
            @Validated() @RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authenticationClient.authenticate(request));
    }
}
