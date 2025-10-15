package ru.kraser.technical_helper.common_module.dto.breakage;

import lombok.Builder;
import ru.kraser.technical_helper.common_module.model.Department;

@Builder
public record CreateBreakageFullDto(
        String room,
        String breakageTopic,
        String breakageText,
        Department department
) {
}
