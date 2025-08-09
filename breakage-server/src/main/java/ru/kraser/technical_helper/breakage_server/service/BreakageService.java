package ru.kraser.technical_helper.breakage_server.service;

import ru.kraser.technical_helper.common_module.dto.api.ApiResponse;
import ru.kraser.technical_helper.common_module.dto.api.AppPage;
import ru.kraser.technical_helper.common_module.dto.breakage.CreateBreakageDto;

public interface BreakageService {

    ApiResponse createBreakage(CreateBreakageDto createBreakageDto);

    ApiResponse cancelBreakage(String breakageId, String breakageDepartmentId);

    AppPage getAllBreakages(
            Integer pageSize, Integer pageIndex, String sortBy, String direction,
            boolean statusNew, boolean statusSolved, boolean statusInProgress,
            boolean statusPaused, boolean statusRedirected, boolean statusCancelled);
}
