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

    public ApiResponse createDepartment(CreateDepartmentDto createDepartmentDto) {
        return super.post(
                MAIN_SERVER_URL + BASE_URL + ADMIN_URL + DEPARTMENT_URL,
                createDepartmentDto,
                ParameterizedTypeReference.forType(ApiResponse.class));
    }

    public ApiResponse updateDepartment(String departmentHeaderName, String departmentId,
                                   CreateDepartmentDto createDepartmentDto) {
        return super.patch(
                MAIN_SERVER_URL + BASE_URL + ADMIN_URL + DEPARTMENT_URL,
                createDepartmentDto,
                departmentHeaderName,
                departmentId,
                ParameterizedTypeReference.forType(ApiResponse.class)
        );
    }

    public List<DepartmentDto> getAllDepartments() {
        return super.getAll(
                MAIN_SERVER_URL + BASE_URL + ADMIN_URL + DEPARTMENT_URL + ALL_URL,
                ParameterizedTypeReference.forType(DepartmentDto.class)
        );
    }

    public DepartmentDto getDepartmentById(String departmentId, String departmentIdHeader) {
        return super.get(
                MAIN_SERVER_URL + BASE_URL + ADMIN_URL + DEPARTMENT_URL + CURRENT_URL,
                departmentIdHeader,
                departmentId,
                ParameterizedTypeReference.forType(DepartmentDto.class)
        );
    }

    public ApiResponse deleteDepartment(String departmentHeaderName, String departmentId) {
        return super.delete(
                MAIN_SERVER_URL + BASE_URL + ADMIN_URL + DEPARTMENT_URL + DELETE_URL,
                departmentHeaderName,
                departmentId,
                ParameterizedTypeReference.forType(ApiResponse.class)
        );
    }
}
