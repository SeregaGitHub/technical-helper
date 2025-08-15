package ru.kraser.technical_helper.breakage_server.util.mapper;

import lombok.experimental.UtilityClass;
import ru.kraser.technical_helper.common_module.dto.breakage.BreakageDto;
import ru.kraser.technical_helper.common_module.dto.breakage.BreakageFullDto;
import ru.kraser.technical_helper.common_module.dto.breakage.CreateBreakageDto;
import ru.kraser.technical_helper.common_module.dto.breakage_comment.BreakageCommentBackendDto;
import ru.kraser.technical_helper.common_module.dto.breakage_comment.BreakageCommentFrontDto;
import ru.kraser.technical_helper.common_module.enums.Priority;
import ru.kraser.technical_helper.common_module.enums.Status;
import ru.kraser.technical_helper.common_module.model.Breakage;
import ru.kraser.technical_helper.common_module.model.Department;
import ru.kraser.technical_helper.common_module.util.SecurityUtil;

import java.time.LocalDateTime;
import java.util.List;

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
        breakage.setDeadline(null);
        breakage.setCreatedBy(currentUserId);
        breakage.setCreatedDate(now);
        breakage.setLastUpdatedBy(currentUserId);
        breakage.setLastUpdatedDate(now);

        return breakage;
    }

    public BreakageFullDto toBreakageFullDto(
            BreakageDto breakageDto, List<BreakageCommentBackendDto> backComments) {
        String currentUserId = SecurityUtil.getCurrentUserId();

        List<BreakageCommentFrontDto> comments = backComments.stream()
                .map(comment -> toFrontCommentDto(comment, currentUserId))
                .toList();

        return BreakageFullDto.builder()
                .id(breakageDto.id())
                .departmentId(breakageDto.departmentId())
                .departmentName(breakageDto.departmentName())
                .room(breakageDto.room())
                .breakageTopic(breakageDto.breakageTopic())
                .breakageText(breakageDto.breakageText())
                .status(breakageDto.status())
                .priority(breakageDto.priority())
                .executor(breakageDto.executor())
                .executorAppointedBy(breakageDto.executorAppointedBy())
                .createdBy(breakageDto.createdBy())
                .createdDate(breakageDto.createdDate())
                .lastUpdatedBy(breakageDto.lastUpdatedBy())
                .lastUpdatedDate(breakageDto.lastUpdatedDate())
                .deadline(breakageDto.deadline())
                .comments(comments)
                .build();
    }

    private BreakageCommentFrontDto toFrontCommentDto(BreakageCommentBackendDto backendDto, String currentUserId) {
        boolean actionEnabled = currentUserId.equals(backendDto.createdBy());

        return BreakageCommentFrontDto.builder()
                .id(backendDto.id())
                .comment(backendDto.comment())
                .actionEnabled(actionEnabled)
                .build();
    }
}
