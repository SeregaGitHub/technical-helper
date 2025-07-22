package ru.kraser.technical_helper.main_server.model;

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
public class BaseEntityShort {

    @Column(name = "last_updated_by", length = 36)
    protected String lastUpdatedBy;

    @Column(name = "last_updated_date")
    protected LocalDateTime lastUpdatedDate;
}
