package ru.kraser.technical_helper.common_module.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import ru.kraser.technical_helper.common_module.enums.Priority;
import ru.kraser.technical_helper.common_module.enums.Status;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Setter
@Table(name = "breakage")
@NoArgsConstructor
@AllArgsConstructor
public class Breakage extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department", nullable = false)
    private Department department;

    @Column(name = "room", length = 128, nullable = false)
    private String room;

    @Column(name = "breakage_topic", length = 128, nullable = false)
    private String breakageTopic;

    @Column(name = "breakage_text", length = 2048, nullable = false)
    private String breakageText;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private Status status;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private Priority priority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "executor")
    private User executor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "executor_appointed_by")
    private User executorAppointedBy;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Breakage breakage = (Breakage) o;
        return Objects.equals(room, breakage.room) &&
                Objects.equals(breakageTopic, breakage.breakageTopic) &&
                Objects.equals(breakageText, breakage.breakageText) &&
                Objects.equals(id, breakage.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, room, breakageTopic, breakageText);
    }
}
