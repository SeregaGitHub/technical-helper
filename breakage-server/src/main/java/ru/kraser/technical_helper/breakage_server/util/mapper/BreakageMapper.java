package ru.kraser.technical_helper.breakage_server.util.mapper;

import lombok.experimental.UtilityClass;
import ru.kraser.technical_helper.common_module.dto.breakage.BreakageDto;
import ru.kraser.technical_helper.common_module.dto.breakage.BreakageFullDto;
import ru.kraser.technical_helper.common_module.dto.breakage.CreateBreakageFullDto;
import ru.kraser.technical_helper.common_module.dto.breakage_comment.BreakageCommentBackendDto;
import ru.kraser.technical_helper.common_module.dto.breakage_comment.BreakageCommentFrontDto;
import ru.kraser.technical_helper.common_module.enums.Priority;
import ru.kraser.technical_helper.common_module.enums.Status;
import ru.kraser.technical_helper.common_module.model.Breakage;

import java.time.LocalDateTime;
import java.util.List;

@UtilityClass
public class BreakageMapper {

    public Breakage toBreakage(CreateBreakageFullDto createBreakageFullDto, String currentUserId) {
        Breakage breakage = new Breakage();
        LocalDateTime now = LocalDateTime.now().withNano(0);

        breakage.setDepartment(createBreakageFullDto.department());
        breakage.setRoom(createBreakageFullDto.room());
        breakage.setBreakageTopic(createBreakageFullDto.breakageTopic());
        breakage.setBreakageText(createBreakageFullDto.breakageText());
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
            BreakageDto breakageDto, List<BreakageCommentBackendDto> backComments, String currentUserId) {
        List<BreakageCommentFrontDto> comments = backComments.stream()
                .map(comment -> toFrontCommentDto(comment, currentUserId))
                .toList();

        return BreakageFullDto.builder()
                .id(breakageDto.id())
                .departmentId(breakageDto.departmentId())
                .breakageExecutorId(breakageDto.breakageExecutorId())
                .departmentName(breakageDto.departmentName())
                .room(breakageDto.room())
                .breakageTopic(breakageDto.breakageTopic())
                .breakageText(breakageDto.breakageText())
                .status(breakageDto.status())
                .priority(breakageDto.priority())
                .breakageExecutor(breakageDto.breakageExecutor())
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
                .creatorName(backendDto.creatorName())
                .createdDate(backendDto.createdDate())
                .lastUpdatedDate(backendDto.lastUpdatedDate())
                .build();
    }
}
