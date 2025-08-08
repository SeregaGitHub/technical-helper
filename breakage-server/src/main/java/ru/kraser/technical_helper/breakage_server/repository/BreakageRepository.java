package ru.kraser.technical_helper.breakage_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.kraser.technical_helper.common_module.enums.Status;
import ru.kraser.technical_helper.common_module.model.Breakage;

import java.time.LocalDateTime;

@Repository
public interface BreakageRepository extends JpaRepository<Breakage, String> {

    @Modifying
    @Query(
            value = """
                    UPDATE Breakage
                    SET
                    status = :status,
                    lastUpdatedBy = :lastUpdatedBy,
                    lastUpdatedDate = :lastUpdatedDate
                    WHERE id = :breakageId
                    """
    )
    int cancelBreakage(String breakageId, Status status, String lastUpdatedBy, LocalDateTime lastUpdatedDate);
}
