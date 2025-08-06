package ru.kraser.technical_helper.common_module.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "breakage_comment")
@NoArgsConstructor
@AllArgsConstructor
public class BreakageComment extends BaseEntityShot {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "breakage", nullable = false)
    private Breakage breakage;

    @Column(name = "comment", nullable = false)
    private String comment;
}
