package ru.kraser.technical_helper.breakage_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.kraser.technical_helper.common_module.model.BreakageComment;

import java.time.LocalDateTime;

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
}
