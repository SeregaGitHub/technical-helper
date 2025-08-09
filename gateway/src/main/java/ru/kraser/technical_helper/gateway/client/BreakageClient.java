package ru.kraser.technical_helper.gateway.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.kraser.technical_helper.common_module.dto.api.ApiResponse;
import ru.kraser.technical_helper.common_module.dto.breakage.CreateBreakageDto;
import ru.kraser.technical_helper.common_module.dto.breakage.BreakageDto;

import java.util.List;

import static ru.kraser.technical_helper.common_module.util.Constant.*;

@Service
public class BreakageClient extends BaseClient {
    public BreakageClient(WebClient webClient) {
        super(webClient);
    }

    public ApiResponse createBreakage(CreateBreakageDto createBreakageDto, String jwt) {
        return super.post(
                BREAKAGE_SERVER_URL + BASE_URL + BREAKAGE_URL + EMPLOYEE_URL,
                createBreakageDto,
                jwt,
                ParameterizedTypeReference.forType(ApiResponse.class)
        );
    }

    public ApiResponse cancelBreakage(String breakageHeaderName, String breakageId,
                                      String breakageDepartmentHeaderName, String breakageDepartmentId, String jwt) {
        return super.patch(
                BREAKAGE_SERVER_URL + BASE_URL + BREAKAGE_URL + EMPLOYEE_URL,
                jwt,
                breakageHeaderName,
                breakageId,
                breakageDepartmentHeaderName,
                breakageDepartmentId,
                ParameterizedTypeReference.forType(ApiResponse.class)
        );
    }

    public List<BreakageDto> getAllBreakages(
            String jwt, Integer pageSize, Integer pageIndex, String sortBy, String direction) {
        return super.getAllByPage(
                BREAKAGE_SERVER_URL + BASE_URL + BREAKAGE_URL + EMPLOYEE_URL,
                pageSize,
                pageIndex,
                sortBy,
                direction,
                jwt,
                ParameterizedTypeReference.forType(BreakageDto.class)
        );
    }
}
