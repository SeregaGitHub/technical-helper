package ru.kraser.technical_helper.common_module.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BaseEntityShot {

    @Column(name = "last_updated_by", length = 36)
    public String lastUpdatedBy;

    @Column(name = "last_updated_date")
    public LocalDateTime lastUpdatedDate;
}
