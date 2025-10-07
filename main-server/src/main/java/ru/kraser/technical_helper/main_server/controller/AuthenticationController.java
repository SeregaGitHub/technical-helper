package ru.kraser.technical_helper.main_server.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kraser.technical_helper.common_module.dto.auth.AuthenticationRequest;
import ru.kraser.technical_helper.common_module.model.User;
import ru.kraser.technical_helper.main_server.service.UserService;

import static ru.kraser.technical_helper.common_module.util.Constant.AUTH_URL;
import static ru.kraser.technical_helper.common_module.util.Constant.BASE_URL;

@RestController
@RequestMapping(value = BASE_URL)
@Slf4j
@RequiredArgsConstructor
public class AuthenticationController {
    // private final UserRepository userRepository;
    private final UserService userService;

    /*@PostMapping(value = AUTH_URL)
    public ResponseEntity<User> authenticate(@RequestBody AuthenticationRequest request) {
        log.info("Authenticating User with name - {}", request.username());
        ResponseEntity<AuthenticationResponse> response = ResponseEntity.ok(service.authenticate(request));
        var user = userRepository.findUserByUsernameAndEnabledTrue(request.username()).orElseThrow(
                () -> new NotFoundException("Пользователь с логином - " + request.username() + ", не был найден.")
        );
        ResponseEntity<User> response = ResponseEntity.ok(user);
        log.info("User with name - {}, successfully authenticated", request.username());
        return response;
    }*/

    @PostMapping(value = AUTH_URL)
    public ResponseEntity<User> authenticate(@RequestBody AuthenticationRequest request) {
        log.info("Authenticating User with name - {}", request.username());
        //ResponseEntity<AuthenticationResponse> response = ResponseEntity.ok(service.authenticate(request));
        User user = userService.getUserByName(request.username());
        ResponseEntity<User> response = ResponseEntity.ok(user);
        log.info("User with name - {}, successfully authenticated", request.username());
        return response;
    }
}
