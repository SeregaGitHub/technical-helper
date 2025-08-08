package ru.kraser.technical_helper.breakage_server.service;

import ru.kraser.technical_helper.common_module.dto.api.ApiResponse;
import ru.kraser.technical_helper.common_module.dto.breakage.EmployeeBreakageDto;
import ru.kraser.technical_helper.common_module.dto.breakage.CreateBreakageDto;

import java.util.List;

public interface BreakageService {

    ApiResponse createBreakage(CreateBreakageDto createBreakageDto);

    ApiResponse cancelBreakage(String breakageId, String breakageDepartmentId);

    List<EmployeeBreakageDto> getAllBreakages(
            Integer size, Integer from, String sortBy, String direction);
}
