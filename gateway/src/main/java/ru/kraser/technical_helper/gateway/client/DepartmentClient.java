package ru.kraser.technical_helper.gateway.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.kraser.technical_helper.common_module.dto.api.ApiResponse;
import ru.kraser.technical_helper.common_module.dto.department.CreateDepartmentDto;
import ru.kraser.technical_helper.common_module.dto.department.DepartmentDto;

import java.util.List;

import static ru.kraser.technical_helper.common_module.util.Constant.*;

@Service
public class DepartmentClient extends BaseClient {
    public DepartmentClient(WebClient webClient) {
        super(webClient);
    }

    public ApiResponse createDepartment(CreateDepartmentDto createDepartmentDto, String jwt) {
        return super.post(
                MAIN_SERVER_URL + BASE_URL + ADMIN_URL + DEPARTMENT_URL,
                createDepartmentDto,
                jwt,
                ParameterizedTypeReference.forType(ApiResponse.class));
    }

    public ApiResponse updateDepartment(String departmentHeaderName, String departmentId,
                                   CreateDepartmentDto createDepartmentDto, String jwt) {
        return super.patch(
                MAIN_SERVER_URL + BASE_URL + ADMIN_URL + DEPARTMENT_URL,
                createDepartmentDto,
                jwt,
                departmentHeaderName,
                departmentId,
                ParameterizedTypeReference.forType(ApiResponse.class)
        );
    }

    public List<DepartmentDto> getAllDepartments(String jwt) {
        return super.getAll(
                MAIN_SERVER_URL + BASE_URL + ADMIN_URL + DEPARTMENT_URL + ALL_URL,
                jwt,
                ParameterizedTypeReference.forType(DepartmentDto.class)
        );
    }

    public DepartmentDto getDepartment(String departmentId, String jwt, String departmentHeaderName) {
        return super.get(
                MAIN_SERVER_URL + BASE_URL + ADMIN_URL + DEPARTMENT_URL + CURRENT_URL,
                jwt,
                departmentHeaderName,
                departmentId,
                ParameterizedTypeReference.forType(DepartmentDto.class)
        );
    }

    public ApiResponse deleteDepartment(String departmentHeaderName, String departmentId, String jwt) {
        return super.delete(
                MAIN_SERVER_URL + BASE_URL + ADMIN_URL + DEPARTMENT_URL + DELETE_URL,
                jwt,
                departmentHeaderName,
                departmentId,
                ParameterizedTypeReference.forType(ApiResponse.class)
        );
    }
}
