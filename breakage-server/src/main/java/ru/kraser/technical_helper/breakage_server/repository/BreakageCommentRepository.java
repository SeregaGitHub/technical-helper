package ru.kraser.technical_helper.breakage_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.kraser.technical_helper.common_module.dto.breakage_comment.BreakageCommentBackendDto;
import ru.kraser.technical_helper.common_module.model.BreakageComment;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BreakageCommentRepository extends JpaRepository<BreakageComment, String> {

    @Modifying
    @Query(
            value = """
                    UPDATE BreakageComment
                    SET
                    comment = :updatedComment,
                    lastUpdatedBy = :currentUserId,
                    lastUpdatedDate = :lastUpdatedDate
                    WHERE id = :breakageCommentId
                    """
    )
    int updateBreakageComment(String breakageCommentId, String updatedComment,
                              String currentUserId, LocalDateTime lastUpdatedDate);

    @Query(
            value = """
                    SELECT new ru.kraser.technical_helper.common_module.dto.breakage_comment.BreakageCommentBackendDto
                    (bc.id, bc.comment, bc.createdBy, u.username AS creatorName, bc.createdDate, bc.lastUpdatedDate)
                    FROM BreakageComment as bc
                    JOIN User AS u ON bc.createdBy = u.id
                    WHERE bc.breakage.id = :breakageId
                    ORDER BY bc.createdDate DESC
                    """
    )
    List<BreakageCommentBackendDto> getAllBreakageComments(String breakageId);
}
