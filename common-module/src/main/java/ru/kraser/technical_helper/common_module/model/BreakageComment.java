package ru.kraser.technical_helper.common_module.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Entity
@Getter
@Setter
@Table(name = "breakage_comment")
@NoArgsConstructor
@AllArgsConstructor
public class BreakageComment extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "breakage", nullable = false)
    private Breakage breakage;

    @Column(name = "comment", nullable = false)
    private String comment;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BreakageComment that = (BreakageComment) o;
        return Objects.equals(breakage, that.breakage) &&
                Objects.equals(comment, that.comment) &&
                Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, breakage, comment);
    }
}
