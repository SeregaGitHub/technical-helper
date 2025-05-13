package ru.kraser.technical_helper.gateway.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.kraser.technical_helper.common_module.dto.department.CreateDepartmentDto;

import static ru.kraser.technical_helper.common_module.util.Constant.*;

@Service
public class DepartmentClient extends BaseClient {
    public DepartmentClient(WebClient webClient) {
        super(webClient);
    }

    public String createDepartment(CreateDepartmentDto createDepartmentDto, String jwt) {
        return super.post(
                MAIN_SERVER_URL + BASE_URL + ADMIN_URL + DEPARTMENT_URL,
                createDepartmentDto,
                jwt,
                ParameterizedTypeReference.forType(String.class));
    }

    public String updateDepartment(String entityHeaderName, String departmentId,
                                   CreateDepartmentDto createDepartmentDto, String jwt) {
        return super.patch(
                MAIN_SERVER_URL + BASE_URL + ADMIN_URL + DEPARTMENT_URL,
                createDepartmentDto,
                jwt,
                entityHeaderName,
                departmentId,
                ParameterizedTypeReference.forType(String.class)
        );
    }
}
