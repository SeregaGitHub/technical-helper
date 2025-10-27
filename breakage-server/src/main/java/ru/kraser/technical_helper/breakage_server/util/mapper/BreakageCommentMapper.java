package ru.kraser.technical_helper.breakage_server.util.mapper;

import lombok.experimental.UtilityClass;
import ru.kraser.technical_helper.common_module.dto.breakage_comment.CreateBreakageCommentDto;
import ru.kraser.technical_helper.common_module.model.Breakage;
import ru.kraser.technical_helper.common_module.model.BreakageComment;

import java.time.LocalDateTime;

@UtilityClass
public class BreakageCommentMapper {
    public BreakageComment toBreakageComment(CreateBreakageCommentDto createBreakageCommentDto,
                                             Breakage breakage, String currentUserId) {
        BreakageComment breakageComment = new BreakageComment();
        LocalDateTime now = LocalDateTime.now().withNano(0);

        breakageComment.setBreakage(breakage);
        breakageComment.setComment(createBreakageCommentDto.comment());
        breakageComment.setCreatedBy(currentUserId);
        breakageComment.setCreatedDate(now);
        breakageComment.setLastUpdatedBy(currentUserId);
        breakageComment.setLastUpdatedDate(now);

        return breakageComment;
    }
}
