package ru.kraser.technical_helper.breakage_server.util.mapper;

import lombok.experimental.UtilityClass;
import ru.kraser.technical_helper.common_module.dto.breakage.CreateBreakageDto;
import ru.kraser.technical_helper.common_module.enums.Priority;
import ru.kraser.technical_helper.common_module.enums.Status;
import ru.kraser.technical_helper.common_module.model.Breakage;
import ru.kraser.technical_helper.common_module.model.Department;
import ru.kraser.technical_helper.common_module.util.SecurityUtil;

import java.time.LocalDateTime;

@UtilityClass
public class BreakageMapper {

    public Breakage toBreakage(CreateBreakageDto createBreakageDto) {
        Breakage breakage = new Breakage();

        LocalDateTime now = LocalDateTime.now().withNano(0);
        Department currentUserDepartment = SecurityUtil.getCurrentUserDepartment();
        String currentUserId = SecurityUtil.getCurrentUserId();

        breakage.setDepartment(currentUserDepartment);
        breakage.setRoom(createBreakageDto.room());
        breakage.setBreakageTopic(createBreakageDto.breakageTopic());
        breakage.setBreakageText(createBreakageDto.breakageText());
        breakage.setStatus(Status.NEW);
        breakage.setPriority(Priority.MEDIUM);
        breakage.setCreatedBy(currentUserId);
        breakage.setCreatedDate(now);
        breakage.setLastUpdatedBy(currentUserId);
        breakage.setLastUpdatedDate(now);

        return breakage;
    }
}
