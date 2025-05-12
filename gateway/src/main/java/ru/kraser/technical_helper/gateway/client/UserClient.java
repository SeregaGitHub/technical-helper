package ru.kraser.technical_helper.gateway.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.kraser.technical_helper.common_module.dto.user.CreateUserDto;

import static ru.kraser.technical_helper.common_module.util.Constant.*;

@Service
public class UserClient extends BaseClient {
    public UserClient(WebClient webClient) {
        super(webClient);
    }

    public String createUser(CreateUserDto createUserDto, String jwt) {
        return super.post(
                MAIN_SERVER_URL + BASE_URL + ADMIN_URL + USER_URL,
                createUserDto,
                jwt,
                ParameterizedTypeReference.forType(String.class));
    }
}
